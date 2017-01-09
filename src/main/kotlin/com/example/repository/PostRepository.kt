package com.example.repository

import com.example.model.Post
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Component

interface PostRepository : ElasticsearchRepository<Post, String>, CustomRepository<Post>

@Component
class PostRepositoryImpl(elasticsearchTemplate: ElasticsearchTemplate) :
        CustomRepository<Post> by AuthorizableCustomRepositoryImpl(elasticsearchTemplate, Post::class.java)