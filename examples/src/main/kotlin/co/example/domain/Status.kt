package co.example.domain

sealed class Status {
    object Draft : Status()
    object Published : Status()
    data class Rejected(val reason: String) : Status()
}
