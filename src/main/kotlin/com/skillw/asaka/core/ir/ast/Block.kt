package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.Asaka.creator
import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.data.BlockContext
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.member.AsahiVariable
import com.skillw.asaka.core.ir.type.AsakaClass
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.Undefined
import com.skillw.asaka.util.unsafeLazy

interface Block : Serializable, AsakaRepresent {
    val label: Identifier
    val path: String
}

interface ModuleBlock : Block {
    fun module(): ModuleBlock
    fun classes(): List<ClassDefinition>
    fun addClass(define: ClassDefinition)
    fun clazz(label: Identifier, clazz: AsakaClass): ClassBlock
}

interface ClassBlock : ModuleBlock {
    val module: ModuleBlock
    var context: BlockContext

    var init: MethodBlock
    fun self(): AsakaClass
    fun clazz(): ClassBlock
    fun child(label: Identifier): MethodBlock
    fun fields(): List<FieldDefinition>
    fun methods(): List<MethodDefinition>
    fun addField(define: FieldDefinition)
    fun addMethod(define: MethodDefinition)

    fun variable(call: VarCallExpression): AsahiVariable?

    fun variable(def: VarDefineStatement): AsahiVariable?

    var passing: Boolean
    fun <T> data(key: String): T
    fun <T : Any> data(key: String, default: () -> T): T
}

interface MethodBlock : ClassBlock, TypeInferable, MutableList<Node> {
    val clazz: ClassBlock
    var inline: Boolean
    fun addVariable(stm: VarDefineStatement)
    fun addVariable(param: AsahiParameter)
    fun addVariable(variable: AsahiVariable)
    fun nextCountedName(): String
    fun method(): MethodBlock
    fun hasLabel(label: String): Boolean
    fun loop(): LoopBlock
    fun loop(label: Identifier): LoopBlock
    fun randomLabel(source: Span): Identifier
    override fun child(label: Identifier): SubMethodBlock
    fun lambda(label: Identifier, params: List<AsahiParameter>, returnType: TypeInst): LambdaBlock
}


interface SubMethodBlock : MethodBlock {
    val parent: MethodBlock
}

interface LoopBlock : SubMethodBlock

interface LambdaBlock : SubMethodBlock {
    val params: List<AsahiParameter>
    val returnType: TypeInst
    val usedVars: MutableSet<AsahiVariable>
}

private abstract class BlockImpl(override val label: Identifier) : Block
private open class ModuleBlockImpl(label: Identifier) : BlockImpl(label), ModuleBlock {
    override val path: String
        get() = label.name

    override fun module() = this

    private val classes = mutableListOf<ClassDefinition>()

    override fun classes() = classes

    override fun addClass(define: ClassDefinition) {
        classes.add(define)
    }

    override fun clazz(label: Identifier, clazz: AsakaClass): ClassBlock {
        return ClassBlockImpl(label, this, clazz).also { clazz.body = it }
    }

    override fun serialize() = linkedMapOf(
        "module" to label.name,
        "classes" to classes.map { it.serialize() }
    )
}

private class ClassBlockImpl(
    override val label: Identifier,
    override val module: ModuleBlock,
    val clazz: AsakaClass
) :
    ModuleBlock by module,
    ClassBlock {
    override var context = BlockContext(label.name)
    override val path: String
        get() = module.path + "." + label.name

    override var passing = false
    private val metadata = HashMap<String, Any>()

    override var init: MethodBlock = child(Identifier("init", label.source))

    override fun <T> data(key: String): T {
        return metadata[key] as T
    }

    override fun <T : Any> data(key: String, default: () -> T): T {
        return metadata.getOrPut(key, default) as T
    }

    override fun self() = clazz
    override fun clazz() = this
    override fun child(label: Identifier): MethodBlock {
        return MethodBlockImpl(label, this)
    }

    private val fields = mutableListOf<FieldDefinition>()
    private val methods = mutableListOf<MethodDefinition>()


    override fun fields() = fields
    override fun addField(define: FieldDefinition) {
        define.block = this
        fields.add(define)
        define.value?.let {
            init.add(
                creator.binary(
                    ASSIGN,
                    creator.callVar(define.name, define.getType(), define.source),
                    it,
                    define.source
                )
            )
        }
        context.variables[define.name] = define.toMember()
    }

    override fun methods() = methods

    override fun addMethod(define: MethodDefinition) {
        methods.add(define)
        context.methods[define.toKey()] = define.toMember()
    }


    override fun variable(call: VarCallExpression): AsahiVariable? {
        return context.variables[call.name]
    }

    override fun variable(def: VarDefineStatement): AsahiVariable? {
        return context.variables[def.name]
    }

    override fun serialize() = linkedMapOf(
        "class-body" to label.name,
        "fields" to fields.map { it.serialize() },
        "methods" to methods.map { it.serialize() }
    )
}

