package com.example.core.model

enum class UserRole {
    USER,
    AUDITOR,
    ADMIN
}

enum class Permission {
    VIEW_OWN_PROFILE,
    UPDATE_RAW_PROFILE,
    SUBMIT_EVIDENCE,
    REQUEST_ASSESSMENT,
    VIEW_AUDIT_LOGS,
    CALCULATE_OFFICIAL_STATE,
    PROMOTE_EVOLUTION_CLASS,
    MODIFY_CORE_RULES
}

object RolePermissions {
    private val rolePermissionMap = mapOf(
        UserRole.USER to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.UPDATE_RAW_PROFILE,
            Permission.SUBMIT_EVIDENCE,
            Permission.REQUEST_ASSESSMENT
        ),
        UserRole.AUDITOR to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_AUDIT_LOGS
        ),
        UserRole.ADMIN to Permission.values().toSet()
    )

    fun getPermissionsForRole(role: UserRole): Set<Permission> {
        return rolePermissionMap[role] ?: emptySet()
    }

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return getPermissionsForRole(role).contains(permission)
    }
}
