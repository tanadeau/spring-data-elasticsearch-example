package com.example.service

import com.example.config.KeycloakAccountAuthenticationToken
import com.example.model.Account
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SecurityInfoService {
    val authentication: KeycloakAccountAuthenticationToken
        get() = SecurityContextHolder.getContext().authentication as KeycloakAccountAuthenticationToken

    val account: Account
        get() = authentication.domainAccount

    val auths: Set<String>
        get() = account.groupMemberships
}
