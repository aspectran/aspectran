/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.thymeleaf.expression;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.ExpressionParserException;
import com.aspectran.core.context.asel.TokenizedExpression;
import com.aspectran.core.context.asel.ognl.OgnlSupport;
import com.aspectran.thymeleaf.context.CurrentActivityHolder;
import com.aspectran.thymeleaf.context.UtilizedOgnlContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.IStandardConversionService;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.SelectionVariableExpression;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.expression.VariableExpression;
import org.thymeleaf.standard.util.StandardExpressionUtils;

import static com.aspectran.thymeleaf.expression.OgnlContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS;

/**
 * Evaluator for variable expressions ({@code ${...}}) in Thymeleaf Standard Expressions, using the
 * Aspectran expression language.
 *
 * <p>Created: 2024. 11. 23.</p>
 */
public class ASELVariableExpressionEvaluator implements IStandardVariableExpressionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ASELVariableExpressionEvaluator.class);

    private static final String EXPRESSION_CACHE_TYPE_OGNL = "asel";

    private final boolean applyOgnlShortcuts;

    public ASELVariableExpressionEvaluator(boolean applyOgnlShortcuts) {
        this.applyOgnlShortcuts = applyOgnlShortcuts;

        /*
         * INITIALIZE AND REGISTER THE PROPERTY ACCESSOR
         */
        OgnlContextPropertyAccessor accessor = new OgnlContextPropertyAccessor();
        OgnlRuntime.setPropertyAccessor(IContext.class, accessor);
    }

    @Override
    public Object evaluate(
            IExpressionContext context, IStandardVariableExpression expression,
            StandardExpressionExecutionContext exeContext) {
        return evaluate(context, expression, exeContext, applyOgnlShortcuts);
    }

    private static Object evaluate(
           IExpressionContext context, IStandardVariableExpression expression,
           StandardExpressionExecutionContext exeContext, boolean applyOgnlShortcuts) {
        if (logger.isTraceEnabled()) {
            logger.trace("[THYMELEAF][" + TemplateEngine.threadIndex() +
                "] AspectranEL expression: evaluating expression \"" + expression.getExpression() + "\" on target");
        }

        try {
            String expressionStr = expression.getExpression();
            if (expressionStr == null) {
                throw new TemplateProcessingException("Expression content is null, which is not allowed");
            }

            Activity activity = (context instanceof CurrentActivityHolder holder ? holder.getActivity() : null);
            IEngineConfiguration configuration = context.getConfiguration();
            boolean useSelectionAsRoot = expression.getUseSelectionAsRoot();

            ComputedOgnlExpression parsedExpression = obtainComputedOgnlExpression(
                    activity, configuration, expression, expressionStr, exeContext, applyOgnlShortcuts);

            OgnlContext contextVariables = resolveContextVariables(context, exeContext, parsedExpression);

            // The root object on which we will evaluate expressions will depend on whether a selection target is
            // active or not...
            ITemplateContext templateContext = (context instanceof ITemplateContext tc ? tc : null);
            Object root = (useSelectionAsRoot && templateContext != null && templateContext.hasSelectionTarget() ?
                    templateContext.getSelectionTarget() : templateContext);

            if (root != null) {
                contextVariables.setRoot(root);
            }

            // Execute the expression!
            Object result;
            try {
                result = executeExpression(activity, configuration, parsedExpression.expression, contextVariables);
            } catch (OgnlShortcutExpression.OGNLShortcutExpressionNotApplicableException notApplicable) {
                // We tried to apply shortcuts, but it is not possible for this expression even if it parsed OK,
                // so we need to empty the cache and try again disabling shortcuts. Once processed for the first time,
                // an OGNL (non-shortcut) parsed expression will already be cached and this exception will not be
                // thrown again
                invalidateComputedOgnlExpression(configuration, expression, expressionStr);
                return evaluate(context, expression, exeContext, false);
            }

            if (exeContext.getPerformTypeConversion()) {
                IStandardConversionService conversionService = StandardExpressions.getConversionService(configuration);
                return conversionService.convert(context, result, String.class);
            } else {
                return result;
            }
        } catch (Exception e) {
            throw new TemplateProcessingException(
                    "Exception evaluating OGNL expression: \"" + expression.getExpression() + "\"", e);
        }
    }

    @NonNull
    private static OgnlContext resolveContextVariables(
            IExpressionContext context,
            StandardExpressionExecutionContext exeContext,
            @NonNull ComputedOgnlExpression parsedExpression) {
        OgnlContext contextVariables;
        if (parsedExpression.mightNeedExpressionObjects) {
            // The IExpressionObjects implementation returned by processing contexts that include the Standard
            // Dialects will be lazy in the creation of expression objects (i.e. they won't be created until really
            // needed). And in order for this behaviour to be accepted by OGNL, we will be wrapping this object
            // inside an implementation of Map<String,Object>, which will afterwards be fed to the constructor
            // of an OgnlContext object.

            // Note this will never happen with shortcut expressions, as the '#' character with which all
            // expression object names start is not allowed by the OgnlShortcutExpression parser.
            contextVariables = new UtilizedOgnlContext(context.getExpressionObjects());
        } else {
            contextVariables = OgnlSupport.createDefaultContext();
        }

        // We might need to apply restrictions on the request parameters. In the case of OGNL, the only way we
        // can actually communicate with the PropertyAccessor, (OGNLVariablesMapPropertyAccessor), which is the
        // agent in charge of applying such restrictions, is by adding a context variable that the property accessor
        // can later lookup during evaluation.
        if (exeContext.getRestrictVariableAccess()) {
            contextVariables.put(RESTRICT_REQUEST_PARAMETERS, RESTRICT_REQUEST_PARAMETERS);
        }

        return contextVariables;
    }

    private static ComputedOgnlExpression obtainComputedOgnlExpression(
            Activity activity, IEngineConfiguration configuration,
            IStandardVariableExpression expression, String expressionStr,
            @NonNull StandardExpressionExecutionContext exeContext,
            boolean applyOgnlShortcuts) throws ExpressionParserException {
        // If restrictions apply, we want to avoid applying shortcuts so that we delegate to OGNL validation
        // of method calls and references to allowed classes.
        boolean doApplyOgnlShortcuts = applyOgnlShortcuts &&
                !exeContext.getRestrictVariableAccess() && !exeContext.getRestrictInstantiationAndStatic();

        if (exeContext.getRestrictInstantiationAndStatic() &&
                StandardExpressionUtils.containsOGNLInstantiationOrStaticOrParam(expressionStr)) {
            throw new TemplateProcessingException(
                "Instantiation of new objects and access to static classes or parameters is forbidden in this context");
        }

        if (expression instanceof VariableExpression ve) {
            Object cachedExpression = ve.getCachedExpression();
            if (cachedExpression instanceof ComputedOgnlExpression coe) {
                return coe;
            }
            cachedExpression = parseComputedOgnlExpression(activity, configuration, expressionStr, doApplyOgnlShortcuts);
            ve.setCachedExpression(cachedExpression);
            return (ComputedOgnlExpression)cachedExpression;
        }

        if (expression instanceof SelectionVariableExpression sve) {
            Object cachedExpression = sve.getCachedExpression();
            if (cachedExpression instanceof ComputedOgnlExpression coe) {
                return coe;
            }
            cachedExpression = parseComputedOgnlExpression(activity, configuration, expressionStr, doApplyOgnlShortcuts);
            sve.setCachedExpression(cachedExpression);
            return (ComputedOgnlExpression)cachedExpression;
        }

        return parseComputedOgnlExpression(activity, configuration, expressionStr, doApplyOgnlShortcuts);
    }

    @NonNull
    private static ComputedOgnlExpression parseComputedOgnlExpression(
            Activity activity, IEngineConfiguration configuration, String expressionStr,
            boolean applyOGNLShortcuts) throws ExpressionParserException {
        ComputedOgnlExpression parsedExpression =
                ExpressionCache.getFromCache(configuration, expressionStr, EXPRESSION_CACHE_TYPE_OGNL);
        if (parsedExpression != null) {
            return parsedExpression;
        }
        // The result of parsing might be an OGNL expression AST or a ShortcutOGNLExpression (for simple cases)
        parsedExpression = parseExpression(activity, expressionStr, applyOGNLShortcuts);
        ExpressionCache.putIntoCache(configuration, expressionStr, parsedExpression, EXPRESSION_CACHE_TYPE_OGNL);
        return parsedExpression;
    }

    private static void invalidateComputedOgnlExpression(
            IEngineConfiguration configuration, IStandardVariableExpression expression, String expressionStr) {
        if (expression instanceof VariableExpression ve) {
            ve.setCachedExpression(null);
        } else if (expression instanceof SelectionVariableExpression ve) {
            ve.setCachedExpression(null);
        }
        ExpressionCache.removeFromCache(configuration, expressionStr, EXPRESSION_CACHE_TYPE_OGNL);
    }

    @NonNull
    private static ComputedOgnlExpression parseExpression(
            Activity activity, String expressionStr, boolean applyOgnlShortcuts) throws ExpressionParserException {
        if (applyOgnlShortcuts) {
            String[] expressions = OgnlShortcutExpression.parse(expressionStr);
            if (expressions != null) {
                OgnlShortcutExpression ose = new OgnlShortcutExpression(expressions);
                boolean mightNeedExpressionObjects = StandardExpressionUtils.mightNeedExpressionObjects(expressionStr);
                return new ComputedOgnlExpression(ose, mightNeedExpressionObjects);
            }
        }
        boolean mightNeedExpressionObjects = false;
        Object parsedExpression;
        if (activity != null) {
            TokenizedExpression tokenizedExpression = new TokenizedExpression(expressionStr);
            String substitutedExpression = tokenizedExpression.getSubstitutedExpression();
            if (substitutedExpression != null) {
                mightNeedExpressionObjects = StandardExpressionUtils.mightNeedExpressionObjects(substitutedExpression);
            }
            parsedExpression = tokenizedExpression;
        } else {
            try {
                parsedExpression = Ognl.parseExpression(expressionStr);
            } catch (OgnlException e) {
                throw new ExpressionParserException(expressionStr, e);
            }
            mightNeedExpressionObjects = StandardExpressionUtils.mightNeedExpressionObjects(expressionStr);
        }
        return new ComputedOgnlExpression(parsedExpression, mightNeedExpressionObjects);
    }

    private static Object executeExpression(
            Activity activity, IEngineConfiguration configuration,
            Object parsedExpression, OgnlContext contextVariables) throws Exception {
        if (parsedExpression instanceof OgnlShortcutExpression ose) {
            return ose.evaluate(configuration, contextVariables, contextVariables.getRoot());
        }
        if (activity != null && parsedExpression instanceof TokenizedExpression te) {
            return te.evaluate(activity, contextVariables, contextVariables.getRoot());
        } else {
            return Ognl.getValue(parsedExpression, contextVariables, contextVariables.getRoot());
        }
    }

    private static class ComputedOgnlExpression {

        Object expression;

        boolean mightNeedExpressionObjects;

        ComputedOgnlExpression(Object expression, boolean mightNeedExpressionObjects) {
            this.expression = expression;
            this.mightNeedExpressionObjects = mightNeedExpressionObjects;
        }

    }

    @Override
    public String toString() {
        return "AspectranEL";
    }

}
