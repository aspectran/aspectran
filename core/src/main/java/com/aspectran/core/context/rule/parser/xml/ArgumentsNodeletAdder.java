package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ability.HasArguments;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class ArgumentsNodeletAdder implements NodeletAdder {

    private static final ArgumentsNodeletAdder INSTANCE = new ArgumentsNodeletAdder();

    static ArgumentsNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("arguments")
            .nodelet(attrs -> {
                ItemRuleMap irm = new ItemRuleMap();
                irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                AspectranNodeParser.current().pushObject(irm);
            })
            .mount(ItemNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRuleMap irm = AspectranNodeParser.current().popObject();
                HasArguments hasArguments = AspectranNodeParser.current().peekObject();
                irm = AspectranNodeParser.current().getAssistant().profiling(irm, hasArguments.getArgumentItemRuleMap());
                hasArguments.setArgumentItemRuleMap(irm);
            });
    }

}
