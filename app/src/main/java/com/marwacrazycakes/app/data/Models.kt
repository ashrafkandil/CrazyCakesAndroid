package com.marwacrazycakes.app.data

data class GalleryCategory(
    val id: Int,
    val slug: String,
    val title: String,
    val sort: Int,
)

data class GalleryImage(
    val id: Int,
    val src: String,
    val alt: String,
    val width: Int?,
    val height: Int?,
    val sort: Int,
    val featured: Boolean,
    val categorySlug: String,
    val categoryTitle: String,
)

data class GalleryPage(
    val categories: List<GalleryCategory>,
    val images: List<GalleryImage>,
    val activeCategory: String,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val pageCount: Int,
)

data class Testimonial(
    val id: Int,
    val author: String,
    val initials: String,
    val date: String,
    val quote: String,
    val rating: Int,
)
