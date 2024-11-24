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
//import org.thymeleaf.standard.expression.ExpressionCache;
//import org.thymeleaf.standard.expression.IStandardConversionService;
//import org.thymeleaf.standard.expression.IStandardVariableExpression;
//import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
//import org.thymeleaf.standard.expression.OGNLContextPropertyAccessor;
//import org.thymeleaf.standard.expression.OGNLExpressionObjectsWrapper;
//import org.thymeleaf.standard.expression.OGNLShortcutExpression;
import org.thymeleaf.standard.expression.SelectionVariableExpression;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.expression.VariableExpression;
import org.thymeleaf.standard.util.StandardExpressionUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Evaluator for variable expressions ({@code ${...}}) in Thymeleaf Standard Expressions, using the
 * Aspectran expression language.
 *
 * <p>Created: 2024. 11. 23.</p>
 */
public class ASELVariableExpressionEvaluator { //implements IStandardVariableExpressionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ASELVariableExpressionEvaluator.class);

    private static final String EXPRESSION_CACHE_TYPE_OGNL = "asel";

//    private static Map<String,Object> CONTEXT_VARIABLES_MAP_NOEXPOBJECTS_RESTRICTIONS =
//        Collections.singletonMap(
//            OGNLContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS,
//            OGNLContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS);

    private final boolean applyOGNLShortcuts;

