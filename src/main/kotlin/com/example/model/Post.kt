package com.example.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "example", type = "post")
data class Post(
        @Id
        val id: String?,
        @Field(type = FieldType.Nested)
        val tags: List<Tag>
)

data class Tag(
        val id: String,
        val name: String
)