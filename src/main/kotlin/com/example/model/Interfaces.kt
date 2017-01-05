package com.example.model

interface ItemReference {
    val id: String?
}

interface Authorizable {
    val visibilities: Set<String>
}
