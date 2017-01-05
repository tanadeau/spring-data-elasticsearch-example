package com.example.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldIndex
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "example", type = "post")
data class Post(
        @Id
        override val id: String?,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        override val visibilities: Set<String>,

        @Field(type = FieldType.Nested)
        val tags: List<Tag>
) : ItemReference, Authorizable

data class Tag(
        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val name: String,

        @Field(type = FieldType.String)
        val value: String
)