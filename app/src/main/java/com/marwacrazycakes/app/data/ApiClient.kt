package com.marwacrazycakes.app.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ApiClient {

    private const val TAG = "ApiClient"
    private const val BASE_URL = BuildConfig.API_BASE_URL.trim().trimEnd('/')

    data class SubmitReviewResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null,
    )

    data class ApproveReviewResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null,
    )

    data class DeleteReviewResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null,
    )

    data class SmsNotifyResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null,
    )

    suspend fun submitReview(author: String, quote: String, rating: Int): SubmitReviewResult = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/reviews")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        val requestBody = JsonObject().apply {
            addProperty("author", author)
            addProperty("quote", quote)
            addProperty("rating", rating)
        }.toString()

        try {
            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(StandardCharsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                SubmitReviewResult(isSuccess = true)
            } else {
                val gson = Gson()
                val errorJson = gson.fromJson(response, JsonObject::class.java)
                val errorMessage = errorJson.get("error")?.asString ?: "Failed to submit review"
                SubmitReviewResult(isSuccess = false, errorMessage = errorMessage)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to submit review", error)
            SubmitReviewResult(isSuccess = false, errorMessage = "Network error: ${error.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun approveReview(id: Int): ApproveReviewResult = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/reviews/$id/approve")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        try {
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val gson = Gson()
                val json = gson.fromJson(response, JsonObject::class.java)
                ApproveReviewResult(isSuccess = true)
            } else {
                val gson = Gson()
                val errorJson = gson.fromJson(response, JsonObject::class.java)
                val errorMessage = errorJson.get("error")?.asString ?: "Failed to approve review"
                ApproveReviewResult(isSuccess = false, errorMessage = errorMessage)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to approve review", error)
            ApproveReviewResult(isSuccess = false, errorMessage = "Network error: ${error.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun deleteReview(id: Int): DeleteReviewResult = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/reviews/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        try {
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val gson = Gson()
                val json = gson.fromJson(response, JsonObject::class.java)
                DeleteReviewResult(isSuccess = true)
            } else {
                val gson = Gson()
                val errorJson = gson.fromJson(response, JsonObject::class.java)
                val errorMessage = errorJson.get("error")?.asString ?: "Failed to delete review"
                DeleteReviewResult(isSuccess = false, errorMessage = errorMessage)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to delete review", error)
            DeleteReviewResult(isSuccess = false, errorMessage = "Network error: ${error.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun sendSmsNotification(name: String, eventType: String, eventDate: String): SmsNotifyResult = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/contact/sms-notify")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 30000

        val requestBody = JsonObject().apply {
            addProperty("name", name)
            addProperty("eventType", eventType)
            addProperty("eventDate", eventDate)
        }.toString()

        try {
            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(StandardCharsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val gson = Gson()
                val json = gson.fromJson(response, JsonObject::class.java)
                SmsNotifyResult(isSuccess = true)
            } else {
                val gson = Gson()
                val errorJson = gson.fromJson(response, JsonObject::class.java)
                val errorMessage = errorJson.get("error")?.asString ?: "Failed to send SMS notification"
                SmsNotifyResult(isSuccess = false, errorMessage = errorMessage)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to send SMS notification", error)
            SmsNotifyResult(isSuccess = false, errorMessage = "Network error: ${error.message}")
        } finally {
            connection.disconnect()
        }
    }
}

// Top-level functions for easier import in composables
suspend fun submitReview(author: String, quote: String, rating: Int): ApiClient.SubmitReviewResult {
    return ApiClient.submitReview(author, quote, rating)
}

suspend fun approveReview(id: Int): ApiClient.ApproveReviewResult {
    return ApiClient.approveReview(id)
}

suspend fun deleteReview(id: Int): ApiClient.DeleteReviewResult {
    return ApiClient.deleteReview(id)
}

suspend fun sendSmsNotification(name: String, eventType: String, eventDate: String): ApiClient.SmsNotifyResult {
    return ApiClient.sendSmsNotification(name, eventType, eventDate)
}