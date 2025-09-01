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

    private static volatile PropertiesNodeletAdder INSTANCE;

    static PropertiesNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (PropertiesNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PropertiesNodeletAdder();
                }
            }
        }
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
            .with(ItemNodeletAdder.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParsingContext.popObject();
                Object object = AspectranNodeParsingContext.peekObject();
                try {
//                    HasProperties hasProperties = AspectranNodeParsingContextngContext.peekObject();
                    HasProperties hasProperties = (HasProperties)object;
                    irm = AspectranNodeParsingContext.assistant().profiling(irm, hasProperties.getPropertyItemRuleMap());
                    hasProperties.setPropertyItemRuleMap(irm);
                } catch (ClassCastException e) {
                    System.out.println(e.getMessage());
                }
            });
    }

}
