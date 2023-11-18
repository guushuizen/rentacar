package tech.guus.rentacarapi.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*


class CarPhoto(id: EntityID<UUID>): Entity<UUID>(id) {
    companion object : EntityClass<UUID, CarPhoto>(CarPhotos)

    var car by Car referencedOn CarPhotos.carUuid
    var path by CarPhotos.path
    var index by CarPhotos.index
}

object CarPhotos : UUIDTable() {
    val carUuid = reference("car_uuid", Cars)
    val path = varchar("path", 128)
    val index = integer("index")
}
