package com.github.marcoral.versatia.engine.api.util

import org.bukkit.Server

var versatiaMotdFirstLine: String = ""
var versatiaMotdSecondLine: String = ""

val Server.versatiaMotd get() = "$versatiaMotdFirstLine\n$versatiaMotdSecondLine"