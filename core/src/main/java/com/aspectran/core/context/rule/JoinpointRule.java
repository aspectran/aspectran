/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class JoinpointRule.
 * 
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint target="translet"&gt;
 *     methods: [
 *       "GET"
 *       "POST"
 *       "PATCH"
 *       "PUT"
 *       "DELETE"
 *     ]
 *     headers: [
 *       "Origin"
 *     ]
 *     pointcut: {
 *       type: "wildcard"
 *       +: "/a/b@sample.bean1^method1"
 *       +: "/x/y@sample.bean2^method1"
 *       -: "/a/b/c@sample.bean3^method1"
 *       -: "/x/y/z@sample.bean4^method1"
 *     }
 *     pointcut: {
 *       type: "regexp"
 *       include: {
 *         translet: "/a/b"
 *         bean: "sample.bean1"
 *         method: "method1"
 *       }
 *       exclude: {
 *         translet: "/a/b/c"
 *         bean: "sample.bean3"
 *         method: "method1"
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

    public JoinpointTargetType getJoinpointTargetType() {
        return joinpointTargetType;
    }

    public void setJoinpointTargetType(JoinpointTargetType joinpointTargetType) {
        this.joinpointTargetType = joinpointTargetType;
        if (joinpointParameters != null) {
            if (joinpointTargetType != null) {
                joinpointParameters.putValue(JoinpointParameters.target, joinpointTargetType.toString());
            } else {
                joinpointParameters.putValue(JoinpointParameters.target, null);
            }
        }
    }

    public MethodType[] getMethods() {
        return methods;
    }

    public void setMethods(MethodType[] methods) {
        this.methods = methods;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public PointcutRule getPointcutRule() {
        return pointcutRule;
    }

    public void setPointcutRule(PointcutRule pointcutRule) {
        this.pointcutRule = pointcutRule;
    }

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

    public static JoinpointRule newInstance() {
        return new JoinpointRule();
    }

    public static void updateJoinpoint(JoinpointRule joinpointRule, String text)
            throws IllegalRuleException {
        if (StringUtils.hasText(text)) {
            JoinpointParameters joinpointParameters = new JoinpointParameters(text);
            updateJoinpoint(joinpointRule, joinpointParameters);
        }
    }

    public static void updateJoinpoint(JoinpointRule joinpointRule, JoinpointParameters joinpointParameters)
            throws IllegalRuleException {
        if (joinpointRule.getJoinpointTargetType() == null) {
            String type = joinpointParameters.getString(JoinpointParameters.target);
            updateJoinpointTargetType(joinpointRule, type);
        }
        updateMethods(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.methods));
        updateHeaders(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.headers));
        updatePointcutRule(joinpointRule, joinpointParameters.getParameters(JoinpointParameters.pointcut));
        joinpointRule.setJoinpointParameters(joinpointParameters);
    }

    public static void updateJoinpointTargetType(JoinpointRule joinpointRule, String type) {
        JoinpointTargetType joinpointTargetType;
        if (type != null) {
            joinpointTargetType = JoinpointTargetType.resolve(type);
            if (joinpointTargetType == null) {
                throw new IllegalArgumentException("No joinpoint target type for '" + type + "'");
            }
        } else {
            joinpointTargetType = JoinpointTargetType.TRANSLET;
        }
        joinpointRule.setJoinpointTargetType(joinpointTargetType);
    }

    public static void updateMethods(JoinpointRule joinpointRule, String[] methods) {
        MethodType[] methods2 = null;
        if (methods != null && methods.length > 0) {
            List<MethodType> methodTypes = new ArrayList<>(methods.length);
            for (String method : methods) {
                MethodType methodType = MethodType.resolve(method);
                if (methodType == null) {
                    throw new IllegalArgumentException("No request method type for '" + method + "'");
                }
                methodTypes.add(methodType);
            }
            methods2 = methodTypes.toArray(new MethodType[0]);
        }
        joinpointRule.setMethods(methods2);
    }

    public static void updateHeaders(JoinpointRule joinpointRule, String[] headers) {
        String[] headers2 = null;
        if (headers != null && headers.length > 0) {
            List<String> headerList = new ArrayList<>(headers.length);
            for (String header : headers) {
                if (StringUtils.hasText(header)) {
                    headerList.add(header);
                }
            }
            headers2 = headerList.toArray(new String[0]);
        }
        joinpointRule.setHeaders(headers2);
    }

    public static void updatePointcutRule(JoinpointRule joinpointRule, PointcutParameters pointcutParameters)
            throws IllegalRuleException {
        PointcutRule pointcutRule = null;

        if (pointcutParameters != null) {
            List<String> plusPatternStringList = pointcutParameters.getStringList(PointcutParameters.plus);
            List<String> minusPatternStringList = pointcutParameters.getStringList(PointcutParameters.minus);
            List<PointcutQualifierParameters> includeQualifierParametersList = pointcutParameters.getParametersList(PointcutParameters.include);
            List<PointcutQualifierParameters> excludeQualifierParametersList = pointcutParameters.getParametersList(PointcutParameters.exclude);

            int patternStringSize = (plusPatternStringList != null ? plusPatternStringList.size() : 0);
            int qualifierParametersSize = (includeQualifierParametersList != null ? includeQualifierParametersList.size() : 0);

            if (patternStringSize > 0 || qualifierParametersSize > 0) {
                pointcutRule = PointcutRule.newInstance(pointcutParameters.getString(PointcutParameters.type));
                pointcutRule.newPointcutPatternRuleList(patternStringSize + qualifierParametersSize);

                if (patternStringSize > 0) {
                    List<PointcutPatternRule> minusPointcutPatternRuleList = null;
                    if (minusPatternStringList != null && !minusPatternStringList.isEmpty()) {
                        minusPointcutPatternRuleList = new ArrayList<>(minusPatternStringList.size());
                        for (String patternString : minusPatternStringList) {
                            PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePattern(patternString);
                            minusPointcutPatternRuleList.add(pointcutPatternRule);
                        }
                    }
                    for (String patternString : plusPatternStringList) {
                        PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePattern(patternString);
                        pointcutRule.addPointcutPatternRule(pointcutPatternRule, minusPointcutPatternRuleList);
                    }
                }

                if (qualifierParametersSize > 0) {
                    List<PointcutPatternRule> excludePointcutPatternRuleList = null;
                    if (excludeQualifierParametersList != null && !excludeQualifierParametersList.isEmpty()) {
                        excludePointcutPatternRuleList = new ArrayList<>(excludeQualifierParametersList.size());
                        for (PointcutQualifierParameters excludeTargetParameters : excludeQualifierParametersList) {
                            PointcutPatternRule pointcutPatternRule = createPointcutPatternRule(excludeTargetParameters);
                            if (pointcutPatternRule != null) {
                                excludePointcutPatternRuleList.add(pointcutPatternRule);
                            }
                        }
                    }
                    for (PointcutQualifierParameters includeTargetParameters : includeQualifierParametersList) {
                        PointcutPatternRule pointcutPatternRule = createPointcutPatternRule(includeTargetParameters);
                        pointcutRule.addPointcutPatternRule(pointcutPatternRule, excludePointcutPatternRuleList);
                    }
                }
            }
        }

        joinpointRule.setPointcutRule(pointcutRule);
    }

    private static PointcutPatternRule createPointcutPatternRule(PointcutQualifierParameters pointcutQualifierParameters) {
        PointcutPatternRule pointcutPatternRule = null;
        String translet = pointcutQualifierParameters.getString(PointcutQualifierParameters.translet);
        String bean = pointcutQualifierParameters.getString(PointcutQualifierParameters.bean);
        String method = pointcutQualifierParameters.getString(PointcutQualifierParameters.method);
        if (StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
            pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
        }
        return pointcutPatternRule;
    }

}
