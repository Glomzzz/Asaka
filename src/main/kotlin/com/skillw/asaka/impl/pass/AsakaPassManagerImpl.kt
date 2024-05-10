package com.skillw.asaka.impl.pass

import com.skillw.asaka.Asaka.creator
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.RefSite
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMethodRef
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.member.AsahiVariable
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.ir.type.MixedType.Companion.mix
import com.skillw.asaka.core.pass.*
import com.skillw.asaka.core.util.NameCounter
import com.skillw.asaka.impl.util.getClasses
import com.skillw.asaka.impl.util.instance
import java.util.*

class AsakaPassManagerImpl : AsakaPassManager {
    private val prologues = PassContainer(Prologue::class.java)
    private val nodeReplacer = PassContainer(NodeReplacer::class.java)
    private val epilogues = PassContainer(Epilogue::class.java)


    private class NameCounterImpl : NameCounter {

        private val nameCounter = HashMap<String, Int>()

        override fun getName(name: String): String {
            return name + "$${nameCounter.getOrDefault(name, 0) - 1}"
        }

        override fun getNext(name: String): String {
            val count = nameCounter.getOrDefault(name, 0)
            nameCounter[name] = count + 1
            return "$name$$count"
        }
    }

    private inner class PassContextImpl(val block: MethodBlock, override val counter: NameCounter) :
        MethodBlock by block,
        PassContext, NameCounter by counter {
        override fun addVariable(stm: VarDefineStatement) {
            val name = getNext(stm.name)
            stm.name = name
            context.variables[name] = stm.toMember()
        }

        override fun addVariable(param: AsahiParameter) {
            addVariable(param.toVar(this))
        }

        override fun addVariable(variable: AsahiVariable) {
            val name = getNext(variable.name)
            variable.name = name
            context.variables[name] = variable
        }

        override fun variable(call: VarCallExpression): AsahiVariable? {
            val origin = call.name
            var name = origin
            var variable = block.variable(call)
            if (variable == null) {
                name = getName(name)
                call.name = name
                variable = block.variable(call)
            }
            if (variable == null) {
                name = call.name
                call.name = name
                variable = clazz.variable(call)
            }
            if (variable == null)
                call.name = origin
            return variable
        }


        private val befores = Stack<MethodBlock.() -> Unit>()
        private val afters = Stack<MethodBlock.() -> Unit>()

        override fun before(before: MethodBlock.() -> Unit) {
            befores.add(before)
        }

        override fun after(after: MethodBlock.() -> Unit) {
            afters.add(after)
        }

        override fun executeBefores() {
            while (befores.isNotEmpty()) {
                befores.pop().invoke(this)
            }
        }

        override fun executeAfters() {
            while (afters.isNotEmpty()) {
                afters.pop().invoke(this)
            }
        }
    }

    private fun <B : MethodBlock> MethodBlock.passMapTo(
        new: B,
        counter: NameCounter,
        init: PassContext.() -> Unit = {}
    ): B {
        val context = PassContextImpl(new, counter).apply(init)
        context.executeBefores()
        forEach {
            val final = it.passTopDown(new, context)
            context.executeBefores()
            new.add(final)
            context.executeAfters()
        }
        if (new.getType().unknown())
            new.getType().completeWith(new.inferType())
        return new
    }

    private fun LambdaBlockNode.passMapTo(context: PassContext): LambdaBlockNode {
        return LambdaBlockNode(body.passMapTo(context.lambda(label, params, returnType), context.counter) {
            params.forEach(this::addVariable)
        })
    }

    private fun LoopBlockNode.passMapTo(context: PassContext): LoopBlockNode {
        return LoopBlockNode(body.passMapTo(context.loop(label), context.counter))
    }

    private fun BlockNode.passMapTo(context: PassContext): BlockNode {
        return BlockNode(body.passMapTo(context.child(label), context.counter))
    }

