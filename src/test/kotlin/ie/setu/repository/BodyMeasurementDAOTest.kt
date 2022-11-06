package ie.setu.repository

import ie.setu.domain.BodyMeasurement
import ie.setu.domain.db.BodyMeasurements
import ie.setu.utils.mapToBodyMeasurement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ie.setu.domain.repository.BodyMeasurementDAO
import ie.setu.helpers.bodyMeasurements
import ie.setu.helpers.populateBodyMeasurementTable
import ie.setu.helpers.populateUserTable
import kotlin.test.assertEquals


//retrieving some test data from Fixtures
private val bodyMeasurement1 = bodyMeasurements.get(0)
private val bodyMeasurement2 = bodyMeasurements.get(1)
private val bodyMeasurement3 = bodyMeasurements.get(2)

class BodyMeasurementDAOTest {

    companion object {
        //Make a connection to a local, in memory H2 database.
        @BeforeAll
        @JvmStatic
        internal fun setupInMemoryDatabaseConnection() {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
        }
    }

    @Nested
    inner class CreateBodyMeasurements {

        @Test
        fun `multiple bodyMeasurements added to table can be retrieved successfully`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
                assertEquals(bodyMeasurement1, bodyMeasurementDAO.findByBodyMeasurementId(bodyMeasurement1.id))
                assertEquals(bodyMeasurement2, bodyMeasurementDAO.findByBodyMeasurementId(bodyMeasurement2.id))
                assertEquals(bodyMeasurement3, bodyMeasurementDAO.findByBodyMeasurementId(bodyMeasurement3.id))
            }
        }
    }

    @Nested
    inner class ReadBodyMeasurements {

        @Test
        fun `getting all bodyMeasurements from a populated table returns all rows`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
            }
        }

        @Test
        fun `get bodyMeasurement by user id that has no bodyMeasurements, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(0, bodyMeasurementDAO.findByUserId(3).size)
            }
        }

        @Test
        fun `get bodyMeasurement by user id that exists, results in a correct bodyMeasurement(s) returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(bodyMeasurement1, bodyMeasurementDAO.findByUserId(1).get(0))
                assertEquals(bodyMeasurement2, bodyMeasurementDAO.findByUserId(1).get(1))
                assertEquals(bodyMeasurement3, bodyMeasurementDAO.findByUserId(2).get(0))
            }
        }

        @Test
        fun `get all bodyMeasurements over empty table returns none`() {
            transaction {

                //Arrange - create and setup bodyMeasurementDAO object
                SchemaUtils.create(BodyMeasurements)
                val bodyMeasurementDAO = BodyMeasurementDAO()

                //Act & Assert
                assertEquals(0, bodyMeasurementDAO.getAll().size)
            }
        }

        @Test
        fun `get bodyMeasurement by bodyMeasurement id that has no records, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(null, bodyMeasurementDAO.findByBodyMeasurementId(4))
            }
        }

        @Test
        fun `get bodyMeasurement by bodyMeasurement id that exists, results in a correct bodyMeasurement returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()
                //Act & Assert
                assertEquals(bodyMeasurement1, bodyMeasurementDAO.findByBodyMeasurementId(1))
                assertEquals(bodyMeasurement3, bodyMeasurementDAO.findByBodyMeasurementId(3))
            }
        }
    }

    @Nested
    inner class UpdateBodyMeasurements {

        @Test
        fun `updating existing bodyMeasurement in table results in successful update`() {
            transaction {

                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                val bodyMeasurement3updated = BodyMeasurement(id = 3, weight = 68.5, height = 177.8,
                    waist = 80.9, chest = 90.0, userId = 3)
                bodyMeasurementDAO.updateByBodyMeasurementId(bodyMeasurement3updated.id,  bodyMeasurement3updated)
                assertEquals(bodyMeasurement3updated, bodyMeasurementDAO.findByBodyMeasurementId(3))
            }
        }

        @Test
        fun `updating non-existant  bodyMeasurement in table results in no updates`() {
            transaction {

                //Arrange - create and populate tables with three users and three  bodyMeasurements
                val userDAO = populateUserTable()
                val  bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                val  bodyMeasurement4updated =  BodyMeasurement(id = 3, weight = 68.5, height = 177.8,
                    waist = 80.9, chest = 90.0, userId = 3)
                bodyMeasurementDAO.updateByBodyMeasurementId(4, bodyMeasurement4updated)
                assertEquals(null, bodyMeasurementDAO.findByBodyMeasurementId(4))
                assertEquals(3, bodyMeasurementDAO.getAll().size)
            }
        }
    }

    @Nested
    inner class DeleteBodyMeasurements {

        @Test
        fun `deleting a non-existant bodyMeasurement (by id) in table results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
                bodyMeasurementDAO.deleteByBodyMeasurementId(4)
                assertEquals(3, bodyMeasurementDAO.getAll().size)
            }
        }

        @Test
        fun `deleting an existing bodyMeasurement (by id) in table results in record being deleted`() {
            transaction {

                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
                bodyMeasurementDAO.deleteByBodyMeasurementId(bodyMeasurement3.id)
                assertEquals(2, bodyMeasurementDAO.getAll().size)
            }
        }


        @Test
        fun `deleting bodyMeasurements when none exist for user id results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
                bodyMeasurementDAO.deleteByUserId(3)
                assertEquals(3, bodyMeasurementDAO.getAll().size)
            }
        }

        @Test
        fun `deleting bodyMeasurements when 1 or more exist for user id results in deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three bodyMeasurements
                val userDAO = populateUserTable()
                val bodyMeasurementDAO = populateBodyMeasurementTable()

                //Act & Assert
                assertEquals(3, bodyMeasurementDAO.getAll().size)
                bodyMeasurementDAO.deleteByUserId(1)
                assertEquals(1, bodyMeasurementDAO.getAll().size)
            }
        }
    }
}