package com.example.controller

import com.example.model.Group
import com.example.model.SystemRole
import com.example.service.GroupService
import com.example.service.SecurityInfoService
import mu.KLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/group")
class GroupController(private val groupService: GroupService, private val securityInfoService: SecurityInfoService) {
    companion object : KLogging()

    @GetMapping(produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getAll(paging: Pageable): List<Group> {
        return groupService.findAll(paging).toList()
    }

    @GetMapping("/{id}", produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getById(@PathVariable id: String): ResponseEntity<Group> {
        val found = groupService.findById(id)
        return if (found == null) { ResponseEntity.notFound().build() } else { ResponseEntity.ok(found) }
    }

    @PostMapping(
            consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun create(@RequestBody newGroup: Group): ResponseEntity<Group> {
        if (securityInfoService.account.systemRole != SystemRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val saved = groupService.save(newGroup)
        AccountController.logger.info { "Saved new account with ID ${saved.id}" }
        return ResponseEntity.ok(saved)
    }
}