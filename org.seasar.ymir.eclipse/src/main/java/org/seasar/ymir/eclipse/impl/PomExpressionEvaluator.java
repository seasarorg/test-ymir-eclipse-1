package org.seasar.ymir.eclipse.impl;

import net.skirnir.freyja.ExpressionEvaluator;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;

class PomExpressionEvaluator implements ExpressionEvaluator {
    public Object evaluate(TemplateContext context, VariableResolver varResolver, String expression) {
        return expression;
    }

    public boolean isTrue(Object obj) {
        return false;
    }
}
