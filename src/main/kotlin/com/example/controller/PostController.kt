package com.example.controller

import com.example.model.Post
import com.example.model.Tag
import com.example.service.PostService
import mu.KLogging
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody



@RestController
@RequestMapping("/post")
class PostController(val postService: PostService) {
    companion object : KLogging()

    @GetMapping(produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getAll(): List<Post> {
        return postService.findAll().toList()
    }

    @GetMapping("/{id}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getById(@PathVariable id: String): ResponseEntity<Post> {
        val found = postService.findOne(id)
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

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun getUserInformation(principal: KeycloakPrincipal<RefreshableKeycloakSecurityContext>): Map<String, String> {
        val token = principal.getKeycloakSecurityContext().getToken()

        val id = token.getId()
        val firstName = token.getGivenName()
        val lastName = token.getFamilyName()
        return mapOf("id" to id, "firstName" to firstName, "lastName" to lastName, "username" to token.preferredUsername )
    }
}