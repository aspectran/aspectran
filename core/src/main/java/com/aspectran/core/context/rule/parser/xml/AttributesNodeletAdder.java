package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.HasAttributes;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class AttributesNodeletAdder implements NodeletAdder {

    private static final AttributesNodeletAdder INSTANCE = new AttributesNodeletAdder();

    static AttributesNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("attributes")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParser.current().pushObject(irm);
            })
            .mount(ItemNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParser.current().popObject();
                HasAttributes hasAttributes = AspectranNodeParser.current().peekObject();
                irm = AspectranNodeParser.current().getAssistant().profiling(irm, hasAttributes.getAttributeItemRuleMap());
                hasAttributes.setAttributeItemRuleMap(irm);
            });
    }

}
