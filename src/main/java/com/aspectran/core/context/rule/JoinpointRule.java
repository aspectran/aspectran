/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.PointcutParameters;
import com.aspectran.core.context.builder.apon.params.PointcutTargetParameters;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class AspectRule.
 * 
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint scope="translet"&gt;
 *     methods: [
 *       "GET"
 *       "POST"
 *       "PATCH"
 *       "PUT"
 *       "DELETE"
 *     ]
 *     headers: [
 * 	     "Origin"
 *     ]
 *     pointcut: {
 * 	     type: "wildcard"
 * 	     +: "/a/b@sample.bean1^method1"
 * 	     +: "/x/y@sample.bean2^method1"
 * 	     -: "/a/b/c@sample.bean3^method1"
 * 	     -: "/x/y/z@sample.bean4^method1"
 *     }
 *     pointcut: {
 * 	     type: "regexp"
 * 	     include: {
 *         translet: "/a/b"
 *         bean: "sample.bean1"
 *         method: "method1"
 *       }
 *       execlude: {
 * 	       translet: "/a/b/c"
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

	private JoinpointType joinpointType;
	
	private MethodType[] targetMethods;
	
	private String[] targetHeaders;
	
	private PointcutRule pointcutRule;

	private Parameters joinpointParameters;

	public JoinpointType getJoinpointType() {
		return joinpointType;
	}

	public void setJoinpointType(JoinpointType joinpointType) {
		this.joinpointType = joinpointType;
		if(joinpointParameters != null) {
			if(joinpointType != null) { 
				joinpointParameters.putValue(JoinpointParameters.type, joinpointType.toString());
			} else {
				joinpointParameters.putValue(JoinpointParameters.type, null);
			}
		}
	}

	public MethodType[] getTargetMethods() {
		return targetMethods;
	}

	public void setTargetMethods(MethodType[] targetMethods) {
		this.targetMethods = targetMethods;
	}

	public String[] getTargetHeaders() {
		return targetHeaders;
	}

	public void setTargetHeaders(String[] targetHeaders) {
		this.targetHeaders = targetHeaders;
	}

	public PointcutRule getPointcutRule() {
		return pointcutRule;
	}

	public void setPointcutRule(PointcutRule pointcutRule) {
		this.pointcutRule = pointcutRule;
	}

	public Parameters getJoinpointParameters() {
		return joinpointParameters;
	}

	public void setJoinpointParameters(Parameters joinpointParameters) {
		this.joinpointParameters = joinpointParameters;

		updateJoinpointType(this, joinpointParameters.getString(JoinpointParameters.type));
		updateTargetMethods(this, joinpointParameters.getStringArray(JoinpointParameters.methods));
		updateTargetHeaders(this, joinpointParameters.getStringArray(JoinpointParameters.headers));
		updatePointcutRule(this, joinpointParameters.getParameters(JoinpointParameters.pointcut));
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("joinpointType", joinpointType);
		tsb.append("targetMethods", targetMethods);
		tsb.append("targetHeaders", targetHeaders);
		tsb.append("pointcutRule", pointcutRule);
		return tsb.toString();
	}

	public static JoinpointRule newInstance() {
		JoinpointRule joinpointRule = new JoinpointRule();
		return joinpointRule;
	}
	
	public static void updateJoinpoint(JoinpointRule joinpointRule, String text) {
		if(StringUtils.hasText(text)) {
			Parameters joinpointParameters = new JoinpointParameters(text);
			updateJoinpoint(joinpointRule, joinpointParameters);
		}
	}

	public static void updateJoinpoint(JoinpointRule joinpointRule, Parameters joinpointParameters) {
		String type = joinpointParameters.getString(JoinpointParameters.type);
		if(joinpointRule.getJoinpointType() == null) {
			updateJoinpointType(joinpointRule, type);
		}
		updateTargetMethods(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.methods));
		updateTargetHeaders(joinpointRule, joinpointParameters.getStringArray(JoinpointParameters.headers));
		joinpointRule.setJoinpointParameters(joinpointParameters);
	}
	
	public static void updateJoinpointType(JoinpointRule joinpointRule, String type) {
		JoinpointType joinpointType;
		if(type != null) {
			joinpointType = JoinpointType.resolve(type);
			if(joinpointType == null)
				throw new IllegalArgumentException("No joinpoint type registered for '" + type + "'.");
		} else {
			joinpointType = JoinpointType.TRANSLET;
		}
		joinpointRule.setJoinpointType(joinpointType);
	}

	public static void updateTargetMethods(JoinpointRule joinpointRule, String[] methods) {
		MethodType[] targetMethods = null;
		if(methods != null) {
			List<MethodType> methodTypes = new ArrayList<>(methods.length);
			for(String method : methods) {
				MethodType methodType = MethodType.resolve(method);
				if(methodType == null) {
					throw new IllegalArgumentException("No request method type registered for '" + method + "'.");
				}
				methodTypes.add(methodType);
			}
			targetMethods = methodTypes.toArray(new MethodType[methodTypes.size()]);
		}
		joinpointRule.setTargetMethods(targetMethods);
	}
	
	public static void updateTargetHeaders(JoinpointRule joinpointRule, String[] headers) {
		String[] targetHeaders = null;
		if(headers != null) {
			List<String> headerList = new ArrayList<>(headers.length);
			for(String header : headers) {
				if(StringUtils.hasText(header)) {
					headerList.add(header);
				}
			}
			targetHeaders = headerList.toArray(new String[headerList.size()]);
		}
		joinpointRule.setTargetHeaders(targetHeaders);
	}
	
	public static void updatePointcutRule(JoinpointRule joinpointRule, Parameters pointcutParameters) {
		PointcutRule pointcutRule = null;
		
		if(pointcutParameters != null) {
			List<String> plusPatternStringList = pointcutParameters.getStringList(PointcutParameters.pluses);
			List<String> minusPatternStringList = pointcutParameters.getStringList(PointcutParameters.minuses);
			List<Parameters> includeTargetParametersList = pointcutParameters.getParametersList(PointcutParameters.includes);
			List<Parameters> execludeTargetParametersList = pointcutParameters.getParametersList(PointcutParameters.execludes);

			int patternStringCount = (plusPatternStringList != null) ? plusPatternStringList.size() : 0;
			int targetParametersCount = (includeTargetParametersList != null) ? includeTargetParametersList.size() : 0;
			
			if(patternStringCount > 0 || targetParametersCount > 0) {
				pointcutRule = PointcutRule.newInstance(pointcutParameters.getString(PointcutParameters.type));
				pointcutRule.newPointcutPatternRuleList(patternStringCount + targetParametersCount);
	
				if(patternStringCount > 0) {
					List<PointcutPatternRule> minusPointcutPatternRuleList = null;
					if(minusPatternStringList != null && !minusPatternStringList.isEmpty()) {
						minusPointcutPatternRuleList = new ArrayList<PointcutPatternRule>(minusPatternStringList.size());
						for(String patternString : minusPatternStringList) {
							PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
							minusPointcutPatternRuleList.add(pointcutPatternRule);
						}
					}
					for(String patternString : plusPatternStringList) {
						PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
						pointcutRule.addPointcutPatternRule(pointcutPatternRule, minusPointcutPatternRuleList);
					}

					pointcutRule.setPlusPatternStringList(plusPatternStringList);
					pointcutRule.setMinusPatternStringList(minusPatternStringList);
				}
				
				if(targetParametersCount > 0) {
					List<PointcutPatternRule> excludePointcutPatternRuleList = null;
					if(execludeTargetParametersList != null && !execludeTargetParametersList.isEmpty()) {
						excludePointcutPatternRuleList = new ArrayList<PointcutPatternRule>(execludeTargetParametersList.size());
						for(Parameters excludeTargetParameters : execludeTargetParametersList) {
							PointcutPatternRule pointcutPatternRule = createPointcutPatternRule(excludeTargetParameters);
							if(pointcutPatternRule != null) {
								excludePointcutPatternRuleList.add(pointcutPatternRule);
							}
						}
					}
					for(Parameters includeTargetParameters : includeTargetParametersList) {
						PointcutPatternRule pointcutPatternRule = createPointcutPatternRule(includeTargetParameters);
						pointcutRule.addPointcutPatternRule(pointcutPatternRule, excludePointcutPatternRuleList);
					}

					pointcutRule.setIncludeTargetParametersList(includeTargetParametersList);
					pointcutRule.setExecludeTargetParametersList(execludeTargetParametersList);
				}
			}
		}
		
		joinpointRule.setPointcutRule(pointcutRule);
	}
	
	private static PointcutPatternRule createPointcutPatternRule(Parameters excludeTargetParameters) {
		PointcutPatternRule pointcutPatternRule = null;
		String translet = excludeTargetParameters.getString(PointcutTargetParameters.translet);
		String bean = excludeTargetParameters.getString(PointcutTargetParameters.bean);
		String method = excludeTargetParameters.getString(PointcutTargetParameters.method);
		if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
			pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
		}
		return pointcutPatternRule;
	}

}
