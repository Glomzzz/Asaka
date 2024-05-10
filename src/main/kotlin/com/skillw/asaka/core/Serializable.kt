package com.skillw.asaka.core

interface Serializable {
    fun serialize(): Map<String, Any>

    companion object {

        fun Any?.javaRaw() = when (this) {
            null -> "null"
            is String -> "\"" + this + "\""
            is Short -> "(short) $this"
            is Long -> toString() + "L"
            is Float -> toString() + "f"
            is Double -> toString() + "d"
            is Char -> "'$this'"
            else -> "$this"
        }
    }
}
