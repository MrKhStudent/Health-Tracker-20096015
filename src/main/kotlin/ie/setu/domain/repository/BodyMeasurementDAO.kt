package ie.setu.domain.repository

import ie.setu.domain.BodyMeasurement
import ie.setu.domain.db.BodyMeasurements
import ie.setu.utils.mapToBodyMeasurement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


class BodyMeasurementDAO {

    //--------------------------------- Get all the bodyMeasurements in the database
    fun getAll(): ArrayList<BodyMeasurement> {
        val bodyMeasurementsList: ArrayList<BodyMeasurement> = arrayListOf()
        transaction {
            BodyMeasurements.selectAll().map {
                bodyMeasurementsList.add(mapToBodyMeasurement(it))
            }
        }
        return bodyMeasurementsList
    }

    //--------------------------Find a specific bodyMeasurement by bodyMeasurement id
    fun findByBodyMeasurementId(id: Int): BodyMeasurement? {
        return transaction {
            BodyMeasurements
                .select() { BodyMeasurements.id eq id }
                .map { mapToBodyMeasurement(it) }
                .firstOrNull()
        }
    }

    //-------------------------------Find all BodyMeasurements for a specific user id
    fun findByUserId(userId: Int): List<BodyMeasurement> {
        return transaction {
            BodyMeasurements
                .select { BodyMeasurements.userId eq userId }
                .map { mapToBodyMeasurement(it) }
        }
    }


    //-------------------------------------- Save a BodyMeasurement to the database
    fun save(bodyMeasurement: BodyMeasurement): Int {
        return transaction {
            BodyMeasurements.insert {
                it[weight] = bodyMeasurement.weight
                it[height] = bodyMeasurement.height
                it[waist] = bodyMeasurement.waist
                it[chest] = bodyMeasurement.chest
                it[userId] = bodyMeasurement.userId
            }
        } get BodyMeasurements.id
    }

    //----------------------------------------------------- update By BodyMeasurement Id
    fun updateByBodyMeasurementId(bodyMeasurementId: Int, bodyMeasurementToUpdate: BodyMeasurement): Int {
        return transaction {
            BodyMeasurements.update({
                BodyMeasurements.id eq bodyMeasurementId
            }) {
                it[weight] = bodyMeasurementToUpdate.weight
                it[height] = bodyMeasurementToUpdate.height
                it[waist] = bodyMeasurementToUpdate.waist
                it[chest] = bodyMeasurementToUpdate.chest
                it[userId] = bodyMeasurementToUpdate.userId
            }
        }
    }

    //------------------------------------------------------ deleteByBodyMeasurementId
    fun deleteByBodyMeasurementId(bodyMeasurementId: Int): Int {
        return transaction {
            BodyMeasurements.deleteWhere { BodyMeasurements.id eq bodyMeasurementId }
        }
    }

    //--------------------------------------------------------  deleteByUserId
    fun deleteByUserId(userId: Int): Int {
        return transaction {
            BodyMeasurements.deleteWhere { BodyMeasurements.userId eq userId }
        }
    }

}