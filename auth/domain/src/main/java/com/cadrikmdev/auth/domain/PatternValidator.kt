package com.cadrikmdev.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}