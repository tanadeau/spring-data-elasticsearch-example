package com.example.service

import com.example.model.Group
import com.example.repository.GroupRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface GroupService {
    fun save(group: Group): Group

    fun findById(id: String): Group?
    fun findAll(paging: Pageable): Iterable<Group>
}

@Service
class GroupServiceImpl(
        private val groupRepository: GroupRepository,
        private val securityInfoService: SecurityInfoService) : GroupService {
    override fun save(group: Group): Group {
        return groupRepository.save(group)
    }

    override fun findById(id: String): Group? {
        return groupRepository.findByIdUsingAuths(id, securityInfoService.account)
    }

    override fun findAll(paging: Pageable): Iterable<Group> {
       return groupRepository.findAllUsingAuths(securityInfoService.account, paging)
    }
}
