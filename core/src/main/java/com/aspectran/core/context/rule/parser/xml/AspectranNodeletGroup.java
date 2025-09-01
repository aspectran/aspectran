package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-30</p>
 */
public class AspectranNodeletGroup extends NodeletGroup {

    private static volatile AspectranNodeletGroup INSTANCE;

    static AspectranNodeletGroup instance() {
        if (INSTANCE == null) {
            synchronized (AspectranNodeletGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AspectranNodeletGroup();
                    INSTANCE.lazyInit();
                }
            }
        }
        return INSTANCE;
    }

    AspectranNodeletGroup() {
        super("aspectran");
    }

    private void lazyInit() {
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
