package org.grails.gertx.annotation

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GrailsBeanTransformation implements ASTTransformation {

    private boolean isNodesOk(ASTNode[] nodes) {
        if (!nodes) return false;
        if (nodes.size() != 2) return false;
        if (!(nodes[1] instanceof DeclarationExpression)) return false;
        return true;
    }

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (!isNodesOk(nodes)) return;

        DeclarationExpression exp = nodes[1]
        if (!(exp?.rightExpression instanceof EmptyExpression)) {
            addError(
                    "@GrailsBean is expecting a [EmptyExpression] but got: " +
                            "[${exp.rightExpression.getClass().simpleName}],",
                    nodes[1],
                    source
            )
            return
        }
        String beanName = exp.variableExpression.name
        BlockStatement block = assignBean(beanName)
        ExpressionStatement expVal = block.statements[0]
        List<Statement> st = source.getAST().statementBlock.statements
        st.each { ExpressionStatement es ->
            if (es.expression instanceof DeclarationExpression) {
                DeclarationExpression de = (DeclarationExpression) es.expression
                if (de.variableExpression.name == beanName) {
                    de.rightExpression = expVal.expression
                }
            }
        }
    }

    private BlockStatement assignBean(String beanName) {
        def statements = """
            org.grails.gertx.utils.AppContext.instance.${beanName}
        """
        AstBuilder ab = new AstBuilder()
        List<ASTNode> res = ab.buildFromString(
                CompilePhase.SEMANTIC_ANALYSIS, false, statements
        )
        return res[0] as BlockStatement
    }

    private void addError(String msg, ASTNode node, SourceUnit source) {
        int line = node.getLineNumber()
        int col = node.getColumnNumber()
        SyntaxException se = new SyntaxException(msg + '\n', line, col)
        Message message = new SyntaxErrorMessage(se, source)
        source.getErrorCollector().addErrorAndContinue(message);
    }
}
