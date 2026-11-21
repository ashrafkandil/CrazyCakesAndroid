package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.data.Testimonial
import com.marwacrazycakes.app.data.ApiClient
import com.marwacrazycakes.app.data.submitReview
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.cardShadow
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Gold
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ADMIN_PASSWORD = "crazycakes13"

@Composable
fun AdminReviewsScreen() {
    val isWide = rememberIsWide()
    val context = LocalContext.current
    val statusBar = androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val snackbarHostState = remember { SnackbarHostState() }

    var password by remember { mutableStateOf("") }
    var authenticated by remember { mutableStateOf(false) }
    var pendingReviews by remember { mutableStateOf<List<Testimonial>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadPendingReviews() {
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = androidx.sqlite.db.SimpleSQLiteDatabase.openOrCreateDatabase(
                    context.getDatabasePath("crazy-cakes.db").absolutePath,
                    null
                )
                val cursor = db.rawQuery(
                    "SELECT id, author, initials, review_date, quote, rating FROM testimonials WHERE published = 0 ORDER BY id",
                    null
                )
                val reviews = mutableListOf<Testimonial>()
                while (cursor.moveToNext()) {
                    reviews.add(Testimonial(
                        id = cursor.getInt(0),
                        author = cursor.getString(1),
                        initials = cursor.getString(2),
                        date = cursor.getString(3),
                        quote = cursor.getString(4),
                        rating = cursor.getInt(5)
                    ))
                }
                cursor.close()
                db.close()

                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    pendingReviews = reviews
                    loading = false
                }
            } catch (e: Exception) {
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    error = "Failed to load pending reviews: ${e.message}"
                    loading = false
                }
            }
        }
    }

    fun showSnackbar(message: String, isError: Boolean = false) {
        snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Dismiss",
            duration = androidx.compose.material3.SnackbarDuration.Short,
            visuals = if (isError) {
                androidx.compose.material3.SnackbarVisuals(
                    containerColor = androidx.compose.ui.graphics.Color.Red,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            } else {
                androidx.compose.material3.SnackbarVisuals(
                    containerColor = androidx.compose.ui.graphics.Color.Green,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            }
        )
    }

    fun approveReview(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use API call instead of direct DB access
                val result = ApiClient.approveReview(id)
                if (result.isSuccess) {
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        pendingReviews = pendingReviews.filter { it.id != id }
                    }
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        showSnackbar("Review approved successfully")
                    }
                } else {
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        showSnackbar(result.errorMessage ?: "Failed to approve review", isError = true)
                    }
                }
            } catch (e: Exception) {
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    showSnackbar("Error: ${e.message}", isError = true)
                }
            }
        }
    }

    fun deleteReview(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use API call instead of direct DB access
                val result = ApiClient.deleteReview(id)
                if (result.isSuccess) {
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        pendingReviews = pendingReviews.filter { it.id != id }
                    }
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        showSnackbar("Review deleted successfully")
                    }
                } else {
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        showSnackbar(result.errorMessage ?: "Failed to delete review", isError = true)
                    }
                }
            } catch (e: Exception) {
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    showSnackbar("Error: ${e.message}", isError = true)
                }
            }
        }
    }

    if (!authenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBar)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ScriptTitle(
                    text = "Review Moderation",
                    color = Primary,
                    size = 28.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (error != null) {
                    Text(
                        text = error!!,
                        style = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = Color.Red),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                androidx.compose.material3.TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
                    label = { Text(text = "Password", style = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = MutedForeground)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                        containerColor = Card,
                        focusedContainerColor = Card,
                        textColor = Primary,
                        labelColor = MutedForeground,
                        cursorColor = Primary,
                    ),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (password == ADMIN_PASSWORD) {
                            authenticated = true
                            loadPendingReviews()
                        } else {
                            error = "Incorrect password"
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text(text = "Access Reviews", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = PrimaryForeground))
                }
            }
        }
        return
    }

    // Authenticated view
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBar)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ScriptTitle(
                    text = "Pending Reviews",
                    color = Primary,
                    size = 28.sp,
                )
                Button(
                    onClick = { authenticated = false },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Card,
                        contentColor = Primary,
                        borderColor = Border,
                    ),
                ) {
                    Text(text = "Logout", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Primary))
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 56.dp),
                    color = Primary,
                )
            } else if (pendingReviews.isEmpty()) {
                Text(
                    text = "No pending reviews to moderate.",
                    style = TextStyle(fontFamily = Inter, fontSize = 16.sp, color = MutedForeground),
                )
            } else {
                Column(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    pendingReviews.forEach { review ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cardShadow(RoundedCornerShape(12.dp))
                                .background(Card, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = review.initials,
                                        style = TextStyle(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = PrimaryForeground,
                                        ),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.author,
                                        style = TextStyle(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Primary,
                                        ),
                                    )
                                    Text(
                                        text = review.date,
                                        style = TextStyle(
                                            fontFamily = Inter,
                                            fontSize = 11.sp,
                                            color = MutedForeground,
                                        ),
                                    )
                                }
                                Text(
                                    text = "★".repeat(review.rating),
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        color = Gold,
                                    ),
                                )
                            }
                            Text(
                                text = "“${review.quote}”",
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 13.sp,
                                    lineHeight = 25.sp,
                                    color = MutedForeground,
                                ),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = { approveReview(review.id) },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary),
                                ) {
                                    Text(text = "Approve", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PrimaryForeground))
                                }
                                Button(
                                    onClick = { deleteReview(review.id) },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        containerColor = Card,
                                        contentColor = Color.Red,
                                        borderColor = Color.Red,
                                    ),
                                ) {
                                    Text(text = "Delete", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Red))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}