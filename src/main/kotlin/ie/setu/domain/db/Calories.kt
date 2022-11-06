package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object Calories : Table("calories") {
    val id = integer("id").autoIncrement().primaryKey()
    val breakfast = double("breakfast")
    val lunch = double("lunch")
    val dinner = double("dinner")
    val snack = double("snack")
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}