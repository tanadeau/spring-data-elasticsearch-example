package com.example.controller

import com.example.model.Post
import com.example.service.PostService
import com.example.service.SecurityInfoService
import mu.KLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/post")
class PostController(private val postService: PostService, private val securityInfoService: SecurityInfoService) {
    companion object : KLogging()

    @GetMapping(produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getAllWithAuths(paging: Pageable): List<Post> {
        return postService.findAll(securityInfoService.account.authorizations, paging).toList()
    }

    @GetMapping("/{id}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getById(@PathVariable id: String): ResponseEntity<Post> {
        val found = postService.findById(id, securityInfoService.account.authorizations)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping(
            consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody newPost: Post): Post {
        val saved = postService.save(newPost)
        logger.info { "Saved new post with ID ${saved.id}" }
        return saved
    }
}