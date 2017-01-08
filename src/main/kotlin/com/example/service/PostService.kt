package com.example.service

import com.example.model.Post
import com.example.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface PostService {
    fun save(post: Post): Post

    fun findAll(paging: Pageable): Page<Post>
    fun findById(id: String): Post?
}

@Service
class PostServiceImpl(
        private val postRepository: PostRepository,
        private val securityInfoService: SecurityInfoService,
        private val liveEventService: StompLiveEventsService) : PostService {

    override fun save(post: Post): Post = postRepository.save(post).apply {
        liveEventService.save(this)
    }

    override fun findById(id: String) = postRepository.findByIdUsingAuths(id, securityInfoService.auths)
    override fun findAll(paging: Pageable) = postRepository.findAllUsingAuths(securityInfoService.auths, paging)
}
