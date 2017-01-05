package com.example.repository

import com.example.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostRepository : ElasticsearchRepository<Post, String> {
    fun findByTagsName(name: String, pageable: Pageable): Page<Post>
}