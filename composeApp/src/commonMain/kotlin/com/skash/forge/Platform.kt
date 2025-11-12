package com.skash.forge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform