package com.example.service

import com.example.model.Account
import com.example.model.Group
import com.example.repository.AccountGroupRolesRepository
import com.example.repository.AccountRepository
import com.example.repository.GroupRepository
import org.springframework.stereotype.Service

interface AccountService {
    fun save(account: Account): Account
    fun findOne(id: String): Account?
    fun findAll(): Iterable<Account>
    fun findByUsername(username: String): Account?
}

interface GroupService {
    fun save(group: Group): Group
    fun findOne(id: String): Group?
    fun findAll(): Iterable<Group>
}

interface AccountGroupRolesService

@Service
class AccountServiceImpl(private val accountRepository: AccountRepository) : AccountService {
    override fun save(account: Account): Account {
        return accountRepository.save(account)
    }

    override fun findOne(id: String): Account? {
        return accountRepository.findOne(id)
    }

    override fun findAll(): Iterable<Account> {
        return accountRepository.findAll()
    }

    override fun findByUsername(username: String): Account? {
        return accountRepository.findByUsername(username)
    }
}

@Service
class GroupServiceImpl(private val groupRepository: GroupRepository) : GroupService {
    override fun save(group: Group): Group {
        return groupRepository.save(group)
    }

    override fun findOne(id: String): Group? {
        return groupRepository.findOne(id)
    }

    override fun findAll(): Iterable<Group> {
       return groupRepository.findAll()
    }
}

class AccountGroupRolesServiceImpl(
        private val accountGroupRolesRepository: AccountGroupRolesRepository
) : AccountGroupRolesService