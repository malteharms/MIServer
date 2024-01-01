package de.malteharms

import de.malteharms.plugins.*
import de.malteharms.states.CostsWrapper
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val costState = CostsWrapper()

    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureRouting(costState)
}
