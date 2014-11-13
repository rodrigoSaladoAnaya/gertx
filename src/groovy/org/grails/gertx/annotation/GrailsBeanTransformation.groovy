package org.grails.gertx.annotation

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GrailsBeanTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        DeclarationExpression exp = nodes[1]
        def beanName = exp.variableExpression.name
        BlockStatement block = assignBean(beanName)
        ExpressionStatement expSmt = block.statements[0]
        List<Statement> statements = source.getAST().statementBlock.statements
        statements.each { ExpressionStatement es ->
            if (es.expression instanceof DeclarationExpression) {
                DeclarationExpression de = (DeclarationExpression) es.expression
                if (de.variableExpression.name == beanName) {
                    de.rightExpression = expSmt.expression
                }
            }
        }
    }

    BlockStatement assignBean(String beanName) {
        def statements = """
            org.grails.gertx.utils.AppContext.instance.${beanName}
		"""
        AstBuilder ab = new AstBuilder()
        List<ASTNode> res = ab.buildFromString(
                CompilePhase.SEMANTIC_ANALYSIS, false, statements
        )
        BlockStatement bs = res[0]
        return bs
    }
}
