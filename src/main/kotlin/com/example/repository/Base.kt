package com.example.repository

import com.example.model.Account
import com.example.model.Authorizable
import com.example.model.SystemRole
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder

interface CustomRepository<T> {
    fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<T>
    fun findByIdUsingAuths(id: String, userAccount: Account): T?
}

class AuthorizableCustomRepositoryImpl<T: Authorizable>(
        private val elasticsearchTemplate: ElasticsearchTemplate,
        private val javaModelClass: Class<T>) : CustomRepository<T> {
    override fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<T> {
        val queryBuilder = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(paging)

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(
                    QueryBuilders.termsQuery(Authorizable::visibilities.name, authsWithPublic(userAccount)))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), javaModelClass)
    }

    override fun findByIdUsingAuths(id: String, userAccount: Account): T? {
        val queryBuilder = NativeSearchQueryBuilder().withQuery(QueryBuilders.idsQuery().ids(id))

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(
                    QueryBuilders.termsQuery(Authorizable::visibilities.name, authsWithPublic(userAccount)))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), javaModelClass).firstOrNull()
    }

    private fun authsWithPublic(userAccount: Account) = userAccount.groupMemberships.plus("public")
}