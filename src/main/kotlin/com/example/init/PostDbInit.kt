package com.example.init

import com.example.model.Post
import com.example.repository.PostRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class PostDbInit(private val postRepository: PostRepository, private val objectMapper: ObjectMapper) {
    @PostConstruct
    fun initializeDb() {
        val posts: List<Post> = objectMapper.readValue(
                PostDbInit::class.java.classLoader.getResourceAsStream("initial_posts.json"))

        postRepository.save(posts)
    }
}
