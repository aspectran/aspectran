package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class DiscriptionNodeletAdder  implements NodeletAdder {

    private static volatile DiscriptionNodeletAdder INSTANCE;

    static DiscriptionNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (DiscriptionNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiscriptionNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("description")
            .nodelet(attrs -> {
                String profile = attrs.get("profile");
                String style = attrs.get("style");

                DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                AspectranNodeParsingContext.pushObject(descriptionRule);
            })
            .endNodelet(text -> {
                DescriptionRule descriptionRule = AspectranNodeParsingContext.popObject();
                descriptionRule.setContent(text);

                Describable describable;
                if ("/aspectran".equals(group.getXpath())) {
                    describable = AspectranNodeParsingContext.assistant().getAssistantLocal();
                } else {
                    describable = AspectranNodeParsingContext.peekObject();
                }

                DescriptionRule oldDescriptionRule = describable.getDescriptionRule();
                descriptionRule = AspectranNodeParsingContext.assistant().profiling(descriptionRule, oldDescriptionRule);
                describable.setDescriptionRule(descriptionRule);
            });
    }

}
