package com.skillw.asaka

/**
 * @className Test
 * @author Glom
 * @date 2024/2/6 17:46
 * Copyright 2024 @Glom.
 */

abstract class A

enum class E(val a: Int, val b: Int = 2, val c: Int) {
    A(1, 2, 3),
    B(1, b = 2, 3) {
        fun b() {
            println("a")
        }
    },
}


fun main() {

    val a = intArrayOf(1, 2, 3, 4)

    val b = fun(a: IntArray) {
        for (i in a) {
            println(i)
        }
    }
    b(a)
}