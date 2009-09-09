/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.AptIllegalStateException;
import org.seasar.doma.internal.expr.ExpressionParser;
import org.seasar.doma.internal.expr.node.ExpressionNode;
import org.seasar.doma.internal.jdbc.sql.node.BindVariableNode;
import org.seasar.doma.internal.jdbc.sql.node.BindVariableNodeVisitor;
import org.seasar.doma.internal.jdbc.sql.node.ElseifNode;
import org.seasar.doma.internal.jdbc.sql.node.ElseifNodeVisitor;
import org.seasar.doma.internal.jdbc.sql.node.IfNode;
import org.seasar.doma.internal.jdbc.sql.node.IfNodeVisitor;
import org.seasar.doma.internal.jdbc.sql.node.SqlLocation;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.message.DomaMessageCode;

/**
 * @author taedium
 * 
 */
public class SqlValidator implements BindVariableNodeVisitor<Void, Void>,
        IfNodeVisitor<Void, Void>, ElseifNodeVisitor<Void, Void> {

    protected final ProcessingEnvironment env;

    protected final QueryMeta queryMeta;

    protected final ExecutableElement method;

    protected final String path;

    protected final ExpressionValidator expressionValidator;

    public SqlValidator(ProcessingEnvironment env, QueryMeta queryMeta,
            ExecutableElement method, String path) {
        assertNotNull(env, queryMeta, method, path);
        this.env = env;
        this.queryMeta = queryMeta;
        this.method = method;
        this.path = path;
        expressionValidator = new ExpressionValidator(env, queryMeta, method,
                path);
    }

    public void validate(SqlNode sqlNode) {
        sqlNode.accept(this, null);
    }

    @Override
    public Void visitBindVariableNode(BindVariableNode node, Void p) {
        validateExpressionVariable(node.getLocation(), node.getVariableName());
        return null;
    }

    @Override
    public Void visitIfNode(IfNode node, Void p) {
        validateExpressionVariable(node.getLocation(), node.getExpression());
        visitNode(node, p);
        return null;
    }

    @Override
    public Void visitElseifNode(ElseifNode node, Void p) {
        validateExpressionVariable(node.getLocation(), node.getExpression());
        visitNode(node, p);
        return null;
    }

    @Override
    public Void visitUnknownNode(SqlNode node, Void p) {
        visitNode(node, p);
        return null;
    }

    protected void visitNode(SqlNode node, Void p) {
        for (SqlNode child : node.getChildren()) {
            child.accept(this, p);
        }
    }

    protected void validateExpressionVariable(SqlLocation location,
            String expression) {
        try {
            ExpressionParser parser = new ExpressionParser(expression);
            ExpressionNode expressionNode = parser.parse();
            expressionValidator.validate(expressionNode);
        } catch (AptIllegalStateException e) {
            throw e;
        } catch (AptException e) {
            throw new AptException(DomaMessageCode.DOMA4092, env, method, e,
                    location.getSql(), location.getLineNumber(), location
                            .getPosition(), e);
        }
    }

}