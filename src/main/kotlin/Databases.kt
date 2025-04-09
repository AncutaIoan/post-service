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
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import reactor.core.publisher.Mono

fun Application.configureDatabases() {
    val dbConnectionMono: Mono<Connection> = connectToPostgresReactive()

    routing {
        // Create city
        post("/cities") {
            val city = call.receive<City>()
            dbConnectionMono.map { connection ->
                val cityService = CityService(connection)
                cityService.create(city)
            }.subscribe { id ->
                call.respond(HttpStatusCode.Created, id)
            }
        }

        // Read city
        get("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            dbConnectionMono.flatMap { connection ->
                val cityService = CityService(connection)
                cityService.read(id)
                    .doOnTerminate { connection.close() }
            }.subscribe({ city ->
                call.respond(HttpStatusCode.OK, city)
            }, { e ->
                call.respond(HttpStatusCode.NotFound)
            })
        }

        // Update city
        put("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<City>()
            dbConnectionMono.flatMap { connection ->
                val cityService = CityService(connection)
                cityService.update(id, user)
                    .doOnTerminate { connection.close() }
            }.subscribe {
                call.respond(HttpStatusCode.OK)
            }
        }

        // Delete city
        delete("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            dbConnectionMono.flatMap { connection ->
                val cityService = CityService(connection)
                cityService.delete(id)
                    .doOnTerminate { connection.close() }
            }.subscribe {
                call.respond(HttpStatusCode.OK)
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
    val user = environment.config.property("r2dbc.username").getString()
    val password = environment.config.property("r2dbc.password").getString()

    // Use the ConnectionFactoryOptions builder with the correct option keys.
    val connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.DRIVER, "postgresql") // specify the driver
        .option(ConnectionFactoryOptions.PROTOCOL, "postgresql") // specify the protocol
        .option(ConnectionFactoryOptions.HOST, "localhost") // Host where your db is running
        .option(ConnectionFactoryOptions.PORT, 5432) // Default PostgreSQL port
        .option(ConnectionFactoryOptions.USER, user) // Database username
        .option(ConnectionFactoryOptions.PASSWORD, password) // Database password
        .option(ConnectionFactoryOptions.DATABASE, "postgres") // Database name
        .build()

    val connectionFactory = ConnectionFactories.get(connectionFactoryOptions)

    // Return a Mono<Connection> representing the async connection creation
    return connectionFactory.create()
}