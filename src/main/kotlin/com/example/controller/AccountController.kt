package com.example.controller

import com.example.model.Account
import com.example.model.Role
import com.example.service.AccessTokenService
import com.example.service.AccountService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account")
class AccountController(
        private val accountService: AccountService, private val accessTokenService: AccessTokenService) {
    companion object : KLogging()

    @GetMapping(produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getAll(): List<Account> {
        return accountService.findAll().toList()
    }

    @GetMapping("/{id}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getById(@PathVariable id: String): ResponseEntity<Account> {
        val found = accountService.findOne(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping(
            consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody newAccount: Account): Account {
        val saved = accountService.save(newAccount)
        logger.info { "Saved new account with ID ${saved.id}" }
        return saved
    }

    @GetMapping("/whoami", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun getUserInformation(): Account {
        val token = accessTokenService.accessToken

        return Account(
                null, emptySet(), token.preferredUsername, token.email, Role.ADMIN, token.givenName, token.familyName)
    }
}