    public ASELVariableExpressionEvaluator(boolean applyOGNLShortcuts) {
        super();
        this.applyOGNLShortcuts = applyOGNLShortcuts;

//        /*
//         * INITIALIZE AND REGISTER THE PROPERTY ACCESSOR
//         */
//        OGNLContextPropertyAccessor accessor = new OGNLContextPropertyAccessor();
//        OgnlRuntime.setPropertyAccessor(IContext.class, accessor);
    }

//    public final Object evaluate(IExpressionContext context,
//                                 IStandardVariableExpression expression,
//                                 StandardExpressionExecutionContext expContext) {
//        return evaluate(context, expression, expContext, this.applyOGNLShortcuts);
//    }
//
//    private static Object evaluate(IExpressionContext context,
//                                   IStandardVariableExpression expression,
//                                   StandardExpressionExecutionContext expContext,
//                                   boolean applyOGNLShortcuts) {
//        if (logger.isTraceEnabled()) {
//            logger.trace("[THYMELEAF][" + TemplateEngine.threadIndex() +
//                "] SpringEL expression: evaluating expression \"" + expression.getExpression() + "\" on target");
//        }
//
//        try {
//            IEngineConfiguration configuration = context.getConfiguration();
//
//            String exp = expression.getExpression();
//            boolean useSelectionAsRoot = expression.getUseSelectionAsRoot();
//
//            if (exp == null) {
//                throw new TemplateProcessingException("Expression content is null, which is not allowed");
//            }
//
//            ComputedOGNLExpression parsedExpression =
//                obtainComputedOGNLExpression(configuration, expression, exp, expContext, applyOGNLShortcuts);
//
//            Map<String, Object> contextVariablesMap;
//            if (parsedExpression.mightNeedExpressionObjects) {
//
//                // The IExpressionObjects implementation returned by processing contexts that include the Standard
//                // Dialects will be lazy in the creation of expression objects (i.e. they won't be created until really
//                // needed). And in order for this behaviour to be accepted by OGNL, we will be wrapping this object
//                // inside an implementation of Map<String,Object>, which will afterwards be fed to the constructor
//                // of an OgnlContext object.
//
//                // Note this will never happen with shortcut expressions, as the '#' character with which all
//                // expression object names start is not allowed by the OGNLShortcutExpression parser.
//
//                contextVariablesMap = new OGNLExpressionObjectsWrapper(context.getExpressionObjects());
//
//                // We might need to apply restrictions on the request parameters. In the case of OGNL, the only way we
//                // can actually communicate with the PropertyAccessor, (OGNLVariablesMapPropertyAccessor), which is the
//                // agent in charge of applying such restrictions, is by adding a context variable that the property accessor
//                // can later lookup during evaluation.
//                if (expContext.getRestrictVariableAccess()) {
//                    contextVariablesMap.put(OGNLContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS, OGNLContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS);
//                } else {
//                    contextVariablesMap.remove(OGNLContextPropertyAccessor.RESTRICT_REQUEST_PARAMETERS);
//                }
//            } else {
//                if (expContext.getRestrictVariableAccess()) {
//                    contextVariablesMap = CONTEXT_VARIABLES_MAP_NOEXPOBJECTS_RESTRICTIONS;
//                } else {
//                    contextVariablesMap = Collections.EMPTY_MAP;
//                }
//            }
//
//            // The root object on which we will evaluate expressions will depend on whether a selection target is
//            // active or not...
//            ITemplateContext templateContext = (context instanceof ITemplateContext ? (ITemplateContext) context : null);
//            Object evaluationRoot = (useSelectionAsRoot && templateContext != null && templateContext.hasSelectionTarget()
//                    ? templateContext.getSelectionTarget() : templateContext);
//
//            // Execute the expression!
//            Object result;
//            try {
//                result = executeExpression(configuration, parsedExpression.expression, contextVariablesMap, evaluationRoot);
//            } catch (OGNLShortcutExpression.OGNLShortcutExpressionNotApplicableException notApplicable) {
//                // We tried to apply shortcuts, but it is not possible for this expression even if it parsed OK,
//                // so we need to empty the cache and try again disabling shortcuts. Once processed for the first time,
//                // an OGNL (non-shortcut) parsed expression will already be cached and this exception will not be
//                // thrown again
//                invalidateComputedOGNLExpression(configuration, expression, exp);
//                return evaluate(context, expression, expContext, false);
//            }
//
//            if (!expContext.getPerformTypeConversion()) {
//                return result;
//            }
//
//            IStandardConversionService conversionService = StandardExpressions.getConversionService(configuration);
//            return conversionService.convert(context, result, String.class);
//        } catch (Exception e) {
//            throw new TemplateProcessingException(
//                "Exception evaluating OGNL expression: \"" + expression.getExpression() + "\"", e);
//        }
//    }
//
//    private static ComputedOGNLExpression obtainComputedOGNLExpression(
//            IEngineConfiguration configuration,
//            IStandardVariableExpression expression, String exp,
//            StandardExpressionExecutionContext expContext,
//            boolean applyOGNLShortcuts) throws OgnlException {
//        // If restrictions apply, we want to avoid applying shortcuts so that we delegate to OGNL validation
//        // of method calls and references to allowed classes.
//        boolean doApplyOGNLShortcuts =
//            applyOGNLShortcuts &&
//                !expContext.getRestrictVariableAccess() && !expContext.getRestrictInstantiationAndStatic();
//
//        if (expContext.getRestrictInstantiationAndStatic()
//            && StandardExpressionUtils.containsOGNLInstantiationOrStaticOrParam(exp)) {
//            throw new TemplateProcessingException(
//                "Instantiation of new objects and access to static classes or parameters is forbidden in this context");
//        }
//
//        if (expression instanceof VariableExpression vexpression) {
//            Object cachedExpression = vexpression.getCachedExpression();
//            if (cachedExpression instanceof ComputedOGNLExpression computedOGNLExpression) {
//                return computedOGNLExpression;
//            }
//            cachedExpression = parseComputedOGNLExpression(configuration, exp, doApplyOGNLShortcuts);
//            vexpression.setCachedExpression(cachedExpression);
//            return (ComputedOGNLExpression)cachedExpression;
//        }
//
//        if (expression instanceof SelectionVariableExpression vexpression) {
//            Object cachedExpression = vexpression.getCachedExpression();
//            if (cachedExpression instanceof ComputedOGNLExpression computedOGNLExpression) {
//                return computedOGNLExpression;
//            }
//            cachedExpression = parseComputedOGNLExpression(configuration, exp, doApplyOGNLShortcuts);
//            vexpression.setCachedExpression(cachedExpression);
//            return (ComputedOGNLExpression)cachedExpression;
//        }
//
//        return parseComputedOGNLExpression(configuration, exp, doApplyOGNLShortcuts);
//    }
//
//    @NonNull
//    private static ComputedOGNLExpression parseComputedOGNLExpression(
//            IEngineConfiguration configuration, String exp,
//            boolean applyOGNLShortcuts)
//            throws OgnlException {
//        ComputedOGNLExpression parsedExpression =
//            (ComputedOGNLExpression) ExpressionCache.getFromCache(configuration, exp, EXPRESSION_CACHE_TYPE_OGNL);
//        if (parsedExpression != null) {
//            return parsedExpression;
//        }
//        // The result of parsing might be an OGNL expression AST or a ShortcutOGNLExpression (for simple cases)
//        parsedExpression = parseExpression(exp, applyOGNLShortcuts);
//        ExpressionCache.putIntoCache(configuration, exp, parsedExpression, EXPRESSION_CACHE_TYPE_OGNL);
//        return parsedExpression;
//    }
//
//    private static void invalidateComputedOGNLExpression(
//        IEngineConfiguration configuration, IStandardVariableExpression expression, String exp) {
//        if (expression instanceof VariableExpression vexpression) {
//            vexpression.setCachedExpression(null);
//        } else if (expression instanceof SelectionVariableExpression vexpression) {
//            vexpression.setCachedExpression(null);
//        }
//        ExpressionCache.removeFromCache(configuration, exp, EXPRESSION_CACHE_TYPE_OGNL);
//    }
//
//    @Override
//    public String toString() {
//        return "AspectranEL";
//    }
//
//    @NonNull
//    private static ComputedOGNLExpression parseExpression(String expression, boolean applyOGNLShortcuts)
//            throws OgnlException {
//        boolean mightNeedExpressionObjects = StandardExpressionUtils.mightNeedExpressionObjects(expression);
//        if (applyOGNLShortcuts) {
//            String[] parsedExpression = OGNLShortcutExpression.parse(expression);
//            if (parsedExpression != null) {
//                return new ComputedOGNLExpression(new OGNLShortcutExpression(parsedExpression), mightNeedExpressionObjects);
//            }
//        }
//        Object parsedExpression = Ognl.parseExpression(expression);
//        return new ComputedOGNLExpression(parsedExpression, mightNeedExpressionObjects);
//    }
//
//    private static Object executeExpression(IEngineConfiguration configuration, Object parsedExpression,
//                                            Map<String, Object> context, Object root) throws Exception {
//        if (parsedExpression instanceof OGNLShortcutExpression shortcutExpression) {
//            return shortcutExpression.evaluate(configuration, context, root);
//        }
//
//        // We create the OgnlContext here instead of just sending the Map as context because that prevents OGNL from
//        // creating the OgnlContext empty and then setting the context Map variables one by one
//        OgnlContext ognlContext = OgnlSupport.createDefaultContext();
//        ognlContext.putAll(context);
//        return Ognl.getValue(parsedExpression, ognlContext, root);
//    }
//
//    private static class ComputedOGNLExpression {
//
//        Object expression;
//        boolean mightNeedExpressionObjects;
//
//        ComputedOGNLExpression(Object expression, boolean mightNeedExpressionObjects) {
//            this.expression = expression;
//            this.mightNeedExpressionObjects = mightNeedExpressionObjects;
//        }
//
//    }

}
