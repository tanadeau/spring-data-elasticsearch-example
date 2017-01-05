package com.example.init

import com.example.model.Account
import com.example.model.Group
import com.example.repository.AccountRepository
import com.example.repository.GroupRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class AccountDbInit(val accountRepository: AccountRepository, val objectMapper: ObjectMapper) {
    @PostConstruct
    fun initializeDb() {
        val accounts: List<Account> = objectMapper.readValue(
                AccountDbInit::class.java.classLoader.getResourceAsStream("initial_accounts.json"))

        accountRepository.save(accounts)
    }
}

@Component
class GroupDbInit(val groupRepository: GroupRepository, val objectMapper: ObjectMapper) {
    @PostConstruct
    fun initializeDb() {
        val groups: List<Group> = objectMapper.readValue(
                GroupDbInit::class.java.classLoader.getResourceAsStream("initial_groups.json"))

        groupRepository.save(groups)
    }
}