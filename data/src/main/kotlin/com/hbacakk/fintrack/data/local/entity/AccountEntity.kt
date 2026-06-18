package com.hbacakk.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val balance: Double,
    val type: String,
    val currency: String,
    val color: String,
    val isDefault: Boolean,
    val isSynced: Boolean = false,
)