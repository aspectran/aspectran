package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class TypeAliasNodeletAdder implements NodeletAdder {

    private static final TypeAliasNodeletAdder INSTANCE = new TypeAliasNodeletAdder();

    static TypeAliasNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("typeAliases")
            .nodelet(attrs -> {
                String alias = attrs.get("alias");
                String type = attrs.get("type");

                AspectranNodeParsingContext.pushObject(type);
                AspectranNodeParsingContext.pushObject(alias);
            })
            .endNodelet(text -> {
                String alias = AspectranNodeParsingContext.popObject();
                String type = AspectranNodeParsingContext.popObject();

                if (type != null) {
                    AspectranNodeParsingContext.assistant().addTypeAlias(alias, type);
                } else if (text != null) {
                    AspectranNodeParsingContext.assistant().addTypeAlias(alias, text);
                }
            });
    }

}
