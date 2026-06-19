package com.example.examcall.model

data class ServerInfo(
    val ip: String,
    val name: String,
    val lastSeen: Long = System.currentTimeMillis()
)
