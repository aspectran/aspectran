package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-31</p>
 */
class AppendNodeletAdder implements NodeletAdder {

    private static final AppendNodeletAdder INSTANCE = new AppendNodeletAdder();

    static AppendNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("append")
            .nodelet(attrs -> {
                String file = attrs.get("file");
                String resource = attrs.get("resource");
                String url = attrs.get("url");
                String format = attrs.get("format");
                String profile = attrs.get("profile");

                RuleAppendHandler appendHandler = AspectranNodeParsingContext.assistant().getRuleAppendHandler();
                if (appendHandler != null) {
                    AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                    appendHandler.pending(appendRule);
                }
            });
    }

}
