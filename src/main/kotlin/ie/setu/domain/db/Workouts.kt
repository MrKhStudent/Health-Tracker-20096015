package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object Workouts : Table("workouts") {
    val id = integer("id").autoIncrement().primaryKey()
    val description = varchar("description", 100)
    val duration = double("duration")
    val numbers = integer("numbers")
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}