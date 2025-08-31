package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-30</p>
 */
public class AspectranNodeletGroup extends NodeletGroup {

    private static final AspectranNodeletGroup INSTANCE = new AspectranNodeletGroup();

    static AspectranNodeletGroup instance() {
        return INSTANCE;
    }

    AspectranNodeletGroup() {
        super("aspectran");
        with(DiscriptionNodeletAdder.instance());
        with(SettingsNodeletAdder.instance());
        with(TypeAliasNodeletAdder.instance());
        with(AppendNodeletAdder.instance());
        with(EnvironmentNodeletAdder.instance());
        with(AspectNodeletAdder.instance());
        with(BeanNodeletAdder.instance());
        with(ScheduleNodeletAdder.instance());
        with(TemplateNodeletAdder.instance());
        with(TransletNodeletAdder.instance());
    }

}
