package de.malteharms

import de.malteharms.di.mainModule
import de.malteharms.plugins.*
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    install(Koin) {
        modules(mainModule)
    }

    configureSerialization()
    configureSockets()
    configureRouting()
    configureMonitoring()
    configureSecurity()
}
