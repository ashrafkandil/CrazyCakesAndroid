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
    val categoryId: Int,
    val slug: String,
    val categoryTitle: String,
)

data class GalleryVideo(
    val id: Int,
    val src: String,
    val title: String,
    val sort: Int,
    val categoryId: Int,
    val slug: String,
    val categoryTitle: String,
)

data class GalleryPage(
    val categories: List<GalleryCategory>,
    val images: List<GalleryImage>,
    val videos: List<GalleryVideo>,
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

data class SocialLink(
    val id: Int,
    val label: String,
    val href: String,
    val icon: String,
    val sort: Int,
    val active: Boolean,
)

data class ContentSnapshot(
    val categories: List<GalleryCategory> = emptyList(),
    val images: List<GalleryImage> = emptyList(),
    val videos: List<GalleryVideo> = emptyList(),
    val testimonials: List<Testimonial> = emptyList(),
    val content: Map<String, String> = emptyMap(),
    val eventTypes: List<String> = emptyList(),
    val socialLinks: List<SocialLink> = emptyList(),
)
