package ie.setu.repository

import ie.setu.domain.Calorie
import ie.setu.domain.db.Calories
import ie.setu.utils.mapToCalorie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ie.setu.domain.repository.CalorieDAO
import ie.setu.helpers.calories
import ie.setu.helpers.populateCalorieTable
import ie.setu.helpers.populateUserTable
import kotlin.test.assertEquals


//retrieving some test data from Fixtures
private val calorie1 = calories.get(0)
private val calorie2 = calories.get(1)
private val calorie3 = calories.get(2)

class CalorieDAOTest {

    companion object {
        //Make a connection to a local, in memory H2 database.
        @BeforeAll
        @JvmStatic
        internal fun setupInMemoryDatabaseConnection() {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
        }
    }

    @Nested
    inner class CreateCalories {

        @Test
        fun `multiple calories added to table can be retrieved successfully`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
                assertEquals(calorie1, calorieDAO.findByCalorieId(calorie1.id))
                assertEquals(calorie2, calorieDAO.findByCalorieId(calorie2.id))
                assertEquals(calorie3, calorieDAO.findByCalorieId(calorie3.id))
            }
        }
    }

    @Nested
    inner class ReadCalories {

        @Test
        fun `getting all calories from a populated table returns all rows`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
            }
        }

        @Test
        fun `get calorie by user id that has no calories, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(0, calorieDAO.findByUserId(3).size)
            }
        }

        @Test
        fun `get calorie by user id that exists, results in a correct calorie(s) returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(calorie1, calorieDAO.findByUserId(1).get(0))
                assertEquals(calorie2, calorieDAO.findByUserId(1).get(1))
                assertEquals(calorie3, calorieDAO.findByUserId(2).get(0))
            }
        }

        @Test
        fun `get all calories over empty table returns none`() {
            transaction {

                //Arrange - create and setup calorieDAO object
                SchemaUtils.create(Calories)
                val calorieDAO = CalorieDAO()

                //Act & Assert
                assertEquals(0, calorieDAO.getAll().size)
            }
        }

        @Test
        fun `get calorie by calorie id that has no records, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(null, calorieDAO.findByCalorieId(4))
            }
        }

        @Test
        fun `get calorie by calorie id that exists, results in a correct calorie returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()
                //Act & Assert
                assertEquals(calorie1, calorieDAO.findByCalorieId(1))
                assertEquals(calorie3, calorieDAO.findByCalorieId(3))
            }
        }
    }

    @Nested
    inner class UpdateCalories {

        @Test
        fun `updating existing calorie in table results in successful update`() {
            transaction {

                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()

                //Act & Assert
                val calorie3updated = Calorie(id = 3, breakfast = 487.5, lunch = 1098.8,
                    dinner = 800.9, snack = 90.0, userId = 3)
                calorieDAO.updateByCalorieId(calorie3updated.id,  calorie3updated)
                assertEquals(calorie3updated, calorieDAO.findByCalorieId(3))
            }
        }

        @Test
        fun `updating non-existant  calorie in table results in no updates`() {
            transaction {

                //Arrange - create and populate tables with three users and three  calories
                val userDAO = populateUserTable()
                val  calorieDAO = populateCalorieTable()

                //Act & Assert
                val  calorie4updated =  Calorie(id = 3, breakfast = 487.5, lunch = 1098.8,
                    dinner = 800.9, snack = 90.0, userId = 3)
                calorieDAO.updateByCalorieId(4, calorie4updated)
                assertEquals(null, calorieDAO.findByCalorieId(4))
                assertEquals(3, calorieDAO.getAll().size)
            }
        }
    }

    @Nested
    inner class DeleteCalories {

        @Test
        fun `deleting a non-existant calorie (by id) in table results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()

                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
                calorieDAO.deleteByCalorieId(4)
                assertEquals(3, calorieDAO.getAll().size)
            }
        }

        @Test
        fun `deleting an existing calorie (by id) in table results in record being deleted`() {
            transaction {

                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()

                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
                calorieDAO.deleteByCalorieId(calorie3.id)
                assertEquals(2, calorieDAO.getAll().size)
            }
        }


        @Test
        fun `deleting calories when none exist for user id results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()

                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
                calorieDAO.deleteByUserId(3)
                assertEquals(3, calorieDAO.getAll().size)
            }
        }

        @Test
        fun `deleting calories when 1 or more exist for user id results in deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three calories
                val userDAO = populateUserTable()
                val calorieDAO = populateCalorieTable()

                //Act & Assert
                assertEquals(3, calorieDAO.getAll().size)
                calorieDAO.deleteByUserId(1)
                assertEquals(1, calorieDAO.getAll().size)
            }
        }
    }
}