    private fun Definition<*>.passTopDown() {
        when (this) {
            is FieldDefinition -> {
                if (getType().unknown() && value == null)
                    Err.type("Field $name without specific type must must be initialized", source)
                value?.expect(getType())
            }

            is MethodDefinition -> {
                body = body.passMapTo(body.clazz.child(body.label), NameCounterImpl()) {
                    params.forEach(this::addVariable)
                }
                body.clazz.context.methods[key]?.body = body
                if (getType().unknown()) {
                    getType().completeWith(body.getType())
                }
            }

            is ClassDefinition -> {
                clazz.body.apply {
                    pass(this)
                }
            }
        }
    }

    override fun pass(module: ModuleBlock) {
        module.apply {
            classes().forEach { it.passTopDown() }
        }
    }

    override fun pass(node: ClassBlock) {
        node.apply {
            passing = true
            init = init.passMapTo(node.child(init.label), NameCounterImpl())
            prologues.onEach { prologue() }
            fields().forEach { it.passTopDown() }
            methods().forEach { it.passTopDown() }
            epilogues.onEach { epilogue() }
            passing = false
        }
    }

    private fun Node.pass(context: PassContext): Node {
        if (passed) return this
        passed = true
        var node = this
        nodeReplacer.onEach {
            if (!target.isInstance(node)) return@onEach
            if (this is NodePasser<*>) {
                this as NodePasser<Node>
                node.pass()
            } else {
                this as NodeReplacer<Node>
                node = node.replace(context)
            }
        }
        return node
    }


    // 自顶向下, with 推导类型+类型,语义检查
    private fun Node.passTopDown(owner: AsakaRepresent, context: PassContext): Node {
        owner(owner)
        return when (this) {
            is Expression -> passTopDown(context)
            is Statement -> passTopDown(context)
            else -> Err.type("Unexpected node type ${this.javaClass.name}", source)
        }
    }

    private fun Node.owner(owner: AsakaRepresent) {
        this.block = when (owner) {
            is MethodBlock -> owner
            is Node -> owner.block
            else -> Err.type("Unexpected owner type", source)
        }
        if (this is Expression)
            this.owner = owner
    }

    private fun Expression.passTopDown(owner: AsakaRepresent, context: PassContext): Expression {
        owner(owner)
        return passTopDown(context)
    }


