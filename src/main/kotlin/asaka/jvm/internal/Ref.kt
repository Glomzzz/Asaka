package com.skillw.asaka.asaka.jvm.internal

import java.io.Serializable

class Ref private constructor() {
    class ObjectRef(@JvmField var value: Any? = null) : Serializable {
        override fun toString(): String {
            return value.toString()
        }
    }

    class ByteRef(@JvmField var value: Byte = 0) : Serializable {
        override fun toString(): String {
            return value.toString()
        }
    }

    class ShortRef(@JvmField var value: Short = 0) : Serializable {

        override fun toString(): String {
            return value.toString()
        }
    }

    class IntRef(@JvmField var value: Int = 0) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }

    class LongRef(@JvmField var value: Long = 0) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }

    class FloatRef(@JvmField var value: Float = 0f) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }

    class DoubleRef(@JvmField var value: Double = 0.0) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }

    class CharRef(@JvmField var value: Char = 0.toChar()) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }

    class BooleanRef(@JvmField var value: Boolean = false) : Serializable {


        override fun toString(): String {
            return value.toString()
        }
    }
}