package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class SettingsNodeletAdder implements NodeletAdder {

    private static final SettingsNodeletAdder INSTANCE = new SettingsNodeletAdder();

    static SettingsNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("settings")
            .endNodelet(text -> {
                AspectranNodeParsingContext.assistant().applySettings();
            })
            .child("setting")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String value = attrs.get("value");

                    AspectranNodeParsingContext.pushObject(value);
                    AspectranNodeParsingContext.pushObject(name);
                })
                .endNodelet(text -> {
                    String name = AspectranNodeParsingContext.popObject();
                    String value = AspectranNodeParsingContext.popObject();

                    if (value != null) {
                        AspectranNodeParsingContext.assistant().putSetting(name, value);
                    } else if (text != null) {
                        AspectranNodeParsingContext.assistant().putSetting(name, text);
                    }
                });
    }

}
