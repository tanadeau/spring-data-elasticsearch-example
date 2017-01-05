package com.example.repository

import com.example.model.Account
import com.example.model.AccountGroupRoles
import com.example.model.Group
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface AccountRepository : ElasticsearchRepository<Account, String> {
    fun findByUsername(username: String): Account?
}

interface GroupRepository : ElasticsearchRepository<Group, String>

interface AccountGroupRolesRepository : ElasticsearchRepository<AccountGroupRoles, String>