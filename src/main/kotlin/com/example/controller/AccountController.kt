package com.example.controller

import com.example.service.AccessTokenService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
class AccountController(val accessTokenService: AccessTokenService) {
    @GetMapping("/whoami", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun getUserInformation(): Map<String, Any> {
        val token = accessTokenService.accessToken
        return mapOf(
                "id" to token.id,
                "name" to token.name,
                "givenName" to token.givenName,
                "familyName" to token.familyName,
                "username" to token.preferredUsername
        )
    }
}