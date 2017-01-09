package com.example.service

import com.example.model.Account
import com.example.repository.AccountRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface AccountService {
    fun save(account: Account): Account

    fun findById(id: String): Account?
    fun findAll(paging: Pageable): Iterable<Account>
    fun findByUsername(username: String): Account?
}

@Service
class AccountServiceImpl(
        private val accountRepository: AccountRepository,
        private val securityInfoService: SecurityInfoService) : AccountService {
    override fun save(account: Account): Account {
        return accountRepository.save(account)
    }

    override fun findById(id: String): Account? {
        return accountRepository.findByIdUsingAuths(id, securityInfoService.account)
    }

    override fun findAll(paging: Pageable): Iterable<Account> {
        return accountRepository.findAllUsingAuths(securityInfoService.account, paging)
    }

    override fun findByUsername(username: String): Account? {
        return accountRepository.findByUsernameUsingAuths(username, securityInfoService.account)
    }
}
