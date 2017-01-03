package com.example.controller

import com.example.model.Post
import com.example.model.Tag
import com.example.service.PostService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("/post")
class PostController(val postService: PostService) {
    companion object : KLogging()

    @GetMapping
    fun getAll(): List<Post> {
        return postService.findAll().toList()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Post> {
        val found = postService.findOne(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody newPost: Post) {
        val saved = postService.save(newPost)
        logger.info { "Saved new post with ID ${saved.id}" }
    }
}