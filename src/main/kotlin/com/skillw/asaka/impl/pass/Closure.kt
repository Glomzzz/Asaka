package com.skillw.asaka.impl.pass

import com.skillw.asaka.Asaka.creator
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.ClassBlock
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.ast.LambdaBlockNode
import com.skillw.asaka.core.ir.ast.LambdaExpression
import com.skillw.asaka.core.ir.data.Closure
import com.skillw.asaka.core.ir.type.LambdaType
import com.skillw.asaka.core.pass.AsakaPass
import com.skillw.asaka.core.pass.AutoRegisterPass
import com.skillw.asaka.core.pass.Epilogue
import com.skillw.asaka.core.pass.NodePasser

@AutoRegisterPass
object MutableVarCapture : AsakaPass("var-capture"), NodePasser<LambdaBlockNode> {
    override val target = LambdaBlockNode::class.java
    override fun LambdaBlockNode.pass() {
        if (inline) return
        usedVars.filter { it.mutable }.forEach { it.ref = true }
    }
}


@AutoRegisterPass
object LambdaPasser : AsakaPass("lambda-passer"), NodePasser<LambdaExpression>, Epilogue {
    override val target = LambdaExpression::class.java
    private const val KEY = "lambdas-to-pass"
    override fun LambdaExpression.pass() {
        val main = block.clazz()
        if (main.passing)
            main.data(KEY) { HashSet<LambdaExpression>() }.add(this)
        else
            passLambda()
    }

    private fun LambdaExpression.passLambda() {
        if (returnType.unknown())
            Err.syntax("Cannot infer return type", source)
        val type = (confirmedType() as LambdaType).returnType
        val body = body
        val last = body.lastOrNull()
        if (!type.void() && last is Expression) {
            if (last.getType().lambda())
                last.getType().completeWith(type)
            body.removeLastOrNull()
            body.add(creator.createReturn(body.randomLabel(last.source), last, last.source, type))
        }
        if (type.void()) {
            body.add(creator.createReturn(body.randomLabel(source), creator.void(source), source))
        }
        if (closure()) {
            body.getType().completeWith(type)
            closure = Closure(
                name,
                params,
                confirmedType() as LambdaType,
                returnType,
                body.body
            )
        } else {
            val stm = buildMethod()
            block.clazz().addMethod(stm)
            val method = stm.toMember()
            refSite = method.toRefSite(creator.callClass(stm.body.self().toInst(source)))
        }
    }

    override fun ClassBlock.epilogue() {
        data<Set<LambdaExpression>?>(KEY)?.forEach {
            it.passLambda()
        }
    }

}

