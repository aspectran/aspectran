/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.parser.xml.AspectranDtdResolver;
import com.aspectran.core.context.rule.util.TextStyler;
import com.aspectran.utils.nodelet.NodeletGroup;
import com.aspectran.utils.nodelet.NodeletParser;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * <p>Created: 12/24/23</p>
 */
class ResourceRuleAppenderTest {

    @Test
    void testParsing() throws Exception {
        RuleAppender appender = new ResourceRuleAppender("/config/xml/resource-rule-appender-test-config.xml");

        NodeParserTest nodeParserTest = new NodeParserTest(true);
        nodeParserTest.parse(appender);
    }

    static class NodeParserTest {

        private final NodeletParser nodeletParser;

        private final NodeletGroup nodeletGroup = new NodeletGroup("aspectran");

        NodeParserTest(boolean validating) {
            this.nodeletParser = new NodeletParser(nodeletGroup);
            this.nodeletParser.setValidating(validating);
            this.nodeletParser.setEntityResolver(new AspectranDtdResolver(validating));
            this.nodeletParser.trackingLocation();

            addNodelets();
        }

        void parse(RuleAppender ruleAppender) throws Exception {
            try (InputStream inputStream = ruleAppender.getInputStream()) {
                InputSource inputSource = new InputSource(inputStream);
                inputSource.setSystemId(ruleAppender.getQualifiedName());
                nodeletParser.parse(inputSource);
            } catch (Exception e) {
                throw new Exception("Error parsing aspectran configuration", e);
            }
        }

        private void addNodelets() {
            nodeletGroup.child("description")
                .nodelet(attrs -> {
                    String style = attrs.get("style");
                    nodeletParser.getObjectStack().push(style);
                })
                .endNodelet(text -> {
                    String style = nodeletParser.getObjectStack().pop().toString();

                    String xpath = nodeletParser.getNodeTracker().getPath();
                    System.out.println(xpath + " style=" + style + ", text=" + TextStyler.styling(text, style));
                })
            .parent().child("settings/setting")
                .nodelet(attrs -> {
                    String name = attrs.get("name");
                    String value = attrs.get("value");

                    String xpath = nodeletParser.getNodeTracker().getPath();
                    System.out.println(xpath + name + "=" + value);
                })
            .parent().child("append")
                .nodelet(attrs -> {
                    String file = attrs.get("file");
                    String resource = attrs.get("resource");
                    String url = attrs.get("url");
                    String format = attrs.get("format");
                    String profile = attrs.get("profile");

                    AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);

                    String xpath = nodeletParser.getNodeTracker().getPath();
                    System.out.println(xpath + " rule=" + appendRule);
                });
        }

    }

}
