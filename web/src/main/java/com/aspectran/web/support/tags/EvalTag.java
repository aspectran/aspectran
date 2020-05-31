/*
 * Copyright (c) 2008-2020 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.support.tags;

import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.ObjectUtils;
import com.aspectran.web.support.util.JavaScriptUtils;
import com.aspectran.web.support.util.TagUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * The {@code <eval>} tag evaluates a Token expression and either prints
 * the result or assigns it to a variable.
 *
 * <p>Created: 2020/05/31</p>
 */
public class EvalTag extends HtmlEscapingAwareTag {

    /**
     * {@link javax.servlet.jsp.PageContext} attribute for the
     * page-level {@link TokenEvaluator} instance.
     */
    private static final String TOKEN_EVALUATOR_PAGE_ATTRIBUTE =
            "com.aspectran.web.support.tags.TOKEN_EVALUATOR";

    @Nullable
    private Token[] tokens;

    @Nullable
    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private boolean javaScriptEscape = false;

    /**
     * Set the expression to evaluate.
     */
    public void setExpression(String expression) {
        this.tokens = TokenParser.parse(expression);
    }

    /**
     * Set the variable name to expose the evaluation result under.
     * Defaults to rendering the result to the current JspWriter.
     */
    public void setVar(@Nullable String var) {
        this.var = var;
    }

    /**
     * Set the scope to export the evaluation result to.
     * This attribute has no meaning unless var is also defined.
     */
    public void setScope(String scope) {
        this.scope = TagUtils.getScope(scope);
    }

    /**
     * Set JavaScript escaping for this tag, as boolean value.
     * Default is "false".
     */
    public void setJavaScriptEscape(boolean javaScriptEscape) {
        this.javaScriptEscape = javaScriptEscape;
    }

    @Override
    public int doStartTagInternal() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        TokenEvaluator tokenEvaluator =
                (TokenEvaluator)this.pageContext.getAttribute(TOKEN_EVALUATOR_PAGE_ATTRIBUTE);
        if (tokenEvaluator == null) {
            tokenEvaluator = new TokenEvaluation(getCurrentActivity());
            super.pageContext.setAttribute(TOKEN_EVALUATOR_PAGE_ATTRIBUTE, tokenEvaluator);
        }
        if (var != null) {
            Object result = tokenEvaluator.evaluate(tokens);
            super.pageContext.setAttribute(var, result, scope);
        } else {
            try {
                Object result = tokenEvaluator.evaluate(tokens);
                String str = ObjectUtils.getDisplayString(result);
                str = htmlEscape(str);
                if (javaScriptEscape) {
                    str = JavaScriptUtils.javaScriptEscape(str);
                }
                this.pageContext.getOut().print(str);
            } catch (IOException ex) {
                throw new JspException(ex);
            }
        }
        return EVAL_PAGE;
    }

}
