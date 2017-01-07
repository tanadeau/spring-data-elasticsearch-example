package com.example.repository

import com.example.model.Post
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component

interface PostRepository : ElasticsearchRepository<Post, String>, PostRepositoryCustom

interface PostRepositoryCustom {
    fun findAllUsingAuths(userAuths: Set<String>, paging: Pageable): Page<Post>
    fun findByIdUsingAuths(id: String, userAuths: Set<String>): Post?
}

@Component
class PostRepositoryImpl(private val elasticsearchTemplate: ElasticsearchTemplate) : PostRepositoryCustom {
    override fun findByIdUsingAuths(id: String, userAuths: Set<String>): Post? {
        val query = NativeSearchQueryBuilder()
                .withIds(listOf(id))
                .withFilter(QueryBuilders.termsQuery(Post::visibilities.name, userAuths))
                .build()

        return elasticsearchTemplate.queryForPage(query, Post::class.java).firstOrNull()
    }

    override fun findAllUsingAuths(userAuths: Set<String>, paging: Pageable): Page<Post> {
        val query = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withFilter(QueryBuilders.termsQuery(Post::visibilities.name, userAuths))
                .withPageable(paging)
                .build()

        return elasticsearchTemplate.queryForPage(query, Post::class.java)
    }
}