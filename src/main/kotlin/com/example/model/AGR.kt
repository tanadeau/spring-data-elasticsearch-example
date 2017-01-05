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
        val authorizations: Set<String>,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val username: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val email: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val masterRole: Role,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val givenName: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val familyName: String
) : ItemReference

@Document(indexName = "example", type = "group")
data class Group(
        @Id
        override val id: String?,

        @Field(type = FieldType.String)
        val name: String,

        @Field(type = FieldType.String)
        val description: String
) : ItemReference

@Document(indexName = "example", type = "accountGroupRoles")
data class AccountGroupRoles(
        @Id
        override val id: String?,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val accountId: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val groupId: String,

        @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
        val roles: Set<Role>
) : ItemReference

enum class Role {
    USER,
    ADMIN
}
