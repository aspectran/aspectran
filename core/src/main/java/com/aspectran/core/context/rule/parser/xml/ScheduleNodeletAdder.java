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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ScheduleNodeParser.
 *
 * <p>Created: 2016. 08. 29.</p>
 */
class ScheduleNodeletAdder implements NodeletAdder {

    private static final ScheduleNodeletAdder INSTANCE = new ScheduleNodeletAdder();

    static ScheduleNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(NodeletGroup group) {
        group.child("schedule")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));

                ScheduleRule scheduleRule = ScheduleRule.newInstance(id);

                AspectranNodeParser.current().pushObject(scheduleRule);
            })
            .with(DiscriptionNodeletAdder.instance())
            .endNodelet(text -> {
                ScheduleRule scheduleRule = AspectranNodeParser.current().popObject();
                AspectranNodeParser.current().getAssistant().addScheduleRule(scheduleRule);
            })
            .child("scheduler")
                .nodelet(attrs -> {
                    String beanIdOrClass = StringUtils.emptyToNull(attrs.get("bean"));
                    if (beanIdOrClass != null) {
                        ScheduleRule scheduleRule = AspectranNodeParser.current().peekObject();
                        scheduleRule.setSchedulerBeanId(beanIdOrClass);
                    }
                })
                .child("trigger")
                    .nodelet(attrs -> {
                        String type = StringUtils.emptyToNull(attrs.get("type"));
                        AspectranNodeParser.current().pushObject(type);
                    })
                    .endNodelet(text -> {
                        String type = AspectranNodeParser.current().popObject();
                        ScheduleRule scheduleRule = AspectranNodeParser.current().peekObject();
                        ScheduleRule.updateTrigger(scheduleRule, type, text);
                    })
                .parent()
            .parent().child("job")
                .nodelet(attrs -> {
                    String transletName = StringUtils.emptyToNull(attrs.get("translet"));
                    Boolean disabled = BooleanUtils.toNullableBooleanObject(attrs.get("disabled"));

                    ScheduleRule scheduleRule = AspectranNodeParser.current().peekObject();

                    ScheduledJobRule scheduledJobRule = ScheduledJobRule.newInstance(scheduleRule, transletName, disabled);
                    scheduleRule.addScheduledJobRule(scheduledJobRule);
                });
    }

}
