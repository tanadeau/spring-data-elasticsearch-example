package com.example.service

import com.example.model.Post
import com.example.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

interface PostService {
    fun save(post: Post): Post
    fun findOne(id: String): Post?
    fun findAll(): Iterable<Post>
    fun findByTagsName(tagName: String, pageRequest: PageRequest): Page<Post>
}

@Service
class PostServiceImpl(val postRepository: PostRepository): PostService {
    override fun save(post: Post): Post {
        return postRepository.save(post)
    }

    override fun findOne(id: String): Post? {
        return postRepository.findOne(id)
    }

    override fun findAll(): Iterable<Post> {
        return postRepository.findAll()
    }

    override fun findByTagsName(tagName: String, pageRequest: PageRequest): Page<Post> {
        return postRepository.findByTagsName(tagName, pageRequest)
    }
}
