package com.skillw.asaka.core.ir.ast


val ASSIGN = BinaryOperator.ASSIGN
val AS = BinaryOperator.AS

val ADD = BinaryOperator.ADD
val SUB = BinaryOperator.SUB
val MUL = BinaryOperator.MUL
val DIV = BinaryOperator.DIV
val POW = BinaryOperator.POW
val MOD = BinaryOperator.MOD

val BIT_AND = BinaryOperator.BIT_AND
val BIT_OR = BinaryOperator.BIT_OR
val BIT_XOR = BinaryOperator.BIT_XOR
val BIT_NOT = BinaryOperator.BIT_NOT

val BIT_SHL = BinaryOperator.BIT_SHL
val BIT_SHR = BinaryOperator.BIT_SHR
val BIT_USHR = BinaryOperator.BIT_USHR

val IS = Comparison.IS
val AND = Comparison.AND
val OR = Comparison.OR

val REF_EQ = Comparison.REF_EQ
val REF_NE = Comparison.REF_NE

val EQ = Comparison.EQ
val NE = Comparison.NE
val GT = Comparison.GT
val LT = Comparison.LT
val GE = Comparison.GE
val LE = Comparison.LE

val NOT = UnaryOperator.NOT
val PRE_INC = UnaryOperator.PRE_INC
val PRE_DEC = UnaryOperator.PRE_DEC
val POST_INC = UnaryOperator.POST_INC
val POST_DEC = UnaryOperator.POST_DEC
val POS = UnaryOperator.POS
val NEG = UnaryOperator.NEG