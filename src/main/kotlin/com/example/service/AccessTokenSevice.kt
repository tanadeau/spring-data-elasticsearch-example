package com.example.service

import org.keycloak.KeycloakPrincipal
import org.keycloak.representations.AccessToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AccessTokenService {
    val accessToken: AccessToken
        get() {
            val kcPrincipal = SecurityContextHolder.getContext().authentication.principal as KeycloakPrincipal<*>
            return kcPrincipal.keycloakSecurityContext.token
        }
}
