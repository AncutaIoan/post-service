package angrymiaucino

import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Mono

fun Application.configureDatabases() {
    routing {
        // Create city
        post("/cities") {
            val city = call.receive<City>()
            val connection = connectToPostgresReactive().awaitSingle()
            try {
                val cityService = CityService(connection)
                val id = cityService.create(city)
                call.respond(HttpStatusCode.Created, id)
            } finally {
                connection.close().awaitSingle()
            }
        }

        // Read city
        get("/cities/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val connection = connectToPostgresReactive().awaitSingle()
            try {
                val cityService = CityService(connection)
                val city = cityService.read(id)
                call.respond(HttpStatusCode.OK, city)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            } finally {
                connection.close().awaitSingle()
            }
        }

        // Update city
        put("/cities/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val city = call.receive<City>()
            val connection = connectToPostgresReactive().awaitSingle()
            try {
                val cityService = CityService(connection)
                cityService.update(id, city)
                call.respond(HttpStatusCode.OK)
            } finally {
                connection.close().awaitSingle()
            }
        }

        // Delete city
        delete("/cities/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val connection = connectToPostgresReactive().awaitSingle()
            try {
                val cityService = CityService(connection)
                cityService.delete(id)
                call.respond(HttpStatusCode.OK)
            } finally {
                connection.close().awaitSingle()
            }
        }
    }
}


/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */


fun Application.connectToPostgresReactive(): Mono<Connection> {
    val url = environment.config.property("r2dbc.url").getString()
    val connectionFactory = ConnectionFactories.get(url)
    return Mono.from(connectionFactory.create())
}