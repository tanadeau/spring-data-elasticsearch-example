package com.example.stomp

import com.example.model.Authorizable
import com.example.repository.AccountRepository
import mu.KLogging
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.context.ApplicationListener
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptorAdapter
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import java.util.*

// make thread safe
val sessionRepo = HashMap<String, KeycloakAuthenticationToken>()

fun Set<*>.containsAny(other: Iterable<*>?): Boolean {
    return other != null && other.any { contains(it) }
}

@Component
class RequestHandshakeMessageInterceptor(val accountRepository: AccountRepository) : ChannelInterceptorAdapter() {
    companion object : KLogging()

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        val sessionHeaders = SimpMessageHeaderAccessor.getSessionAttributes(message.headers)
        if (accessor.messageType == SimpMessageType.MESSAGE) {
            val user = sessionRepo[accessor.sessionId] ?: return null
            logger.info { "channel::presend session headers $sessionHeaders" }
            val username = user.account?.keycloakSecurityContext?.token?.preferredUsername
            logger.info { "CHANNEL::PRESEND to ${accessor.destination} for user ${username.orEmpty()}" }
            val visibilities = accessor.getHeader(Authorizable::visibilities.name) as? Iterable<*>
            if (username == null ||
                    !(accountRepository.findByUsername(username)?.authorizations?.containsAny(visibilities)?:false)) {
                logger.info { "user ${username.orEmpty()} not authorized to see message" }
                return null
            }
        }
        return message
    }
}

@Component
class StompConnectionEvent() : ApplicationListener<SessionConnectEvent> {

    companion object : KLogging()

    override fun onApplicationEvent(event: SessionConnectEvent) {
        val sha = StompHeaderAccessor.wrap(event.message)
        logger.info { "Websocket Connection: $sha from ${event.user?.name ?: "annon"} @ ${event.source}" }
        val user = sha.user as KeycloakAuthenticationToken?
        if (user != null) {
            sha.sessionAttributes["accessToken"] = user.account.keycloakSecurityContext.token
            sessionRepo[sha.sessionId] = user
        } else {
            sessionRepo.remove(sha.sessionId)
        }
    }
}