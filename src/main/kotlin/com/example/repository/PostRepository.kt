package com.example.repository

import com.example.model.Account
import com.example.model.Post
import com.example.model.SystemRole
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component

interface PostRepository : ElasticsearchRepository<Post, String>, PostRepositoryCustom

interface PostRepositoryCustom {
    fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<Post>
    fun findByIdUsingAuths(id: String, userAccount: Account): Post?
}

@Component
class PostRepositoryImpl(private val elasticsearchTemplate: ElasticsearchTemplate) : PostRepositoryCustom {
    override fun findAllUsingAuths(userAccount: Account, paging: Pageable): Page<Post> {
        val queryBuilder = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(paging)

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(QueryBuilders.termsQuery(Post::visibilities.name, authsWithPublic(userAccount)))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Post::class.java)
    }

    override fun findByIdUsingAuths(id: String, userAccount: Account): Post? {
        val queryBuilder = NativeSearchQueryBuilder().withQuery(QueryBuilders.idsQuery().ids(id))

        if (userAccount.systemRole != SystemRole.ADMIN) {
            queryBuilder.withFilter(QueryBuilders.termsQuery(Post::visibilities.name, authsWithPublic(userAccount)))
        }

        return elasticsearchTemplate.queryForPage(queryBuilder.build(), Post::class.java).firstOrNull()
    }

    private fun authsWithPublic(userAccount: Account) = userAccount.groupMemberships.plus("public")
}