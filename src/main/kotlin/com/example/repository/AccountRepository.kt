package com.example.repository

import com.example.model.Account
import com.example.model.SystemRole
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component

interface AccountRepository : ElasticsearchRepository<Account, String>, AccountRepositoryCustom {
    fun findByUsername(username: String): Account?
}

interface AccountRepositoryCustom : CustomRepository<Account> {
    fun findByUsernameUsingAuths(username: String, userAccount: Account): Account?
}

@Component
class AccountRepositoryImpl(private val elasticsearchTemplate: ElasticsearchTemplate) : AccountRepositoryCustom {
    override fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<Account> {
        val queryBuilder = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(paging)

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(
                    QueryBuilders.termsQuery(Account::groupMemberships.name, userAccount.groupMemberships))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Account::class.java)
    }

    override fun findByIdUsingAuths(id: String, userAccount: Account): Account? {
        val queryBuilder = NativeSearchQueryBuilder().withQuery(QueryBuilders.idsQuery().ids(id))

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(
                    QueryBuilders.termsQuery(Account::groupMemberships.name, userAccount.groupMemberships))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Account::class.java).firstOrNull()
    }

    override fun findByUsernameUsingAuths(username: String, userAccount: Account): Account? {
        val queryBuilder = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(Account::username.name, username))

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(
                    QueryBuilders.termsQuery(Account::groupMemberships.name, userAccount.groupMemberships))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Account::class.java).firstOrNull()
    }
}
