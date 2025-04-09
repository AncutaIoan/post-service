package angrymiaucino

import io.r2dbc.spi.Connection
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable

@Serializable
data class City(val name: String, val population: Int)

class CityService(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_CITIES = "CREATE TABLE IF NOT EXISTS cities (id SERIAL PRIMARY KEY, name VARCHAR(255), population INT);"
        private const val SELECT_CITY_BY_ID = "SELECT name, population FROM cities WHERE id = $1"
        private const val INSERT_CITY = "INSERT INTO cities (name, population) VALUES ($1, $2) RETURNING id"
        private const val UPDATE_CITY = "UPDATE cities SET name = $1, population = $2 WHERE id = $3"
        private const val DELETE_CITY = "DELETE FROM cities WHERE id = $1"
    }

    init {
        GlobalScope.launch {
            try {
                connection.createStatement(CREATE_TABLE_CITIES)
                    .execute()
                    .awaitSingle()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }


    suspend fun create(city: City): Int {
        val result = connection.createStatement(INSERT_CITY)
            .bind("$1", city.name)
            .bind("$2", city.population)
            .execute()
            .awaitSingle()

        val row = result.map { row: Row, _: RowMetadata ->
            row.get("id", Integer::class.java)?.toInt()
        }.awaitFirstOrNull()

        return row ?: throw Exception("Failed to retrieve generated ID")
    }

    suspend fun read(id: Int): City {
        val result = connection.createStatement(SELECT_CITY_BY_ID)
            .bind("$1", id)
            .execute()
            .awaitSingle()

        return result.map { row: Row, _: RowMetadata ->
            val name = row.get("name", String::class.java) ?: throw Exception("Name missing")
            val population = row.get("population", Integer::class.java)?.toInt() ?: 0
            City(name, population)
        }.awaitFirstOrNull() ?: throw Exception("City not found")
    }

    suspend fun update(id: Int, city: City) {
        connection.createStatement(UPDATE_CITY)
            .bind("$1", city.name)
            .bind("$2", city.population)
            .bind("$3", id)
            .execute()
            .awaitSingle()
    }

    suspend fun delete(id: Int) {
        connection.createStatement(DELETE_CITY)
            .bind("$1", id)
            .execute()
            .awaitSingle()
    }
}
