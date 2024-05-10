package com.skillw.asaka.core.pass

import com.skillw.asaka.core.ir.ast.ClassBlock
import com.skillw.asaka.core.ir.ast.MethodBlock
import com.skillw.asaka.core.ir.ast.Node
import com.skillw.asaka.core.util.NameCounter


abstract class AsakaPass(val key: String, vararg depends: AsakaPass) {
    val depends: HashSet<AsakaPass> = depends.toHashSet()

    init {
        val toAdd = HashSet<AsakaPass>()
        this.depends.forEach { toAdd.addAll(it.depends) }
        this.depends.addAll(toAdd)
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as AsakaPass
        return key == other.key
    }
}

interface NodePasser<N : Node> : NodeReplacer<N> {
    fun N.pass()

    override fun N.replace(context: PassContext): Node {
        pass()
        return this
    }
}

interface NodeReplacer<N> {
    val target: Class<N>
    fun N.replace(context: PassContext): Node
}

interface Prologue {
    fun ClassBlock.prologue()
}

interface Epilogue {
    fun ClassBlock.epilogue()
}

interface PassContext : MethodBlock, NameCounter {
    val counter: NameCounter
    fun before(before: MethodBlock.() -> Unit)
    fun after(after: MethodBlock.() -> Unit)
    fun executeBefores()
    fun executeAfters()
}