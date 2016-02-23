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
package com.aspectran.core.context.builder.xml;

import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.AspectJobAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class AspectNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
public class AspectNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new AspectNodeletAdder.
	 *
	 * @param assistant the assistant
	 */
	public AspectNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/aspect", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String id = StringUtils.emptyToNull(attributes.get("id"));
				String useFor = StringUtils.emptyToNull(attributes.get("for"));

				if(id == null)
					throw new IllegalArgumentException("The <aspect> element requires an id attribute.");

				AspectRule aspectRule = AspectRule.newInstance(id, useFor);
				
				assistant.pushObject(aspectRule);
			}
		});
		parser.addNodelet(xpath, "/aspect/description", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				if(text != null) {
					AspectRule aspectRule = assistant.peekObject();
					aspectRule.setDescription(text);
				}
			}
		});
		parser.addNodelet(xpath, "/aspect/joinpoint", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String scope = StringUtils.emptyToNull(attributes.get("scope"));

				AspectRule aspectRule = assistant.peekObject();
				AspectRule.updateJoinpointScope(aspectRule, scope);
			}
		});
		parser.addNodelet(xpath, "/aspect/joinpoint/pointcut", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String type = StringUtils.emptyToNull(attributes.get("type"));
				
				AspectRule aspectRule = assistant.peekObject();
				PointcutRule pointcutRule = PointcutRule.newInstance(aspectRule, type, text);
				aspectRule.setPointcutRule(pointcutRule);
			}
		});
/*
		parser.addNodelet(xpath, "/aspect/joinpoint/pointcut/target", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				AspectRule aspectRule = assistant.peekObject();
				
				if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
					List<PointcutPatternRule> pointcutPatternRuleList = PointcutRule.newPointcutPatternRuleList();
					PointcutRule.addPointcutPatternRule(pointcutPatternRuleList, translet, bean, method, text);
					
					assistant.pushObject(pointcutPatternRuleList);
				} else {
					assistant.pushObject(null);
				}
			}
		});
		parser.addNodelet(xpath, "/aspect/joinpoint/pointcut/target/exclude", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String translet = attributes.get("translet");
				String bean = attributes.get("bean");
				String method = attributes.get("method");
				
				List<PointcutPatternRule> pointcutPatternRuleList = assistant.peekObject();
				
				if(pointcutPatternRuleList != null) {
					PointcutRule.addExcludePointcutPatternRule(pointcutPatternRuleList, translet, bean, method);
				}
			}
		});
		parser.addNodelet(xpath, "/aspect/joinpoint/pointcut/target/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				List<PointcutPatternRule> pointcutPatternRuleList = assistant.popObject();
				
				if(pointcutPatternRuleList != null) {
					AspectRule aspectRule = assistant.peekObject();
					aspectRule.getPointcutRule().addPointcutPatternRule(pointcutPatternRuleList);
				}
			}
		});
*/
		parser.addNodelet(xpath, "/aspect/settings", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = assistant.peekObject();
				
				SettingsAdviceRule sar = SettingsAdviceRule.newInstance(aspectRule, text);
				
				assistant.pushObject(sar);
			}
		});
		parser.addNodelet(xpath, "/aspect/settings/setting", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = StringUtils.emptyToNull(attributes.get("name"));
				String value = attributes.get("value");
				
				if(name != null) {
					SettingsAdviceRule sar = assistant.peekObject();
					sar.putSetting(name, value);
				}
			}
		});
		parser.addNodelet(xpath, "/aspect/settings/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				SettingsAdviceRule sar = assistant.popObject();
				AspectRule aspectRule = assistant.peekObject();
				aspectRule.setSettingsAdviceRule(sar);
			}
		});	
		parser.addNodelet(xpath, "/aspect/advice", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanIdOrClass = StringUtils.emptyToNull(attributes.get("bean"));
				
				if(beanIdOrClass != null) {
					AspectRule aspectRule = assistant.peekObject();
					aspectRule.setAdviceBeanId(beanIdOrClass);

					Class<?> adviceBeanClass = assistant.resolveBeanClass(beanIdOrClass);
					if(adviceBeanClass != null) {
						aspectRule.setAdviceBeanClass(adviceBeanClass);
						assistant.putBeanReference(adviceBeanClass, aspectRule);
					} else {
						assistant.putBeanReference(beanIdOrClass, aspectRule);
					}
				}
			}
		});
		parser.addNodelet(xpath, "/aspect/advice/before", new AspectAdviceNodeletAdder(assistant, AspectAdviceType.BEFORE));
		parser.addNodelet(xpath, "/aspect/advice/after", new AspectAdviceNodeletAdder(assistant, AspectAdviceType.AFTER));
		parser.addNodelet(xpath, "/aspect/advice/around", new AspectAdviceNodeletAdder(assistant, AspectAdviceType.AROUND));
		parser.addNodelet(xpath, "/aspect/advice/finally", new AspectAdviceNodeletAdder(assistant, AspectAdviceType.FINALLY));
		parser.addNodelet(xpath, "/aspect/exceptionRaised", new AspectExceptionRaisedNodeletAdder(assistant));
		parser.addNodelet(xpath, "/aspect/advice/job", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String transletName = StringUtils.emptyToNull(attributes.get("translet"));
				Boolean disabled = BooleanUtils.toNullableBooleanObject(attributes.get("disabled"));

				transletName = assistant.applyTransletNamePattern(transletName);
				AspectRule ar = assistant.peekObject();

				if(transletName == null)
					throw new IllegalArgumentException("The <job> element requires a translet attribute.");

				AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(ar, transletName, disabled);
				ar.addAspectJobAdviceRule(ajar);
			}
		});
		parser.addNodelet(xpath, "/aspect/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				AspectRule aspectRule = assistant.popObject();
				assistant.addAspectRule(aspectRule);
			}
		});
	}

}