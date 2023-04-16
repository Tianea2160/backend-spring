package com.teamteam.backend.domain.building.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "building")
class Building(
    @Id
    var id: String,
    @Column(name = "admin_id")
    var adminId : String,
    var name: String,
    var location: String,
    var description: String,
    var imageUrl: String
) {
    override fun toString(): String =
        "Building(id=$id, name='$name', location='$location', description='$description', imageUrl='$imageUrl')"
}