    private fun Expression.passTopDown(context: PassContext): Expression {
        when (this) {
            is UnaryExpression -> {
                target = target.passTopDown(this, context)
                if (operator == NOT) getType().type = A_BOOLEAN else getType().completeWith(target.getType())
            }

            is BinaryExpression -> {
                left = left.passTopDown(this, context)
                right = right.passTopDown(this, context)
                val leftTypeInst = left.getType()
                val rightTypeInst = right.getType()
                val leftType = leftTypeInst.confirm()
                val rightType = rightTypeInst.confirm()
                when (operator) {
                    AS -> {
//                        if (rightType.isAssignableBy(leftType)) {
//                            // Warning here
//                        }
                        if (!rightTypeInst.isChildOf(leftTypeInst))
                            Err.type(
                                "Type mismatch: ${leftTypeInst.display()} cannot be converted to ${rightTypeInst.display()}",
                                source
                            )
                        (right as? ClassCallExpression) ?: Err.syntax("Expected a type", right.source)
                        getType().completeWith(rightTypeInst)
                    }

                    ASSIGN -> {
                        if (!leftTypeInst.isAssignableBy(rightTypeInst))
                            Err.type(
                                "Type mismatch: ${leftTypeInst.display()} cannot be converted to ${rightTypeInst.display()}",
                                source
                            )
                        if (left is VarCallExpression) {
                            val call = left as VarCallExpression
                            val variable = context.variable(call)
                                ?: Err.type("Cannot find variable ${call.name}", call.source)
                            if (variable.value != null && !variable.mutable)
                                Err.type("Cannot assign to a immutable variable", call.source)
                            if (left.getType().unknown())
                                getType().completeWith(rightTypeInst)
                        }
                        getType().completeWith(rightTypeInst)
                    }

                    REF_EQ, REF_NE,
                    EQ, LT, GT, GE, LE, NE,
                    AND, OR, IS -> getType().type = A_BOOLEAN

                    BIT_AND, BIT_OR, BIT_XOR, BIT_NOT,
                    BIT_SHL, BIT_SHR, BIT_USHR -> getType().type =
                        if (leftType == A_LONG || rightType == A_LONG) A_LONG else A_INT

                    else -> getType().type = numberType(leftType, rightType, source)
                }
            }

            is InvokeExpression -> {
                self = self.passTopDown(this, context)
                args.replaceAll { _, arg ->
                    if (arg is LambdaExpression || arg is ReferenceExpression) arg
                    else arg.passTopDown(this, context)
                }
                val call = creator.callVar(name, TypeInst.unknown(source), source)
                fun invoke() {
                    val refs = self.getType().getMethods(name, args, generics)
                    val lambda = context.variable(call)?.let { if (!it.type.lambda()) null else it }
                    fun invokeMethod() {
                        val methodRef: AsahiMethodRef
                        when (refs.size) {
                            0 -> Err.type(
                                "Cannot find Method $name(${
                                    args.values.joinToString(", ") {
                                        it.getType().display()
                                    }
                                }) in class ${self.getType().display()}", source
                            )

                            1 -> methodRef = refs.first()
                            else -> Err.type(
                                "Ambiguous method $name(${
                                    args.values.joinToString(", ") {
                                        it.getType().display()
                                    }
                                }) in class ${self.getType().display()}",
                                source,
                                *refs.map { it.member.source }.toTypedArray()
                            )
                        }
                        val method = methodRef.member
                        val invokeData = methodRef.invokeData
                        invokeData.complete()
                        args.replaceAll { _, arg ->
                            if (arg is LambdaExpression) arg.passTopDown(this, context) else arg
                        }

                        generics.clear()
                        method.generics.forEach {
                            invokeData.generics[it.name]?.let(generics::add)
                                ?: Err.type("Missing generic type ${it.name}", source)
                        }
                        getType().completeWith(methodRef.returnType)

                        val params = method.params
                        val arguments = invokeData.arguments(params, source)
                        when (this) {

                            is ConstructorInvokeExpression -> {
                                this.callSite = method.toCallSite(self, arguments)
                            }

                            is ObjNewExpression -> {
                                this.callSite = method.toCallSite(self, arguments)
                            }

                            is LambdaInvokeExpression -> Err.syntax("Unexpected lambda invoke expression", source)

                            is ArrayNewExpression -> {}

                            is MethodInvokeExpression -> {
                                this.method = method
                                this.callSite = method.toCallSite(self, arguments)
                            }
                            // method invoke
                            else -> this.invoke = MethodInvokeExpression(
                                source,
                                self,
                                name,
                                args,
                                generics,
                                nullSafety,
                                getType()
                            ).apply {
                                if (!method.modifiers.isInline) {
                                    this.method = method
                                    this.callSite = method.toCallSite(self, arguments)
                                } else {
                                    this@passTopDown.inline = true
                                    this.inline = true
                                }
                            }
                        }
                    }
                    if (lambda == null) {
                        invokeMethod()
                        return
                    }
                    val type = lambda.type.type as LambdaType
                    val paramTypes = type.paramTypes
                    val returnType = type.returnType
                    if (args.size != paramTypes.size) {
                        if (refs.isEmpty())
                            Err.type("Expected ${paramTypes.size} arguments but got ${args.size}", source)
                        else invokeMethod()
                        return
                    }
                    // ambiguous todo...
                    args.values.forEachIndexed { index2, argExpr ->
                        val arg = argExpr.getType()
                        val param = paramTypes[index2]
                        argExpr.expect(param)
                        arg.completeWith(param)
                    }
                    getType().completeWith(returnType)
                    this.invoke = creator.invokeLambda(
                        creator.callVar(name, lambda.type, source),
                        args,
                        nullSafety,
                        source
                    ).also {
                        it.next = next
                        it.inline = lambda.inline
                    }
                }
                invoke()
                args.replaceAll { _, arg ->
                    if (arg is ReferenceExpression) arg.passTopDown(this, context) else arg
                }
            }


            is ReferenceExpression -> {
                val refs = mutableListOf<RefSite>()
                if (expected != null) {
                    val type = expected!!.asLambda()
                    val paramTypes = type.paramTypes
                    // Method reference
                    self.getType().getMethods(name, paramTypes).filter {
                        it.returnType.isAssignableBy(type.returnType)
                    }.forEach {
                        it.member.toRefSite(self).let { refSite -> refs.add(refSite) }
                    }
                    // Field reference
                    (self.getType().getField(name)?.let {
                        if (returnType.isAssignableBy(type.returnType)) it
                        else null
                    }?.member as? AsahiField?)?.toRefSite(self)?.let { refs.add(it) }
                } else {
                    self.getType().methods.filterKeys { it.name == name }.values.forEach {
                        refs.add(it.toRefSite(self))
                    }
                    self.getType().fields.filterKeys { it == name }.values.forEach {
                        refs.add(it.toRefSite(self))
                    }
                }
                if (refs.size > 1)
                    Err.type("Ambiguous reference $name in ${self.getType().display()}", source)
                if (refs.isEmpty())
                    Err.type(
                        "Cannot find method or field $name in class ${self.getType().display()}", source
                    )
                refSite = refs.first()
            }

            is FieldCallExpression -> {
                self = self.passTopDown(this, context)
                val field = self.getType().getField(name)
                    ?: Err.type("Cannot find field $name in class ${self.getType().display()}", source)
                getType().completeWith(field.returnType)
                callSite = (field.member as AsahiField).toCallSite(self)
            }

            is VarCallExpression -> {
                val variable = context.variable(this)
                    ?: Err.type("Cannot find variable $name", source)
                getType().completeWith(variable.type)
            }

            is VarNestedCallExpression -> {
                (nestable as Statement).passTopDown(this, context)
                getType().completeWith(nestable.getType())
            }

            is LambdaExpression -> {
                body = body.passMapTo(context)
            }


            else -> {}
        }
        return pass(context) as? Expression
            ?: Err.type("Unexpected node type, the replacement of expression must also be a Expression!", source)
    }

    private fun Statement.passTopDown(context: PassContext): Node {
        when (this) {
            is ReturnStatement -> {
                value = value.passTopDown(this, context)
                val type = value.getType()
                getType().completeWith(type)
            }

            is VarDefineStatement -> {
                context.addVariable(this)
                if (getType().known()) return pass(context)
                value ?: Err.type("Variable without specific type must must be initialized", source)
                val passed = value!!.passTopDown(this, context)
                value = if (passed is VoidExpression) null else passed
                getType().completeWith(passed.getType())
            }

            is IfStatement -> {
                condition = condition.passTopDown(this, context)
                condition.getType().assertType(A_BOOLEAN)
                then = then.passMapTo(context)
                otherwise = otherwise?.passMapTo(context)
                val thenType = then.getType()
                val otherwiseType = otherwise?.getType()
                getType().completeWith(thenType, otherwiseType)
            }

            is WhenStatement -> {
                val newCases = LinkedHashMap<Expression, BlockNode>()
                cases.forEach { (key, value) ->
                    newCases[key.passTopDown(this, context)] = value.passMapTo(context)
                }
                cases.clear()
                cases.putAll(newCases)
                val types = cases.values.map { it.getType() }
                if (types.isEmpty()) {
                    getType().type = A_VOID
                    return pass(context)
                }
                var type = types.first()
                types.forEach {
                    type = type.mix(it)
                }
                getType().completeWith(type)
            }

            is TryCatchStatement -> {
                tryBlock = tryBlock.passMapTo(context)
                catchBlocks.replaceAll { _, catch ->
                    catch.passMapTo(context)
                }
                val tryType = tryBlock.getType()
                val catchTypes = catchBlocks.values.map { it.getType() }
                if (catchTypes.isEmpty()) {
                    getType().completeWith(tryType)
                    return pass(context)
                }
                var type = tryType
                catchTypes.forEach {
                    type = type.mix(it)
                }
                getType().completeWith(type)
            }

            is WhileStatement -> {
                condition = condition.passTopDown(this, context)
                condition.getType().assertType(A_BOOLEAN)
                body = body.passMapTo(context)
            }

            else -> {}
        }
        return pass(context)
    }

    override fun register(pass: AsakaPass) {
        prologues.register(pass)
        nodeReplacer.register(pass)
        epilogues.register(pass)
    }


    private fun <E : Expression> E.expect(type: TypeInst): E {
        if (type.known()) expected = type
        return this
    }

    private fun Node.isReturn(): Boolean = when (this) {
        is ReturnStatement -> true
        is BlockNodeKind -> lastOrNull()?.isReturn() ?: false
        is IfStatement -> then.isReturn() && (otherwise?.isReturn() ?: true)
        is WhileStatement -> false
        is TryCatchStatement -> tryBlock.isReturn() && (catchBlocks.all { it.value.isReturn() } || finallyBlock?.isReturn() ?: true)
        else -> false
    }

    private fun MethodBlock.inferType(inferred: TypeInst? = null): TypeInst {
        if (size == 1) {
            return first().inferType(inferred)
        } else {
            var type: TypeInst? = null
            filterIsInstance<ReturnStatement>().forEach {
                type = it.value.inferType(type)
            }
            val lastOrNull = lastOrNull()
            if (type != null && lastOrNull != null && !lastOrNull.isReturn())
                Err.syntax("Missing return statement", lastOrNull.source)
            if (type == null) {
                val last = lastOrNull ?: VoidExpression(label.source)
                type = last.inferType(inferred)
            }
            return type!!
        }
    }

    private fun Node.inferType(inferred: TypeInst? = null): TypeInst {
        val type: TypeInst = when (this) {
            is BlockNodeKind -> body.inferType(inferred)

            is TypeInferable ->
                if (getType().unknown())
                    Err.type("Cannot infer type", source)
                else getType()

            else -> A_VOID.toInst(source)
        }
        val common = type.intersectSuperWith(inferred ?: return type)
        if (common.isEmpty())
            Err.type(
                """
                Type mismatch: ${type.display()} cannot be converted to ${inferred.display()} and they havent a common super type
                """.trimIndent(), source
            )
        return type
    }

    private class PassContainer<T>(private val target: Class<T>) {
        private val all = HashSet<T>()
        private val processes = LinkedHashSet<T>()

        fun onEach(action: T.() -> Unit) {
            processes.onEach(action)
        }

        fun register(pass: AsakaPass) {
            if (!target.isInstance(pass)) return
            all.add(pass as T)
            processes.clear()
            all.forEach { addPass(it as AsakaPass) }
            if (processes.size < all.size) {
                throw IllegalArgumentException("Circular dependency detected")
            }
        }

        private fun addPass(pass: AsakaPass) {
            if (processes.contains(pass as T)) return
            pass.depends.forEach(::addPass)
            processes.add(pass)
        }
    }


    private fun numberType(a: AsakaType, b: AsakaType, source: Span): AsakaType {
        if (a == A_DOUBLE || b == A_DOUBLE)
            return A_DOUBLE
        else if (a == A_FLOAT || b == A_FLOAT)
            return A_FLOAT
        else if (a == A_LONG || b == A_LONG)
            return A_LONG
        else if (a == A_INT || b == A_INT)
            return A_INT
        else if (a == A_SHORT || b == A_SHORT)
            return A_SHORT
        else if (a == A_BYTE || b == A_BYTE)
            return A_BYTE
        else
            Err.syntax("Unexpected number type", source)
    }

    override fun register(path: String) {
        getClasses(path)
            .filter { it.isAnnotationPresent(AutoRegisterPass::class.java) && AsakaPass::class.java.isAssignableFrom(it) }
            .mapNotNull { it.instance }
            .filterIsInstance<AsakaPass>()
            .forEach(this::register)
    }
}