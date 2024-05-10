package com.skillw.asaka.core.ir.ast

sealed class BlockNodeKind(open val body: MethodBlock) : Node(source = body.label.source), MethodBlock by body

open class BlockNode(override val body: MethodBlock) : BlockNodeKind(body) {
    override fun serialize() = body.serialize()

    override fun clone(blc: MethodBlock): BlockNode {
        return super.clone(blc) as BlockNode
    }

    override fun cloneNode(blc: MethodBlock): BlockNode {
        val block = body.clazz().child(blc.label)
        block.inline = body.inline
        block.context = body.context.clone(block)
        body.forEach { block.add(it.clone(block)) }
        return BlockNode(block)
    }
}

class LambdaBlockNode(override val body: LambdaBlock) : BlockNodeKind(body) {
    val params = body.params
    val returnType = body.returnType
    val usedVars = body.usedVars
    override fun serialize() = body.serialize()

    override fun clone(blc: MethodBlock): LambdaBlockNode {
        return super.clone(blc) as LambdaBlockNode
    }

    override fun cloneNode(blc: MethodBlock): LambdaBlockNode {
        val block = body.method().lambda(blc.label, body.params, body.returnType)
        block.inline = body.inline
        block.context = body.context.clone(block)
        body.forEach { block.add(it.clone(block)) }
        return LambdaBlockNode(block)
    }
}

class LoopBlockNode(override val body: LoopBlock) : BlockNodeKind(body) {
    override fun serialize() = body.serialize()

    override fun clone(blc: MethodBlock): LoopBlockNode {
        return super.clone(blc) as LoopBlockNode
    }

    override fun cloneNode(blc: MethodBlock): LoopBlockNode {
        val block = body.method().loop(blc.label)
        block.inline = body.inline
        block.context = body.context.clone(block)
        body.forEach { block.add(it.clone(block)) }
        return LoopBlockNode(block)
    }
}