package com.example.service

import com.example.model.Authorizable
import com.example.model.Post
import com.example.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.support.MessageBuilder
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
        private var messagingTemplate: MessageSendingOperations<String>) : PostService {
    override fun save(post: Post): Post {
        return postRepository.save(post).apply {
            messagingTemplate.convertAndSend("/topic/activity", this, {msg ->
                MessageBuilder.fromMessage(msg).setHeader(Authorizable::visibilities.name, visibilities).build()
            })
        }
    }

    override fun findById(id: String) = postRepository.findByIdUsingAuths(id, securityInfoService.auths)
    override fun findAll(paging: Pageable) = postRepository.findAllUsingAuths(securityInfoService.auths, paging)
}
