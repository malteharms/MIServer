package de.malteharms.di

import de.malteharms.data.DataSource
import de.malteharms.data.DataSourceImpl
import de.malteharms.sessions.SessionController
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    single {
        KMongo.createClient()
            .coroutine
            .getDatabase("app_db")
    }

    single<DataSource> {
        DataSourceImpl(get())
    }

    single {
        SessionController(get())
    }
}