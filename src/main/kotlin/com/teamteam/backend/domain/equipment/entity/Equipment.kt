package com.teamteam.backend.domain.equipment.entity

import jakarta.persistence.*

@Entity
@Table(name = "equipment")
class Equipment(
    @Id
    var id : String,
    @Enumerated(EnumType.STRING)
    var type : EquipmentType,
    @Column(name = "room_id")
    var roomId : String
)