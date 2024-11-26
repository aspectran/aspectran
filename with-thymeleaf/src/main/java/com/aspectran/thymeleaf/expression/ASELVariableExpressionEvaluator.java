package com.aspectran.thymeleaf.expression;

import com.aspectran.core.context.asel.ognl.OgnlSupport;
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

import java.util.Collections;
import java.util.Map;

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

    private static final Map<String,Object> CONTEXT_VARIABLES_MAP_NOEXPOBJECTS_RESTRICTIONS =
            Collections.singletonMap(RESTRICT_REQUEST_PARAMETERS, RESTRICT_REQUEST_PARAMETERS);

    private final boolean applyOgnlShortcuts;

    public ASELVariableExpressionEvaluator(boolean applyOgnlShortcuts) {
        super();
        this.applyOgnlShortcuts = applyOgnlShortcuts;

        /*
         * INITIALIZE AND REGISTER THE PROPERTY ACCESSOR
         */
        OgnlContextPropertyAccessor accessor = new OgnlContextPropertyAccessor();
        OgnlRuntime.setPropertyAccessor(IContext.class, accessor);
    }

    public final Object evaluate(
            IExpressionContext context, IStandardVariableExpression expression,
            StandardExpressionExecutionContext exeContext) {
        return evaluate(context, expression, exeContext, applyOgnlShortcuts);
    }

    private static Object evaluate(
           IExpressionContext context, IStandardVariableExpression expression,
           StandardExpressionExecutionContext exeContext, boolean applyOgnlShortcuts) {
        if (logger.isTraceEnabled()) {
            logger.trace("[THYMELEAF][" + TemplateEngine.threadIndex() +
                "] SpringEL expression: evaluating expression \"" + expression.getExpression() + "\" on target");
        }

        try {
            String expressionStr = expression.getExpression();
            if (expressionStr == null) {
                throw new TemplateProcessingException("Expression content is null, which is not allowed");
            }

            IEngineConfiguration configuration = context.getConfiguration();
            boolean useSelectionAsRoot = expression.getUseSelectionAsRoot();

            ComputedOgnlExpression parsedExpression =
                obtainComputedOgnlExpression(configuration, expression, expressionStr, exeContext, applyOgnlShortcuts);

            Map<String, Object> contextVariables = resolveContextVariables(context, exeContext, parsedExpression);

            // The root object on which we will evaluate expressions will depend on whether a selection target is
            // active or not...
            ITemplateContext templateContext = (context instanceof ITemplateContext ? (ITemplateContext)context : null);
            Object root = (useSelectionAsRoot && templateContext != null && templateContext.hasSelectionTarget() ?
                    templateContext.getSelectionTarget() : templateContext);

            // Execute the expression!
            Object result;
            try {
                result = executeExpression(configuration, parsedExpression.expression, contextVariables, root);
            } catch (OgnlShortcutExpression.OGNLShortcutExpressionNotApplicableException notApplicable) {
                // We tried to apply shortcuts, but it is not possible for this expression even if it parsed OK,
                // so we need to empty the cache and try again disabling shortcuts. Once processed for the first time,
                // an OGNL (non-shortcut) parsed expression will already be cached and this exception will not be
                // thrown again
                invalidateComputedOgnlExpression(configuration, expression, expressionStr);
                return evaluate(context, expression, exeContext, false);
            }

            if (!exeContext.getPerformTypeConversion()) {
                return result;
            }

            IStandardConversionService conversionService = StandardExpressions.getConversionService(configuration);
            return conversionService.convert(context, result, String.class);
        } catch (Exception e) {
            throw new TemplateProcessingException(
                "Exception evaluating OGNL expression: \"" + expression.getExpression() + "\"", e);
        }
    }

    @NonNull
    private static Map<String, Object> resolveContextVariables(
            IExpressionContext context,
            StandardExpressionExecutionContext exeContext,
            @NonNull ComputedOgnlExpression parsedExpression) {
        Map<String, Object> contextVariables;
        if (parsedExpression.mightNeedExpressionObjects) {

            // The IExpressionObjects implementation returned by processing contexts that include the Standard
            // Dialects will be lazy in the creation of expression objects (i.e. they won't be created until really
            // needed). And in order for this behaviour to be accepted by OGNL, we will be wrapping this object
            // inside an implementation of Map<String,Object>, which will afterwards be fed to the constructor
            // of an OgnlContext object.

            // Note this will never happen with shortcut expressions, as the '#' character with which all
            // expression object names start is not allowed by the OgnlShortcutExpression parser.

            contextVariables = new OgnlExpressionObjectsWrapper(context.getExpressionObjects());

            // We might need to apply restrictions on the request parameters. In the case of OGNL, the only way we
            // can actually communicate with the PropertyAccessor, (OGNLVariablesMapPropertyAccessor), which is the
            // agent in charge of applying such restrictions, is by adding a context variable that the property accessor
            // can later lookup during evaluation.
            if (exeContext.getRestrictVariableAccess()) {
                contextVariables.put(RESTRICT_REQUEST_PARAMETERS, RESTRICT_REQUEST_PARAMETERS);
            } else {
                contextVariables.remove(RESTRICT_REQUEST_PARAMETERS);
            }
        } else {
            if (exeContext.getRestrictVariableAccess()) {
                contextVariables = CONTEXT_VARIABLES_MAP_NOEXPOBJECTS_RESTRICTIONS;
            } else {
                contextVariables = Collections.emptyMap();
            }
        }
        return contextVariables;
    }

    private static ComputedOgnlExpression obtainComputedOgnlExpression(
            IEngineConfiguration configuration,
            IStandardVariableExpression expression, String expStr,
            @NonNull StandardExpressionExecutionContext exeContext,
            boolean applyOgnlShortcuts) throws OgnlException {
        // If restrictions apply, we want to avoid applying shortcuts so that we delegate to OGNL validation
        // of method calls and references to allowed classes.
        boolean doApplyOgnlShortcuts = applyOgnlShortcuts &&
                !exeContext.getRestrictVariableAccess() && !exeContext.getRestrictInstantiationAndStatic();

        if (exeContext.getRestrictInstantiationAndStatic() &&
                StandardExpressionUtils.containsOGNLInstantiationOrStaticOrParam(expStr)) {
            throw new TemplateProcessingException(
                "Instantiation of new objects and access to static classes or parameters is forbidden in this context");
        }

        if (expression instanceof VariableExpression ve) {
            Object cachedExpression = ve.getCachedExpression();
            if (cachedExpression instanceof ComputedOgnlExpression coe) {
                return coe;
            }
            cachedExpression = parseComputedOgnlExpression(configuration, expStr, doApplyOgnlShortcuts);
            ve.setCachedExpression(cachedExpression);
            return (ComputedOgnlExpression)cachedExpression;
        }

        if (expression instanceof SelectionVariableExpression sve) {
            Object cachedExpression = sve.getCachedExpression();
            if (cachedExpression instanceof ComputedOgnlExpression ognlExpression) {
                return ognlExpression;
            }
            cachedExpression = parseComputedOgnlExpression(configuration, expStr, doApplyOgnlShortcuts);
            sve.setCachedExpression(cachedExpression);
            return (ComputedOgnlExpression)cachedExpression;
        }

        return parseComputedOgnlExpression(configuration, expStr, doApplyOgnlShortcuts);
    }

    @NonNull
    private static ComputedOgnlExpression parseComputedOgnlExpression(
            IEngineConfiguration configuration, String expStr,
            boolean applyOGNLShortcuts) throws OgnlException {
        ComputedOgnlExpression parsedExpression =
                ExpressionCache.getFromCache(configuration, expStr, EXPRESSION_CACHE_TYPE_OGNL);
        if (parsedExpression != null) {
            return parsedExpression;
        }
        // The result of parsing might be an OGNL expression AST or a ShortcutOGNLExpression (for simple cases)
        parsedExpression = parseExpression(expStr, applyOGNLShortcuts);
        ExpressionCache.putIntoCache(configuration, expStr, parsedExpression, EXPRESSION_CACHE_TYPE_OGNL);
        return parsedExpression;
    }

    private static void invalidateComputedOgnlExpression(
            IEngineConfiguration configuration, IStandardVariableExpression expression, String expStr) {
        if (expression instanceof VariableExpression ve) {
            ve.setCachedExpression(null);
        } else if (expression instanceof SelectionVariableExpression ve) {
            ve.setCachedExpression(null);
        }
        ExpressionCache.removeFromCache(configuration, expStr, EXPRESSION_CACHE_TYPE_OGNL);
    }

    @NonNull
    private static ComputedOgnlExpression parseExpression(String expression, boolean applyOgnlShortcuts)
            throws OgnlException {
        boolean mightNeedExpressionObjects = StandardExpressionUtils.mightNeedExpressionObjects(expression);
        if (applyOgnlShortcuts) {
            String[] expressions = OgnlShortcutExpression.parse(expression);
            if (expressions != null) {
                OgnlShortcutExpression ose = new OgnlShortcutExpression(expressions);
                return new ComputedOgnlExpression(ose, mightNeedExpressionObjects);
            }
        }
        Object parsedExpression = Ognl.parseExpression(expression);
        return new ComputedOgnlExpression(parsedExpression, mightNeedExpressionObjects);
    }

    private static Object executeExpression(
            IEngineConfiguration configuration, Object parsedExpression,
            Map<String, Object> contextVariables, Object root) throws Exception {
        if (parsedExpression instanceof OgnlShortcutExpression ose) {
            return ose.evaluate(configuration, contextVariables, root);
        }

        // We create the OgnlContext here instead of just sending the Map as context because that prevents OGNL from
        // creating the OgnlContext empty and then setting the context Map variables one by one
        OgnlContext ognlContext = OgnlSupport.createDefaultContext(contextVariables);
        return Ognl.getValue(parsedExpression, ognlContext, root);
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
