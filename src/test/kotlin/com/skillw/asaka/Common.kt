package com.skillw.asaka

import com.skillw.asaka.core.Span
import javax.script.ScriptException
import kotlin.test.Test


object Common {

    @Test
    fun test2() {
        Asaka.boot()
        val span = Span.of(0..3, 0, "test", "local")
        val module = Asaka.module("TestCommon", span) {
            clazz("Main") {
                defineMethod("main") {
                    body {
                        Define("a") assignTo lit("1")
                        Define("b") assignTo lit("2")
                        val a = variable("a") invoke "toInt"
                        val b = variable("b") invoke "toInt"
                        val println = { invoke("println") }
                        Block {
                            add(println() with arrayOf(a plus b))
                        }
                        addAll(
                            println() with arrayOf(
                                a minus b
                            ),
                            println() with arrayOf(
                                (_if(a gt lit(3)) then {
                                    add(lit("!  a > 3  !"))
                                } orElse {
                                    add(lit("!  a <= 3  !"))
                                }).asExpr()
                            ),
                            println() with arrayOf(
                                (_try {
                                    add(a div b)
                                } catch param("e", type(ScriptException::class.java)) then {
                                    addAll(
                                        println() with arrayOf(
                                            lit("catch ScriptException!") plus (variable("e") invoke "message")
                                        ),
                                        lit(0.0)
                                    )
                                } catch param("e", type(Exception::class.java)) then {
                                    addAll(
                                        println() with arrayOf(
                                            lit("catch Exception!") plus (variable("e") invoke "message")
                                        ),
                                        lit(0.0)
                                    )
                                } finally {
                                    add(println() with arrayOf(lit("finally!")))
                                }).asExpr()
                            )
                        )

                        Define("testIfExpr") assignTo (
                                _if(a gt lit(3)) then {
                                    add(lit("!  a > 3  !"))
                                } orElse {
                                    add(lit("!  a <= 3  !"))
                                }
                                ).asExpr()


                        While(a lt lit(10)) then {
                            add(println() with arrayOf(a))
                            add(variable("a") assignTo a plus lit(1))
                        }

//                        array(lit("a"), lit("b"), lit("c")) type type(String::class.java)
//
//                        array(lit(1), lit(2)) type type(A_INT.unboxType)
//
//                        array(lit(1), lit(2)) type type(A_INT.boxType)
                    }
                }
            }
        }.buildTarget()
    }
}