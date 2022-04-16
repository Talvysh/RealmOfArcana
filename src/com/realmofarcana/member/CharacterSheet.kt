package com.realmofarcana.member

class CharacterSheet(var member: Member) {
    lateinit var name: String
    lateinit var bio: String

    init {
        name = member.username
    }
}