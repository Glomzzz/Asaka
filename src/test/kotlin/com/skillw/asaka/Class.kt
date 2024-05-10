package com.skillw.asaka

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.EQ
import com.skillw.asaka.core.ir.type.A_INT
import kotlin.test.Test


object Class {
    @Test
    fun test() {
        Asaka.boot()
        val span = Span.of(0..3, 0, "test", "local")
        Asaka.module("TestClass", span) {
            clazz("TestAsakaClass") {
                defineField("a") {
                    type(A_INT)
                }
                constructor {
                    params {
                        name("a") type type(A_INT)
                    }
                    // superInit(args)
                    // thisInit(args)
                    body {

                        // <init>
                        add(
                            self() field ("a") assignTo variable("a")
                        )
                    }
                }
                clinit {
                    // <clinit>
                    add(
                        invoke("println") with arrayOf(
                            lit("Hello, Asaka!")
                        )
                    )
                }
                defineMethod("test") {
                    body {
                        Define("a") assignTo (
                                lit("Hello, Asaka! from TestAsakaClass#test")
                                )
                        When(variable("a")) {
                            case(EQ, lit("Hello, Asaka! from TestAsakaClass#test")) {
                                add(
                                    invoke("println") with arrayOf(
                                        lit("Hello, Asaka! from TestAsakaClass#test")
                                    )
                                )
                            }
                        }
                    }
                }
                defineMethod("testStatic") {
                    modifiers {
                        static()
                    }
                    body {
                        Define("a") assignTo (
                                lit("Hello, Asaka! from TestAsakaClass.test")
                                )
                    }
                }
            }
        }.buildTarget().print()
    }
}