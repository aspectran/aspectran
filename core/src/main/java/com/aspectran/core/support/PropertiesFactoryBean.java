/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.support;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.PropertiesLoaderSupport;
import com.aspectran.utils.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * Allows for making a properties file from a classpath location available
 * as Properties instance in a bean factory. Can be used to populate
 * any bean property of type Properties via a bean reference.
 * Supports loading from a properties file and/or setting local properties
 * on this FactoryBean. The created Properties instance will be merged from
 * loaded and local values.
 *
 * <p>Created: 2025. 2. 18.</p>
 */
@AvoidAdvice
public class PropertiesFactoryBean extends PropertiesLoaderSupport
        implements ActivityContextAware, InitializableFactoryBean<Properties> {

    private ActivityContext context;

    private Properties properties;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    @Override
    protected InputStream getResourceAsStream(String location) throws IOException {
        Assert.notNull(location, "location must not be null");
        InputStream is;
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            is = ResourceUtils.getResourceAsStream(location.substring(CLASSPATH_URL_PREFIX.length()));
        } else {
            Assert.state(context != null, "No ActivityContext injected");
            is = new FileInputStream(context.getApplicationAdapter().getRealPath(location).toFile());
        }
        return is;
    }

    @Override
    public void initialize() throws Exception {
        if (properties == null) {
            Properties properties = mergeProperties();
            if (context != null) {
                TokenEvaluator evaluator = context.getAvailableActivity().getTokenEvaluator();
                Set<String> propertyNames = properties.stringPropertyNames();
                for (String name : propertyNames) {
                    String value = properties.getProperty(name);
                    if (value != null && Token.hasToken(value)) {
                        Token[] tokens = TokenParser.parse(value);
                        for (int i = 0; i < tokens.length; i++) {
                            Token token = tokens[i];
                            if (token.getType() == TokenType.PROPERTY &&
                                    token.getGetterName() == null &&
                                    token.getDirectiveType() == null &&
                                    token.getValueProvider() == null) {
                                String tokenName = token.getName();
                                String defaultValue = token.getDefaultValue();
                                if (tokenName != null && !tokenName.equals(name) && propertyNames.contains(tokenName)) {
                                    tokens[i] = new Token(properties.getProperty(tokenName, defaultValue));
                                }
                            }
                        }
                        String evaluated = (String)evaluator.evaluate(tokens);
                        if (!value.equals(evaluated)) {
                            properties.setProperty(name, evaluated);
                        }
                    }
                }
            }
            this.properties = properties;
        }
    }

    @Override
    public Properties getObject() throws Exception {
        return properties;
    }

}
