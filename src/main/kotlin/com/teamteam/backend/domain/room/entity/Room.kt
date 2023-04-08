package com.teamteam.backend.domain.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "room")
class Room(
    @Id
    var id: String? = null,
    @Column(name = "building_id")
    var buildingId: String,
    var name: String,
    var description: String,
    var capacity: Long,
) {
    fun update(name: String = this.name, description: String = this.description, capacity: Long = this.capacity) {
        this.name = name
        this.description = description
        this.capacity = capacity
    }
}