package com.example.controller

import com.example.model.Account
import com.example.model.SystemRole
import com.example.service.AccountService
import com.example.service.SecurityInfoService
import mu.KLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account")
class AccountController(
        private val accountService: AccountService, private val securityInfoService: SecurityInfoService) {
    companion object : KLogging()

    @GetMapping(produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getAll(paging: Pageable): List<Account> {
        return accountService.findAll(paging).toList()
    }

    @GetMapping("/{id}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getById(@PathVariable id: String): ResponseEntity<Account> {
        val found = accountService.findById(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping(
            consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun create(@RequestBody newAccount: Account): ResponseEntity<Account> {
        if (securityInfoService.account.systemRole != SystemRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val saved = accountService.save(newAccount)
        logger.info { "Saved new account with ID ${saved.id}" }
        return ResponseEntity.ok(saved)
    }

    @GetMapping("/whoami", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun getUserInformation(): Account {
        return securityInfoService.account
    }
}