package ie.setu.repository

import ie.setu.domain.Workout
import ie.setu.domain.db.Workouts
import ie.setu.utils.mapToWorkout
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ie.setu.domain.repository.WorkoutDAO
import ie.setu.helpers.workouts
import ie.setu.helpers.populateWorkoutTable
import ie.setu.helpers.populateUserTable
import kotlin.test.assertEquals


//retrieving some test data from Fixtures
private val workout1 = workouts.get(0)
private val workout2 = workouts.get(1)
private val workout3 = workouts.get(2)

class WorkoutDAOTest {

    companion object {
        //Make a connection to a local, in memory H2 database.
        @BeforeAll
        @JvmStatic
        internal fun setupInMemoryDatabaseConnection() {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
        }
    }

    @Nested
    inner class CreateWorkouts {

        @Test
        fun `multiple workouts added to table can be retrieved successfully`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
                assertEquals(workout1, workoutDAO.findByWorkoutId(workout1.id))
                assertEquals(workout2, workoutDAO.findByWorkoutId(workout2.id))
                assertEquals(workout3, workoutDAO.findByWorkoutId(workout3.id))
            }
        }
    }

    @Nested
    inner class ReadWorkouts {

        @Test
        fun `getting all workouts from a populated table returns all rows`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
            }
        }

        @Test
        fun `get workout by user id that has no workouts, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(0, workoutDAO.findByUserId(3).size)
            }
        }

        @Test
        fun `get workout by user id that exists, results in a correct workout(s) returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(workout1, workoutDAO.findByUserId(1).get(0))
                assertEquals(workout2, workoutDAO.findByUserId(1).get(1))
                assertEquals(workout3, workoutDAO.findByUserId(2).get(0))
            }
        }

        @Test
        fun `get all workouts over empty table returns none`() {
            transaction {

                //Arrange - create and setup workoutDAO object
                SchemaUtils.create(Workouts)
                val workoutDAO = WorkoutDAO()

                //Act & Assert
                assertEquals(0, workoutDAO.getAll().size)
            }
        }

        @Test
        fun `get workout by workout id that has no records, results in no record returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(null, workoutDAO.findByWorkoutId(4))
            }
        }

        @Test
        fun `get workout by workout id that exists, results in a correct workout returned`() {
            transaction {
                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()
                //Act & Assert
                assertEquals(workout1, workoutDAO.findByWorkoutId(1))
                assertEquals(workout3, workoutDAO.findByWorkoutId(3))
            }
        }
    }

    @Nested
    inner class UpdateWorkouts{

        @Test
        fun `updating existing workout in table results in successful update`() {
            transaction {

                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()


                //Act & Assert
                val workout3updated = Workout(id = 3, description = "abdominal",
                    duration = 90.2, numbers = 59, userId = 3)
                workoutDAO.updateByWorkoutId(workout3updated.id,  workout3updated)
                assertEquals(workout3updated, workoutDAO.findByWorkoutId(3))
            }

        }

        @Test
        fun `updating non-existant  workout in table results in no updates`() {
            transaction {

                //Arrange - create and populate tables with three users and three  workouts
                val userDAO = populateUserTable()
                val  workoutDAO = populateWorkoutTable()

                //Act & Assert
                val  workout4updated =  Workout(id = 3, description = "abdominal",
                    duration = 90.2, numbers = 59, userId = 3)
                workoutDAO.updateByWorkoutId(4, workout4updated)
                assertEquals(null, workoutDAO.findByWorkoutId(4))
                assertEquals(3, workoutDAO.getAll().size)
            }
        }
    }

    @Nested
    inner class DeleteWorkouts {

        @Test
        fun `deleting a non-existant workout (by id) in table results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()

                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
                workoutDAO.deleteByWorkoutId(4)
                assertEquals(3, workoutDAO.getAll().size)
            }
        }

        @Test
        fun `deleting an existing workout (by id) in table results in record being deleted`() {
            transaction {

                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()

                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
                workoutDAO.deleteByWorkoutId(workout3.id)
                assertEquals(2, workoutDAO.getAll().size)
            }
        }


        @Test
        fun `deleting workouts when none exist for user id results in no deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()

                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
                workoutDAO.deleteByUserId(3)
                assertEquals(3, workoutDAO.getAll().size)
            }
        }

        @Test
        fun `deleting workouts when 1 or more exist for user id results in deletion`() {
            transaction {

                //Arrange - create and populate tables with three users and three workouts
                val userDAO = populateUserTable()
                val workoutDAO = populateWorkoutTable()

                //Act & Assert
                assertEquals(3, workoutDAO.getAll().size)
                workoutDAO.deleteByUserId(1)
                assertEquals(1, workoutDAO.getAll().size)
            }
        }
    }
}