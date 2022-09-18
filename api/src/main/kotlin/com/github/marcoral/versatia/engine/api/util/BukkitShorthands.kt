package com.github.marcoral.versatia.engine.api.util

import com.github.marcoral.versatia.engine.api.asColored
import org.bukkit.Bukkit

fun bukkitPrintln(message: String) = Bukkit.getConsoleSender().sendMessage(message.asColored())
fun bukkitBroadcast(message: String) = Bukkit.getServer().broadcastMessage(message.asColored())