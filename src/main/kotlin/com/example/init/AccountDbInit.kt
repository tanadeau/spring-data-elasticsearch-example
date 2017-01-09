package com.example.init

import com.example.model.Account
import com.example.repository.AccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class AccountDbInit(private val accountRepository: AccountRepository, private val objectMapper: ObjectMapper) {
    @PostConstruct
    fun initializeDb() {
        val accounts: List<Account> = objectMapper.readValue(
                AccountDbInit::class.java.classLoader.getResourceAsStream("initial_accounts.json"))

        accountRepository.save(accounts)
    }
}