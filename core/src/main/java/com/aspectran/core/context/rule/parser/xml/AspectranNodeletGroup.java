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

    private final NodeletGroup chooseNodeletGroup = new NodeletGroup();
    private final NodeletGroup itemNodeletGroup = new NodeletGroup();
    private final NodeletGroup innerBeanNodeletGroup = new NodeletGroup();

    private final NodeletAdder actionNodeletAdder = new ActionNodeletAdder();
    private final NodeletAdder adviceInnerNodeAdder = new AdviceInnerNodeletAdder();
    private final NodeletAdder exceptionInnerNodeletAdder = new ExceptionInnerNodeletAdder();
    private final NodeletAdder responseInnerNodeletAdder = new ResponseInnerNodeletAdder();

    AspectranNodeletGroup() {
        super("aspectran");

        chooseNodeletGroup.with(new ChooseNodeletAdder());
        itemNodeletGroup.with(new ItemNodeletAdder());
        innerBeanNodeletGroup.with(new InnerBeanNodeletAdder());

        with(createDescriptionNodeletAdder());
        with(createSettingsNodeletAdder());
        with(createTypeAliasNodeletAdder());
        with(createAppendNodeletAdder());
        with(new EnvironmentNodeletAdder());
        with(new AspectNodeletAdder());
        with(new BeanNodeletAdder());
        with(new ScheduleNodeletAdder());
        with(new TemplateNodeletAdder());
        with(new TransletNodeletAdder());
    }

    public NodeletAdder getActionNodeletAdder() {
        return actionNodeletAdder;
    }

    public NodeletAdder getAdviceInnerNodeAdder() {
        return adviceInnerNodeAdder;
    }

    public NodeletAdder getExceptionInnerNodeletAdder() {
        return exceptionInnerNodeletAdder;
    }

    public NodeletAdder getResponseInnerNodeletAdder() {
        return responseInnerNodeletAdder;
    }

    public NodeletGroup getChooseNodeletGroup() {
        return chooseNodeletGroup;
    }

    public NodeletGroup getItemNodeletGroup() {
        return itemNodeletGroup;
    }

    public NodeletGroup getInnerBeanNodeletGroup() {
        return innerBeanNodeletGroup;
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
