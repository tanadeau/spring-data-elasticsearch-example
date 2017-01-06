package com.example.init

import com.example.model.Account
import com.example.model.Group
import com.example.repository.AccountRepository
import com.example.repository.GroupRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class AccountDbInit(private val accountRepository: AccountRepository, private val objectMapper: ObjectMapper) {
    companion object : KLogging()

    @PostConstruct
    fun initializeDb() {
        val accounts: List<Account> = objectMapper.readValue(
                AccountDbInit::class.java.classLoader.getResourceAsStream("initial_accounts.json"))

        accountRepository.save(accounts)
    }
}

@Component
class GroupDbInit(private val groupRepository: GroupRepository, private val objectMapper: ObjectMapper) {
    companion object : KLogging()

    @PostConstruct
    fun initializeDb() {
        val groups: List<Group> = objectMapper.readValue(
                GroupDbInit::class.java.classLoader.getResourceAsStream("initial_groups.json"))

        groupRepository.save(groups)
    }
}