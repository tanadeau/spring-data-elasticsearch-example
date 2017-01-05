package com.example.service

import com.example.model.Post
import com.example.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface PostService {
    fun save(post: Post): Post

    fun findAll(userAuths: Set<String>, paging: Pageable): Page<Post>
    fun findById(id: String, userAuths: Set<String>): Post?
}

@Service
class PostServiceImpl(private val postRepository: PostRepository) : PostService {
    override fun save(post: Post): Post {
        return postRepository.save(post)
    }

    override fun findById(id: String, userAuths: Set<String>): Post? {
        return postRepository.findByIdUsingAuths(id, userAuths)
    }

    override fun findAll(userAuths: Set<String>, paging: Pageable): Page<Post> {
        return postRepository.findAllUsingAuths(userAuths, paging)
    }
}
