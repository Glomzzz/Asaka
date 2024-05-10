package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.ir.type.A_VOID
import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.ir.ast.ClassBlock
import com.skillw.asaka.core.ir.ast.ClassDefinition
import com.skillw.asaka.core.ir.ast.MethodDefinition
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.type.AsakaClass
import com.skillw.asaka.core.ir.type.TypeInst

abstract class ClassBuilderImpl<B : ModifiersBuilder>(scope: BuilderScope, name: String) :
    ClassKindBuilderImpl(scope, name),
    ClassBuilder<B> {
    val constructors = mutableListOf<ConstructorBuilder>()

    val modifiers = ModifiersBuilderImpl(scope)
    val superTypes = mutableListOf<Builder<TypeInst>>()

    override fun extends(vararg types: Builder<TypeInst>) {
        superTypes.addAll(types)
    }

    override fun modifiers(init: B.() -> Unit) {
        (modifiers as B).init()
    }

    override fun constructor(init: ConstructorBuilder.() -> Unit) {
        val builder = ConstructorBuilderImpl(scope)
        builder.init()
        constructors.add(builder)
    }

    override fun buildBody(context: BuildContext, clazz: AsakaClass): ClassBlock {
        return context.block(super.buildBody(context, clazz)) {
            if (constructors.isEmpty()) {
                this.addMethod(
                    MethodDefinition(
                        source,
                        clazz.toInst(source),
                        "<init>",
                        emptyList(),
                        setOf(Modifier.PUBLIC),
                        clazz.genericTypes,
                        CommonBlockBuilderImpl(scope, "<init>").build(context)
                    )
                )
            } else
                constructors.forEach {
                    val constructor = it.build(context)
                    this.addMethod(
                        MethodDefinition(
                            source,
                            constructor.returnType,
                            "<init>",
                            constructor.params,
                            constructor.modifiersSet,
                            constructor.generics,
                            constructor.body!!
                        )
                    )
                }
        }
    }
}

class ClassDefineBuilderImpl(scope: BuilderScope, name: String) :
    ClassBuilderImpl<ClassModifiersBuilder>(scope, name), ClassDefineBuilder {


    override val generics = mutableListOf<GenericBuilder>()
    override fun generics(builder: GenericsBuilder.() -> Unit) {
        GenericsBuilderImpl(scope, generics, this).apply(builder)
    }

    override fun buildClass(context: BuildContext) = AsakaClass(
        source,
        name,
        modifiers.build(context),
        superTypes.map { it.build(context) }.toSet(),
        generics.map { it.build(context) },
    )


    override fun buildTarget(context: BuildContext): ClassDefinition {
        target = creator.defineClass(buildClass(context), source)
        return target?.apply { clazz.body = buildBody(context, clazz) }!!
    }
}

abstract class ClassKindBuilderImpl(scope: BuilderScope, override val name: String) :
    BuilderImpl<ClassDefinition>(scope),
    ClassKindBuilder {
    val clinit = CommonBlockBuilderImpl(scope, "<clinit>")
    val methods = mutableListOf<MethodDefineBuilder>()
    val fields = mutableListOf<FieldDefineBuilder>()

    override fun clinit(init: CommonBlockBuilder.() -> Unit) {
        scope.block(clinit, init)
    }

    override fun defineMethod(name: String, builder: MethodDefineBuilder.() -> Unit) {
        val methodBuilder = MethodDefineBuilderImpl(scope, name).also { scope.method(it, builder) }
        methods.add(methodBuilder)
    }

    override fun defineField(name: String, builder: FieldDefineBuilder.() -> Unit) {
        val fieldBuilder = FieldDefineBuilderImpl(scope, name)
        fieldBuilder.builder()
        fields.add(fieldBuilder)
    }

    abstract fun buildClass(context: BuildContext): AsakaClass


    open fun buildBody(context: BuildContext, clazz: AsakaClass): ClassBlock {
        return context.block(context.block().clazz(Identifier(name, source), clazz)) {
            fields.map { it.build(context) }.forEach(this::addField)
            methods.map { it.build(context) }.forEach(this::addMethod)
            this.addMethod(
                MethodDefinition(
                    source,
                    A_VOID.toInst(source),
                    "<clinit>",
                    emptyList(),
                    setOf(Modifier.STATIC, Modifier.FINAL),
                    emptyList(),
                    clinit.build(context)
                )
            )
        }
    }
}

class EnumDefineBuilderImpl(scope: BuilderScope, name: String) :
    ClassBuilderImpl<ClassModifiersBuilder>(scope, name),
    EnumDefineBuilder {

    val enums = ArrayList<EnumInstanceBuilderImpl>()
    override fun enum(name: String, vararg args: ExprBuilder<*>, block: (EnumInstanceBuilder.() -> Unit)?) {
        val obj = EnumInstanceBuilderImpl(scope, name, this).apply {
            modifiers { static();final() }
            constructor {
                superInit(*args)
            }
        }
        block?.invoke(obj)
        enums.add(obj)
    }

    override fun enum(
        name: String,
        args: LinkedHashMap<String, ExprBuilder<*>>,
        block: (EnumInstanceBuilder.() -> Unit)?
    ) {
        val obj = EnumInstanceBuilderImpl(scope, name, this).apply {
            modifiers { static();final() }
            constructor {
                superInit(args)
            }
        }
        block?.invoke(obj)
    }

    override fun buildClass(context: BuildContext) = AsakaClass(
        source,
        name,
        modifiers.build(context),
        superTypes.map { it.build(context) }.toSet(),
        emptyList()
    )

    override fun buildTarget(context: BuildContext) = creator.defineClass(
        buildClass(context).apply {
            body = buildBody(context, this).apply {
                context.block(this) {
                    enums.forEach {
                        addClass(it.build(context))
                    }
                }
            }
        },
        source,
    )
}

class EnumInstanceBuilderImpl(scope: BuilderScope, name: String, val enumBuilder: EnumDefineBuilder) :
    ClassKindBuilderImpl(scope, name),
    EnumInstanceBuilder {

    val modifiers = ModifiersBuilderImpl(scope)

    override fun modifiers(init: InnerClassModifiersBuilder.() -> Unit) {
        modifiers.init()
    }

    override fun buildClass(context: BuildContext) = AsakaClass(
        source,
        name,
        modifiers.build(context),
        setOf(enumBuilder.build(context).clazz.toInst(source)),
        emptyList()
    )

    override fun buildTarget(context: BuildContext) = creator.defineClass(
        buildClass(context).apply { body = buildBody(context, this) },
        source
    )
}

class InnerClassBuilderImpl(scope: BuilderScope, name: String) :
    ClassBuilderImpl<InnerClassModifiersBuilder>(scope, name),
    InnerClassBuilder {
    override val generics = mutableListOf<GenericBuilder>()

    override fun generics(builder: GenericsBuilder.() -> Unit) {
        GenericsBuilderImpl(scope, generics, this).apply(builder)
    }

    override fun buildClass(context: BuildContext) = AsakaClass(
        source,
        name,
        modifiers.build(context),
        superTypes.map { it.build(context) }.toSet(),
        generics.map { it.build(context) },
    )

    override fun buildTarget(context: BuildContext) = creator.defineClass(
        buildClass(context).apply { body = buildBody(context, this) },
        source,
    )
}