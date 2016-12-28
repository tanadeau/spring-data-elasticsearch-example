package com.example.controller

import com.example.model.Post
import com.example.model.Tag
import com.example.service.PostService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/post")
class PostController(val postService: PostService) {
    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Post> {
        val found = postService.findOne(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create() {
        val newPost = Post("testId", listOf(Tag("tag1", "value1"), Tag("tag2", "value2")))
        postService.save(newPost)
    }
}