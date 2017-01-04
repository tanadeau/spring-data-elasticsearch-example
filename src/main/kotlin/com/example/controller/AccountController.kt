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
        val token = principal.keycloakSecurityContext.getToken()

        val id = token.getId()
        val firstName = token.getGivenName()
        val lastName = token.getFamilyName()
        return mapOf("id" to id, "firstName" to firstName, "lastName" to lastName, "username" to token.preferredUsername )
    }
}