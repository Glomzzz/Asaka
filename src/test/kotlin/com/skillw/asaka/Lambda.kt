package com.skillw.asaka

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.A_INT
import kotlin.test.Test

object Lambda {
    @Test
    fun test3() {

        Asaka.boot()
        val source = Span.of(0..4, 0, "local", "/")

        val module = Asaka.module("TestLambda", Span.EMPTY) {
            importInvokeAs("println") {
                type(System::class.java) field "out" invoke "println"
            }
            clazz("test3") {
                defineMethod("test") {
                    modifiers { static() }
                    params {
                        name("lambda") type lambdaType(type(A_INT), returnType = type(A_INT))
                    }
                    returnType(type(A_INT))
                    body {
                    }
                }
                clinit {
                    Invoke("test") with arrayOf(
                        lambda("x") then {
                            variable("x") plus lit(1)
                        }
                    )
                }
            }
        }.buildTarget()
        module.print()
        Asaka.passer.pass(module)
        module.print()
    }
}