private open class MethodBlockImpl(
    final override val label: Identifier,
    override val clazz: ClassBlock,
    private val nodes: MutableList<Node> = ArrayList()
) : ClassBlock by clazz,
    MethodBlock, MutableList<Node> by nodes {
    override var inline: Boolean = false
    override var context = BlockContext(label.name)
    override fun variable(call: VarCallExpression): AsahiVariable? {
        return context.variables[call.name] ?: clazz.variable(call)
    }

    override fun variable(def: VarDefineStatement): AsahiVariable? {
        return context.variables[def.name] ?: clazz.variable(def)
    }

    override fun add(element: Node): Boolean {
        element.block = this
        return nodes.add(element)
    }

    override val path: String
        get() = "${clazz.path}.${label.name}"

    override fun addVariable(stm: VarDefineStatement) {
        context.variables[stm.name] = stm.toMember()
    }


    override fun addVariable(variable: AsahiVariable) {
        context.variables[variable.name] = variable
    }

    override fun addVariable(param: AsahiParameter) {
        context.variables[param.name] = param.toVar(this)
    }

    override fun method() = if (this is SubMethodBlock) parent.method() else this

    override fun loop(): LoopBlock {
        Err.syntax("Not in a loop", label.source)
    }

    override fun randomLabel(source: Span): Identifier {
        return Identifier(nextCountedName(), source)
    }

    override fun hasLabel(label: String): Boolean {
        return this.label.name == label
    }

    private var count = 0
    override fun nextCountedName(): String {
        return "${label.name}\$count\$${count++}"
    }

    private val TypeInst by unsafeLazy { Undefined.toInst(label.source) }
    override fun getType() = TypeInst

    override fun serialize() = linkedMapOf(
        "method" to label.name,
        "context" to context.variables.mapValues { it.value.serialize() },
        "nodes" to nodes.map { it.serialize() }
    )

    override fun child(label: Identifier): SubMethodBlock {
        return SubMethodBlockImpl(label, this)
    }

    override fun loop(label: Identifier): LoopBlock {
        return LoopBlockImpl(label, this)
    }

    override fun lambda(label: Identifier, params: List<AsahiParameter>, returnType: TypeInst): LambdaBlock {
        return LambdaBlockImpl(label, this, params, returnType)
    }


}

private open class SubMethodBlockImpl(label: Identifier, override val parent: MethodBlock) :
    MethodBlockImpl(label, parent.clazz),
    SubMethodBlock {
    override fun variable(call: VarCallExpression): AsahiVariable? {
        return context.variables[call.name] ?: parent.variable(call)
    }

    override fun variable(def: VarDefineStatement): AsahiVariable? {
        return context.variables[def.name] ?: parent.variable(def)
    }

    override fun hasLabel(label: String): Boolean {
        return this.label.name == label || parent.hasLabel(label)
    }

    override fun loop(): LoopBlock {
        return this as? LoopBlock ?: parent.loop()
    }


}


private class LambdaBlockImpl(
    label: Identifier,
    parent: MethodBlock,
    override val params: List<AsahiParameter>,
    override val returnType: TypeInst,
) : SubMethodBlockImpl(label, parent), LambdaBlock {
    override val usedVars: MutableSet<AsahiVariable> = HashSet()
    override var inline: Boolean = false

}

private open class LoopBlockImpl(label: Identifier, override val parent: MethodBlock) :
    SubMethodBlockImpl(label, parent), LoopBlock

fun module(label: Identifier): ModuleBlock = ModuleBlockImpl(label)