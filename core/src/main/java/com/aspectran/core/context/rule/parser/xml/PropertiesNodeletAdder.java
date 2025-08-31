package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.HasProperties;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class PropertiesNodeletAdder implements NodeletAdder {

    private static final PropertiesNodeletAdder INSTANCE = new PropertiesNodeletAdder();

    static PropertiesNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("properties")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParsingContext.pushObject(irm);
            })
            .mount(ItemNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                HasProperties hasProperties = AspectranNodeParsingContext.peekObject();
                irm = AspectranNodeParsingContext.assistant().profiling(irm, hasProperties.getPropertyItemRuleMap());
                hasProperties.setPropertyItemRuleMap(irm);
            });
    }

}
