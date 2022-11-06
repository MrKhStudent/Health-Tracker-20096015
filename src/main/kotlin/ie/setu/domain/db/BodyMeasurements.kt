package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BodyMeasurements : Table("bodymeasurements") {

    val id = integer("id").autoIncrement().primaryKey()
    val weight = double("weight")
    val height = double("height")
    val waist = double("waist")
    val chest = double("chest")
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}