package com.example.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldIndex
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "example", type = "account")
data class Account(
        @Id
        override val id: String?,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val groupMemberships: Set<String>,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val username: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val email: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val systemRole: SystemRole,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val givenName: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val familyName: String
) : ItemReference

enum class SystemRole {
    USER,
    ADMIN
}
