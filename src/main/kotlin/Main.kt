package com.skillw.asaka

import com.google.gson.GsonBuilder
import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.A_INT

val gson = GsonBuilder().disableHtmlEscaping().create()
fun Serializable.print() {
    println(gson.toJson(serialize()))
}

fun main() {
    Asaka.boot()
    val span = Span.of(0..3, 0, "test", "local")
    val module = Asaka.module("Test", span) {
        importInvokeAs("println") {
            type(System::class.java) field "out" invoke "println"
        }
        clazz("TestAsakaClass") {
            defineField("a") {
                type(type(A_INT))
            }
            constructor {
                params {
                    name("a") type type(A_INT)
                }

                body {
                    add(
                        self() field ("a") assignTo variable("a")
                    )
                }
            }
            clinit {
                add(
                    invoke("println") with arrayOf(
                        lit("Hello, Asaka!")
                    )
                )
            }
            defineMethod("test") {
                body {
                    Define("a") assignTo (
                            _if(field("a") eq lit(1)) then {
                                Lit("a = 1")
                            } orElse {
                                Lit("a != 1")
                            }).asExpr()

                }
            }
        }
    }.buildTarget()


    module.print()
    Asaka.passer.pass(module)
    module.print()
}