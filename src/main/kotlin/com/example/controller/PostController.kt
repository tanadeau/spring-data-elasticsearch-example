package com.example.controller

import com.example.model.Post
import com.example.model.Tag
import com.example.service.PostService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.atomic.AtomicInteger

val counter = AtomicInteger(0)

@RestController
@RequestMapping("/post")
class PostController(val postService: PostService) {


    @GetMapping
    fun getById(): ResponseEntity<Iterable<Post>> {
        val found = postService.findAll()
        return ResponseEntity.ok(found)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Post> {
        val found = postService.findOne(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create() {
        val newPost = Post(null, listOf(Tag("tag1", "value${counter.andIncrement}"), Tag("tag2", "value${counter.andIncrement}")))
        postService.save(newPost)
    }
}