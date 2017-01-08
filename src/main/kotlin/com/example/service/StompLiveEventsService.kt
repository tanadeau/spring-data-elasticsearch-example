package com.example.service

import com.example.model.Authorizable
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

interface StompLiveEventsService {
    fun save(authorizable: Authorizable)
}

@Service
class StompLiveEventsServiceImpl(private var messagingTemplate: MessageSendingOperations<String>) : StompLiveEventsService {
    override fun save(authorizable: Authorizable) {
        messagingTemplate.convertAndSend("/topic/activity", authorizable, { msg ->
            MessageBuilder.fromMessage(msg).setHeader(Authorizable::visibilities.name, authorizable.visibilities).build()
        })
    }

}