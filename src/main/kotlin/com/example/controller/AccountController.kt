package com.example.controller

import org.keycloak.KeycloakPrincipal
import org.keycloak.KeycloakSecurityContext
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/account")
class AccountController {
    @GetMapping("/whoami", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun getUserInformation(principal: Principal): Map<String, Any> {
        val kcPrincipal = (principal as KeycloakAuthenticationToken).principal as KeycloakPrincipal<KeycloakSecurityContext>
        val token = kcPrincipal.keycloakSecurityContext.token


        return mapOf(
                "id" to token.id,
                "name" to token.name,
                "givenName" to token.givenName,
                "familyName" to token.familyName,
                "username" to token.preferredUsername
        )
    }
}