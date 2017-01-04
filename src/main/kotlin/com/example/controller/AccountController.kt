package com.example.controller

import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
class AccountController {
    @GetMapping("/whoami", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun getUserInformation(principal: KeycloakPrincipal<RefreshableKeycloakSecurityContext>): Map<String, String> {
        val token = principal.keycloakSecurityContext.token

        return mapOf(
                "id" to token.id,
                "firstName" to token.givenName,
                "lastName" to token.familyName,
                "username" to token.preferredUsername)
    }
}