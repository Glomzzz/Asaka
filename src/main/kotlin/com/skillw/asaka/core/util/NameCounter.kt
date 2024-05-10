package com.skillw.asaka.core.util

interface NameCounter {
    fun getName(name: String): String
    fun getNext(name: String): String
}