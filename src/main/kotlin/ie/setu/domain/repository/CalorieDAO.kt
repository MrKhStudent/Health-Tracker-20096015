package ie.setu.domain.repository

import ie.setu.domain.BodyMeasurement
import ie.setu.domain.Calorie
import ie.setu.domain.db.BodyMeasurements
import ie.setu.domain.db.Calories
import ie.setu.utils.mapToCalorie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction



class CalorieDAO {

    //------------------------------------------Get all the calories in the database
    fun getAll(): ArrayList<Calorie> {
        val caloriesList: ArrayList<Calorie> = arrayListOf()
        transaction {
            Calories.selectAll().map {
                caloriesList.add(mapToCalorie(it)) }
        }
        return caloriesList
    }

    //------------------------------------------Find a specific calorie by Calorie id
    fun findByCalorieId(id: Int): Calorie?{
        return transaction {
            Calories
                .select() { Calories.id eq id}
                .map{mapToCalorie(it)}
                .firstOrNull()
        }
    }

    //-------------------------------Find all Calories for a specific user id
    fun findByUserId(userId: Int): List<Calorie>{
        return transaction {
            Calories
                .select { Calories.userId eq userId}
                .map { mapToCalorie(it) }
        }
    }

    //---------------------------------------- Save Calories to the database
    fun save(calorie: Calorie): Int {
        return transaction {
            Calories.insert {
                it[breakfast] = calorie.breakfast
                it[lunch] = calorie.lunch
                it[dinner] = calorie.dinner
                it[snack] = calorie.snack
                it[userId] = calorie.userId
            }
        } get Calories.id
    }

    //----------------------------------------------------- update By Calorie Id
    fun updateByCalorieId(calorieId: Int, calorieToUpdate: Calorie): Int {
        return transaction {
            Calories.update({
                Calories.id eq calorieId
            }) {
                it[breakfast] = calorieToUpdate.breakfast
                it[lunch] = calorieToUpdate.lunch
                it[dinner] = calorieToUpdate.dinner
                it[snack] = calorieToUpdate.snack
                it[userId] = calorieToUpdate.userId
            }
        }
    }

    //------------------------------------------------------ deleteByCalorieId
    fun deleteByCalorieId(calorieId: Int): Int {
        return transaction {
            Calories.deleteWhere { Calories.id eq calorieId }
        }
    }

    //--------------------------------------------------------  deleteByUserId
    fun deleteByUserId(userId: Int): Int {
        return transaction {
            Calories.deleteWhere { Calories.userId eq userId }
        }
    }

}
