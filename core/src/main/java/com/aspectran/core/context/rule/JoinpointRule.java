/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.params.JoinpointParameters;
import com.aspectran.core.context.rule.params.PointcutParameters;
import com.aspectran.core.context.rule.params.PointcutQualifierParameters;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a join point, the specific point in the application where an aspect's advice can be applied.
 * It specifies the target type (e.g., a translet or a bean's method) and can include a pointcut
 * to precisely match specific execution points.
 *
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint&gt;
 *     methods: [
 *       GET
 *       POST
 *       PATCH
 *       PUT
 *       DELETE
 *     ]
 *     headers: [
 *       Origin
 *     ]
 *     pointcut: {
 *       type: wildcard
 *       +: /a/b@sample.bean1^method1
 *       +: /x/y@sample.bean2^method1
 *       -: /a/b/c@sample.bean3^method1
 *       -: /x/y/z@sample.bean4^method1
 *     }
 *     pointcut: {
 *       type: regexp
 *       include: {
 *         translet: /a/b
 *         bean: sample.bean1
 *         method: method1
 *       }
 *       exclude: {
 *         translet: /a/b/c
 *         bean: sample.bean3
 *         method: method1
 *       }
 *     }
 *   &lt;/joinpoint&gt;
 *   &lt;settings&gt;
 *   &lt;/settings&gt;
 *   &lt;advice&gt;
 *   &lt;/advice&gt;
 *   &lt;exception&gt;
 *   &lt;/exception&gt;
 * &lt;aspect&gt;
 * </pre>
 */
public class JoinpointRule {

    private JoinpointTargetType joinpointTargetType;

    private MethodType[] methods;

    private String[] headers;

    private PointcutRule pointcutRule;

    private JoinpointParameters joinpointParameters;

    /**
     * Gets the join point target type.
     * @return the join point target type
     */
    public JoinpointTargetType getJoinpointTargetType() {
        return joinpointTargetType;
    }

    /**
     * Sets the join point target type.
     * @param joinpointTargetType the join point target type
     */
    public void setJoinpointTargetType(JoinpointTargetType joinpointTargetType) {
        this.joinpointTargetType = joinpointTargetType;
        if (joinpointParameters != null) {
            if (joinpointTargetType != null) {
                joinpointParameters.putValue(JoinpointParameters.target, joinpointTargetType.toString());
            } else {
                joinpointParameters.removeValue(JoinpointParameters.target);
            }
        }
    }

    /**
     * Gets the allowed HTTP methods for the join point.
     * @return an array of allowed methods
     */
    public MethodType[] getMethods() {
        return methods;
    }

    /**
     * Sets the allowed HTTP methods for the join point.
     * @param methods an array of allowed methods
     */
    public void setMethods(MethodType[] methods) {
        this.methods = methods;
    }

    /**
     * Gets the required HTTP headers for the join point.
     * @return an array of required headers
     */
    public String[] getHeaders() {
        return headers;
    }

    /**
     * Sets the required HTTP headers for the join point.
     * @param headers an array of required headers
     */
    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    /**
     * Gets the pointcut rule.
     * @return the pointcut rule
     */
    public PointcutRule getPointcutRule() {
        return pointcutRule;
    }

    /**
     * Sets the pointcut rule.
     * @param pointcutRule the pointcut rule
     */
    public void setPointcutRule(PointcutRule pointcutRule) {
        this.pointcutRule = pointcutRule;
    }

    /**
     * Gets the joinpoint parameters used for its creation.
     * @return the joinpoint parameters
     */
    public JoinpointParameters getJoinpointParameters() {
        return joinpointParameters;
    }

    private void setJoinpointParameters(JoinpointParameters joinpointParameters) {
        this.joinpointParameters = joinpointParameters;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("target", joinpointTargetType);
        tsb.append("methods", methods);
        tsb.append("headers", headers);
        tsb.append("pointcut", pointcutRule);
        return tsb.toString();
    }

    /**
     * Creates a new instance of JoinpointRule.
     * @return a new JoinpointRule instance
     */
    @NonNull
    public static JoinpointRule newInstance() {
        return new JoinpointRule();
    }

    static void updateJoinpoint(JoinpointRule joinpointRule, String apon)
            throws IllegalRuleException {
        if (StringUtils.hasText(apon)) {
            JoinpointParameters joinpointParameters;
            try {
                joinpointParameters = new JoinpointParameters(apon);
            } catch (IOException e) {
                throw new IllegalRuleException("Joinpoint parameter can not be parsed", e);
            }
            updateJoinpoint(joinpointRule, joinpointParameters);
        }
    }

    static void updateJoinpoint(@NonNull JoinpointRule joinpointRule, JoinpointParameters joinpointParameters)
            throws IllegalRuleException {
        if (joinpointRule.getJoinpointTargetType() == null) {
            String target = joinpointParameters.getString(JoinpointParameters.target);
            updateJoinpointTargetType(joinpointRule, target);
        }
        updateMethods(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.methods));
        updateHeaders(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.headers));
        updatePointcutRule(joinpointRule, joinpointParameters.getParameters(JoinpointParameters.pointcut));
        joinpointRule.setJoinpointParameters(joinpointParameters);
    }

    static void updateJoinpointTargetType(@NonNull JoinpointRule joinpointRule, String target) {
        if (target != null) {
            JoinpointTargetType joinpointTargetType = JoinpointTargetType.resolve(target);
            if (joinpointTargetType == null) {
                throw new IllegalArgumentException("No joinpoint target type for '" + target + "'");
            }
            joinpointRule.setJoinpointTargetType(joinpointTargetType);
        }
    }

    /**
     * A static helper method to update the allowed methods from an array of strings.
     * @param joinpointRule the joinpoint rule to update
     * @param methods an array of method strings
     */
    public static void updateMethods(@NonNull JoinpointRule joinpointRule, String[] methods) {
        if (methods != null && methods.length > 0) {
            List<MethodType> methodTypes = new ArrayList<>(methods.length);
            for (String method : methods) {
                MethodType methodType = MethodType.resolve(method);
                if (methodType == null) {
                    throw new IllegalArgumentException("No request method type for '" + method + "'");
                }
                methodTypes.add(methodType);
            }
            joinpointRule.setMethods(methodTypes.toArray(new MethodType[0]));
        }
    }

    /**
     * A static helper method to update the required headers from an array of strings.
     * @param joinpointRule the joinpoint rule to update
     * @param headers an array of header strings
     */
    public static void updateHeaders(@NonNull JoinpointRule joinpointRule, String[] headers) {
        if (headers != null && headers.length > 0) {
            List<String> headerList = new ArrayList<>(headers.length);
            for (String header : headers) {
                if (StringUtils.hasText(header)) {
                    headerList.add(header);
                }
            }
            if (!headerList.isEmpty()) {
                joinpointRule.setHeaders(headerList.toArray(new String[0]));
            }
        }
    }

    /**
     * A static helper method to update the pointcut rule from parameters.
     * @param joinpointRule the joinpoint rule to update
     * @param pointcutParameters the pointcut parameters
     * @throws IllegalRuleException if the rule is invalid
     */
    public static void updatePointcutRule(@NonNull JoinpointRule joinpointRule, PointcutParameters pointcutParameters)
            throws IllegalRuleException {
        if (pointcutParameters == null) {
            return;
        }

        List<String> plusPatternStringList = pointcutParameters.getStringList(PointcutParameters.plus);
        List<String> minusPatternStringList = pointcutParameters.getStringList(PointcutParameters.minus);
        List<PointcutQualifierParameters> includeQualifierParametersList = pointcutParameters.getParametersList(PointcutParameters.include);
        List<PointcutQualifierParameters> excludeQualifierParametersList = pointcutParameters.getParametersList(PointcutParameters.exclude);

        List<PointcutPatternRule> pointcutPatternRuleList = mergePointcutPatternRules(plusPatternStringList, includeQualifierParametersList);
        List<PointcutPatternRule> excludePointcutPatternRuleList = mergePointcutPatternRules(minusPatternStringList, excludeQualifierParametersList);
        if (pointcutPatternRuleList == null && excludePointcutPatternRuleList == null) {
            return;
        }

        PointcutRule pointcutRule = PointcutRule.newInstance(pointcutParameters.getString(PointcutParameters.type));
        if (pointcutPatternRuleList != null) {
            for (PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
                pointcutPatternRule.setExcludePatternRuleList(excludePointcutPatternRuleList);
                pointcutRule.addPointcutPatternRule(pointcutPatternRule);
            }
        } else {
            PointcutPatternRule pointcutPatternRule = new PointcutPatternRule();
            pointcutPatternRule.setExcludePatternRuleList(excludePointcutPatternRuleList);
            pointcutRule.addPointcutPatternRule(pointcutPatternRule);
        }
        joinpointRule.setPointcutRule(pointcutRule);
    }

    @Nullable
    private static List<PointcutPatternRule> mergePointcutPatternRules(
            List<String> patternStringList, List<PointcutQualifierParameters> qualifierParametersList) {
        int patternStringSize = (patternStringList != null ? patternStringList.size() : 0);
        int qualifierParametersSize = (qualifierParametersList != null ? qualifierParametersList.size() : 0);
        if (patternStringSize == 0 && qualifierParametersSize == 0) {
            return null;
        }
        List<PointcutPatternRule> pointcutPatternRuleList = new ArrayList<>(patternStringSize + qualifierParametersSize);
        if (patternStringSize > 0) {
            for (String patternString : patternStringList) {
                PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(patternString);
                pointcutPatternRuleList.add(pointcutPatternRule);
            }
        }
        if (qualifierParametersSize > 0) {
            for (PointcutQualifierParameters qualifierParameters : qualifierParametersList) {
                PointcutPatternRule pointcutPatternRule = createPointcutPatternRule(qualifierParameters);
                pointcutPatternRuleList.add(pointcutPatternRule);
            }
        }
        return pointcutPatternRuleList;
    }

    private static PointcutPatternRule createPointcutPatternRule(@NonNull PointcutQualifierParameters qualifierParameters) {
        PointcutPatternRule pointcutPatternRule = null;
        String translet = qualifierParameters.getString(PointcutQualifierParameters.translet);
        String bean = qualifierParameters.getString(PointcutQualifierParameters.bean);
        String method = qualifierParameters.getString(PointcutQualifierParameters.method);
        if (StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
            pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
        }
        return pointcutPatternRule;
    }

}
