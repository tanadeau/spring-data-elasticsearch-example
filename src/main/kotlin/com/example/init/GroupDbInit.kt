package com.example.init

import com.example.model.Group
import com.example.repository.GroupRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class GroupDbInit(private val groupRepository: GroupRepository, private val objectMapper: ObjectMapper) {
    @PostConstruct
    fun initializeDb() {
        val groups: List<Group> = objectMapper.readValue(
                GroupDbInit::class.java.classLoader.getResourceAsStream("initial_groups.json"))

        groupRepository.save(groups)
    }
}