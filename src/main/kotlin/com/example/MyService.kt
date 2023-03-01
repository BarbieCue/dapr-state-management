package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val servicePort = 8080
const val sidecarPort = 3500
const val stateStore = "statestore"

fun main() {
    embeddedServer(Netty, port = servicePort, host = "0.0.0.0", module = Application::module).start(wait = true)
}

@Serializable
data class RequestBody( // https://docs.dapr.io/reference/api/state_api/#request-body
    val key: String,
    val value: MyData
)

@Serializable
data class MyData(
    val name: String,
    val age: Int
)

fun Application.module() {
    routing {

        get("/") {

            val client = HttpClient(CIO) {
                install(Logging) {
                    level = LogLevel.ALL
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                    })
                }
            }

            // save
            client.post("http://localhost:$sidecarPort/v1.0/state/$stateStore") {
                contentType(ContentType.Application.Json)
                setBody(arrayOf(RequestBody("myKey", MyData("Peter", 24))))
            }

            // get
            client.get("http://localhost:$sidecarPort/v1.0/state/$stateStore/myKey") {
                contentType(ContentType.Application.Json)
            }
//
            // delete
            client.delete("http://localhost:$sidecarPort/v1.0/state/$stateStore/myKey") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}