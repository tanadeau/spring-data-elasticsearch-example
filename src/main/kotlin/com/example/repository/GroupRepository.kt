package com.example.repository

import com.example.model.Account
import com.example.model.Group
import com.example.model.SystemRole
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component

interface GroupRepository : ElasticsearchRepository<Group, String>, GroupRepositoryCustom

interface GroupRepositoryCustom : CustomRepository<Group>

@Component
class GroupRepositoryImpl(private val elasticsearchTemplate: ElasticsearchTemplate) : GroupRepositoryCustom {
    override fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<Group> {
        val query: QueryBuilder = when(userAccount.systemRole) {
            SystemRole.USER -> QueryBuilders.boolQuery()
                    .should(QueryBuilders.termQuery(Group::discoverable.name, true))
                    .should(QueryBuilders.idsQuery().ids(userAccount.groupMemberships))
            SystemRole.ADMIN -> QueryBuilders.matchAllQuery()
        }

        val queryBuilder = NativeSearchQueryBuilder()
                .withQuery(query)
                .withPageable(paging)

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Group::class.java)
    }

    override fun findByIdUsingAuths(id: String, userAccount: Account): Group? {
        val queryBuilder = NativeSearchQueryBuilder().withQuery(QueryBuilders.idsQuery().ids(id))

        if (userAccount.systemRole != SystemRole.ADMIN && id !in userAccount.groupMemberships) {
            queryBuilder.withFilter(QueryBuilders.termQuery(Group::discoverable.name, true))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Group::class.java).firstOrNull()
    }
}
