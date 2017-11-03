package com.aspectran.core.util.nodelet;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.appender.ResourceRuleAppender;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.parser.xml.AspectranDtdResolver;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Created: 2017. 11. 2.</p>
 */
public class NodeletParserTest {

    @Test
    public void testParsing() throws Exception {
        RuleAppender appender = new ResourceRuleAppender("/config/xml/nodelet-parser-test-config.xml");

        NodeParserTest nodeParserTest = new NodeParserTest(true);
        nodeParserTest.parse(appender);
    }

    class NodeParserTest {

        private final NodeletParser parser;

        public NodeParserTest(boolean validating) {
            this.parser = new NodeletParser();
            this.parser.setValidating(validating);
            this.parser.setEntityResolver(new AspectranDtdResolver(validating));
            this.parser.trackingLocation();

            addNodelets();
        }

        public void parse(RuleAppender ruleAppender) throws Exception {
            InputStream inputStream = null;
            try {
                inputStream = ruleAppender.getInputStream();
                InputSource inputSource = new InputSource(inputStream);
                inputSource.setSystemId(ruleAppender.getQualifiedName());
                parser.parse(inputSource);
            } catch (Exception e) {
                throw new Exception("Error parsing aspectran configuration", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        private void addNodelets() {
            parser.setXpath("/aspectran/description");
            parser.addNodelet(attrs -> {
                String style = attrs.get("style");
                parser.pushObject(style);
            });
            parser.addNodeEndlet(text -> {
                String style = parser.popObject();
                System.out.println(parser.getXpath() + " style=" + style + ", text=" + text);
            });
            parser.setXpath("/aspectran/settings/setting");
            parser.addNodelet(attrs -> {
                String name = attrs.get("name");
                String value = attrs.get("value");

                System.out.println(parser.getXpath() + " setting " + name + "=" + value);
            });
            parser.setXpath("/aspectran/append");
            parser.addNodelet(attrs -> {
                String file = attrs.get("file");
                String resource = attrs.get("resource");
                String url = attrs.get("url");
                String format = attrs.get("format");
                String profile = attrs.get("profile");

                AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                System.out.println(parser.getXpath() + " appendRule=" + appendRule);
            });
        }
    }

}