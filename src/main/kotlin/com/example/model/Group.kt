package com.example.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldIndex
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "example", type = "group")
data class Group(
        @Id
        override val id: String?,

        @Field(type = FieldType.String)
        val name: String,

        @Field(type = FieldType.String)
        val description: String,

        @Field(type = FieldType.Boolean)
        val discoverable: Boolean,

        @Field(type = FieldType.Boolean)
        val globallyReadable: Boolean,

        @Field(type = FieldType.Object)
        val roles: GroupRoles
) : ItemReference

data class GroupRoles(
        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val users: Set<String>,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val admins: Set<String>
)
