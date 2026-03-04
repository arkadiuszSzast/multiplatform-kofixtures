package co.example.domain

data class Article(
    val id: Int,
    val title: String,
    val author: User,
    val status: Status,
)
