/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ScheduleNodeletAdder.
 * 
 * <p>Created: 2016. 08. 29.</p>
 */
class ScheduleNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/schedule");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));

            ScheduleRule scheduleRule = ScheduleRule.newInstance(id);

            parser.pushObject(scheduleRule);
        });
        parser.addNodeEndlet(text -> {
            ScheduleRule scheduleRule = parser.popObject();
            assistant.addScheduleRule(scheduleRule);
        });
        parser.setXpath(xpath + "/schedule/description");
        parser.addNodelet(attrs -> {
            String profile = attrs.get("profile");
            String style = attrs.get("style");

            DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
            parser.pushObject(descriptionRule);
        });
        parser.addNodeEndlet(text -> {
            DescriptionRule descriptionRule = parser.popObject();
            ScheduleRule scheduleRule = parser.peekObject();

            descriptionRule.setContent(text);
            descriptionRule = assistant.profiling(descriptionRule, scheduleRule.getDescriptionRule());
            scheduleRule.setDescriptionRule(descriptionRule);
        });
        parser.setXpath(xpath + "/schedule/scheduler");
        parser.addNodelet(attrs -> {
            String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
            if (beanIdOrClass != null) {
                ScheduleRule scheduleRule = parser.peekObject();
                scheduleRule.setSchedulerBeanId(beanIdOrClass);
            }
        });
        parser.setXpath(xpath + "/schedule/scheduler/trigger");
        parser.addNodelet(attrs -> {
            String type = StringUtils.emptyToNull(attrs.get("type"));
            parser.pushObject(type);
        });
        parser.addNodeEndlet(text -> {
            String type = parser.popObject();
            ScheduleRule scheduleRule = parser.peekObject();
            ScheduleRule.updateTrigger(scheduleRule, type, text);
        });
        parser.setXpath(xpath + "/schedule/job");
        parser.addNodelet(attrs -> {
            String transletName = StringUtils.emptyToNull(attrs.get("translet"));
            Boolean disabled = BooleanUtils.toNullableBooleanObject(attrs.get("disabled"));

            ScheduleRule scheduleRule = parser.peekObject();

            ScheduledJobRule scheduledJobRule = ScheduledJobRule.newInstance(scheduleRule, transletName, disabled);
            scheduleRule.addScheduledJobRule(scheduledJobRule);
        });
    }

}
