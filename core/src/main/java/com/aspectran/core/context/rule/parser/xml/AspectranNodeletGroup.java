package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * <p>Created: 2025-08-30</p>
 */
public class AspectranNodeletGroup extends NodeletGroup {

    public static final int MAX_INNER_BEAN_DEPTH = 3;
    public static final int MAX_CHOOSE_DEPTH = 1;

    static final NodeletAdder appendNodeletAdder;
    static final NodeletAdder descriptionNodeletAdder;
    static final NodeletAdder settingsNodeletAdder;
    static final NodeletAdder typeAliasNodeletAdder;
    static final NodeletAdder environmentNodeletAdder;
    static final NodeletAdder actionNodeletAdder;
    static final NodeletAdder adviceInnerNodeAdder;
    static final NodeletAdder aspectNodeletAdder;
    static final NodeletAdder beanNodeletAdder;
    static final NodeletAdder exceptionInnerNodeletAdder;
    static final NodeletAdder responseInnerNodeletAdder;
    static final NodeletAdder scheduleNodeletAdder;
    static final NodeletAdder templateNodeletAdder;
    static final NodeletAdder transletNodeletAdder;
    static final ItemNodeletAdder[] itemNodeletAdders;
    static final ItemNodeletAdder itemNodeletAdder;
    static final InnerBeanNodeletAdder[] innerBeanNodeletAdders;
    static final InnerBeanNodeletAdder innerBeanNodeletAdder;
    static final ChooseNodeletAdder[] chooseNodeletAdders;
    static final ChooseNodeletAdder chooseNodeletAdder;

    static {
        descriptionNodeletAdder = createDescriptionNodeletAdder();
        settingsNodeletAdder = createSettingsNodeletAdder();
        typeAliasNodeletAdder = createTypeAliasNodeletAdder();
        environmentNodeletAdder = new EnvironmentNodeletAdder();
        actionNodeletAdder = new ActionNodeletAdder();
        adviceInnerNodeAdder = new AdviceInnerNodeletAdder();
        aspectNodeletAdder = new AspectNodeletAdder();
        beanNodeletAdder = new BeanNodeletAdder();
        exceptionInnerNodeletAdder = new ExceptionInnerNodeletAdder();
        responseInnerNodeletAdder = new ResponseInnerNodeletAdder();
        scheduleNodeletAdder = new ScheduleNodeletAdder();
        templateNodeletAdder = new TemplateNodeletAdder();
        transletNodeletAdder = new TransletNodeletAdder();
        appendNodeletAdder = createAppendNodeletAdder();
        itemNodeletAdders = new ItemNodeletAdder[MAX_INNER_BEAN_DEPTH + 1];
        innerBeanNodeletAdders = new InnerBeanNodeletAdder[MAX_INNER_BEAN_DEPTH + 1];
        for (int i = 0; i < itemNodeletAdders.length; i++) {
            itemNodeletAdders[i] = new ItemNodeletAdder(i);
            innerBeanNodeletAdders[i] = new InnerBeanNodeletAdder(i);
        }
        itemNodeletAdder = itemNodeletAdders[0];
        innerBeanNodeletAdder = innerBeanNodeletAdders[0];
        chooseNodeletAdders = new ChooseNodeletAdder[MAX_CHOOSE_DEPTH + 1];
        for (int i = 0; i < chooseNodeletAdders.length; i++) {
            chooseNodeletAdders[i] = new ChooseNodeletAdder(i);
        }
        chooseNodeletAdder = chooseNodeletAdders[0];
    }

    AspectranNodeletGroup() {
        super("aspectran");
        with(createDescriptionNodeletAdder());
        with(createSettingsNodeletAdder());
        with(createTypeAliasNodeletAdder());
        with(environmentNodeletAdder);
        with(aspectNodeletAdder);
        with(beanNodeletAdder);
        with(scheduleNodeletAdder);
        with(templateNodeletAdder);
        with(transletNodeletAdder);
        with(appendNodeletAdder);
    }

    @NonNull
    private static NodeletAdder createDescriptionNodeletAdder() {
        return group -> group.child("description")
                .nodelet(attrs -> {
                    String profile = attrs.get("profile");
                    String style = attrs.get("style");

                    DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
                    AspectranNodeParser.current().pushObject(descriptionRule);
                })
                .endNodelet(text -> {
                    DescriptionRule descriptionRule = AspectranNodeParser.current().popObject();
                    descriptionRule.setContent(text);
                    descriptionRule = AspectranNodeParser.current().getAssistant().profiling(
                            descriptionRule, AspectranNodeParser.current().getAssistant().getAssistantLocal().getDescriptionRule());
                    AspectranNodeParser.current().getAssistant().getAssistantLocal().setDescriptionRule(descriptionRule);
                });
    }

    @NonNull
    private static NodeletAdder createSettingsNodeletAdder() {
        return group -> group.child("settings")
                .endNodelet("/aspectran/settings", text -> {
                    AspectranNodeParser.current().getAssistant().applySettings();
                })
                .child("setting")
                    .nodelet(attrs -> {
                        String name = attrs.get("name");
                        String value = attrs.get("value");
                        AspectranNodeParser.current().pushObject(value);
                        AspectranNodeParser.current().pushObject(name);
                    })
                    .endNodelet(text -> {
                        String name = AspectranNodeParser.current().popObject();
                        String value = AspectranNodeParser.current().popObject();
                        if (value != null) {
                            AspectranNodeParser.current().getAssistant().putSetting(name, value);
                        } else if (text != null) {
                            AspectranNodeParser.current().getAssistant().putSetting(name, text);
                        }
                    });
    }

    @NonNull
    private static NodeletAdder createTypeAliasNodeletAdder() {
        return group -> group.child("typeAliases")
                .nodelet(attrs -> {
                    String alias = attrs.get("alias");
                    String type = attrs.get("type");
                    AspectranNodeParser.current().pushObject(type);
                    AspectranNodeParser.current().pushObject(alias);
                })
                .endNodelet(text -> {
                    String alias = AspectranNodeParser.current().popObject();
                    String type = AspectranNodeParser.current().popObject();
                    if (type != null) {
                        AspectranNodeParser.current().getAssistant().addTypeAlias(alias, type);
                    } else if (text != null) {
                        AspectranNodeParser.current().getAssistant().addTypeAlias(alias, text);
                    }
                });
    }

    @NonNull
    private static NodeletAdder createAppendNodeletAdder() {
        return group -> group.child("append")
                .nodelet(attrs -> {
                    String file = attrs.get("file");
                    String resource = attrs.get("resource");
                    String url = attrs.get("url");
                    String format = attrs.get("format");
                    String profile = attrs.get("profile");

                    RuleAppendHandler appendHandler = AspectranNodeParser.current().getAssistant().getRuleAppendHandler();
                    if (appendHandler != null) {
                        AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                        appendHandler.pending(appendRule);
                    }
                });
    }

}
