package ie.setu.domain.repository

import ie.setu.domain.Workout
import ie.setu.domain.db.Calories
import ie.setu.domain.db.Workouts
import ie.setu.utils.mapToWorkout
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class WorkoutDAO {

    //-------------------------------Get all the workouts in the database
    fun getAll(): ArrayList<Workout> {
        val workoutsList: ArrayList<Workout> = arrayListOf()
        transaction {
            Workouts.selectAll().map {
                workoutsList.add(mapToWorkout(it)) }
        }
        return workoutsList
    }


    //-------------------------------Find a specific workout by workout id
    fun findByWorkoutId(id: Int): Workout?{
        return transaction {
            Workouts
                .select() { Workouts.id eq id}
                .map{mapToWorkout(it)}
                .firstOrNull()
        }
    }

    //-------------------------------Find all Workouts for a specific user id
    fun findByUserId(userId: Int): List<Workout>{
        return transaction {
            Workouts
                .select { Workouts.userId eq userId}
                .map { mapToWorkout(it) }
        }
    }

    //---------------------------------------- Save Workouts to the database
    fun save(workout: Workout): Int {
        return transaction {
            Workouts.insert {
                it[description] = workout.description
                it[duration] = workout.duration
                it[numbers] = workout.numbers
                it[userId] = workout.userId
            }
        } get Workouts.id
    }

    //----------------------------------------------------- update By Workout Id
    fun updateByWorkoutId(workoutId: Int, workoutToUpdate: Workout): Int {
        return transaction {
            Workouts.update({
                Workouts.id eq workoutId
            }) {
                it[description] = workoutToUpdate.description
                it[duration] = workoutToUpdate.duration
                it[numbers] = workoutToUpdate.numbers
                it[userId] = workoutToUpdate.userId
            }
        }
    }

    //------------------------------------------------------ deleteByWorkoutId
    fun deleteByWorkoutId(workoutId: Int): Int {
        return transaction {
            Workouts.deleteWhere { Workouts.id eq workoutId }
        }
    }

    //--------------------------------------------------------  deleteByUserId
    fun deleteByUserId(userId: Int): Int {
        return transaction {
            Workouts.deleteWhere { Workouts.userId eq userId }
        }
    }

}

