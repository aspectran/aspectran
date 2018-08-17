/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.context.expr.token;

import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * A token has a string value of its own or contains information
 * for fetching a specific value from another provider.
 *
 * <p>The following symbols are used to distinguish the providers of values:</p>
 * <dl>
 *     <dt>#</dt>
 *     <dd><p>Refers to a bean specified by the Bean Registry.</p>
 *         ex)
 *         <ul>
 *         <li>#{beanId}
 *         <li>#{beanId^getterName}
 *         <li>#{beanId^getterName:defaultString}
 *         <li>#{class:className}
 *         <li>#{class:className^getterName}
 *         <li>#{class:className^getterName:defaultString}
 *         </ul>
 *     </dd>
 *     <dt>~</dt>
 *     <dd><p>Refers to a string formatted from the Template Rule Registry.</p>
 *         ex)
 *         <ul>
 *         <li>~{templateId}
 *         <li>~{templateId:defaultString}
 *         </ul>
 *     </dd>
 *     <dt>$</dt>
 *     <dd><p>Refers to a parameter value.</p>
 *         ex)
 *         <ul>
 *         <li>${parameterName}
 *         <li>${parameterName:defaultString}
 *         </ul>
 *     </dd>
 *     <dt>@</dt>
 *     <dd><p>Refers to an attribute value.</p>
 *         ex)
 *         <ul>
 *         <li>{@literal @}{attributeName}
 *         <li>{@literal @}{attributeName:defaultString}
 *         <li>{@literal @}{attributeName^getterName:defaultString}
 *         </ul>
 *     </dd>
 *     <dt>%</dt>
 *     <dd><p>Refers to a property from the specified Properties file or environment variables.</p>
 *         ex)
 *         <ul>
 *         <li>%{environmentPropertyName}
 *         <li>%{classpath:propertiesPath^getterName}
 *         <li>%{classpath:propertiesPath^getterName:defaultString}
 *         </ul>
 *     </dd>
 * </dl>
 *
 * <p>Created: 2008. 03. 27 PM 10:20:06</p>
 */
public class Token implements BeanReferenceInspectable, Replicable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.TOKEN;

    static final char BEAN_SYMBOL = '#';

    static final char TEMPLATE_SYMBOL = '~';

    static final char PARAMETER_SYMBOL = '$';

    static final char ATTRIBUTE_SYMBOL = '@';

    static final char PROPERTY_SYMBOL = '%';

    static final char START_BRACKET = '{';

    static final char END_BRACKET = '}';

    static final char VALUE_SEPARATOR = ':';

    static final char GETTER_SEPARATOR = '^';

    private final TokenType type;

    private TokenDirectiveType directiveType;

    private final String name;

    private String value;

    private Object alternativeValue;

    private String getterName;

    private String defaultValue;

    /**
     * Instantiates a new Token.
     *
     * @param defaultValue the default value
     */
    public Token(String defaultValue) {
        this.type = TokenType.TEXT;
        this.name = null;
        this.defaultValue = defaultValue;
    }

    /**
     * Instantiates a new Token.
     *
     * @param type the token type
     * @param name the token name
     */
    public Token(TokenType type, String name) {
        if (type == TokenType.TEXT) {
            throw new UnsupportedOperationException();
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Argument 'name' must not be null");
        }
        this.type = type;
        this.name = name;
    }

    /**
     * Instantiates a new Token.
     *
     * @param type the token type
     * @param directiveType the token directive type
     * @param value the token value
     */
    public Token(TokenType type, TokenDirectiveType directiveType, String value) {
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' must not be null");
        }
        if (directiveType == null) {
            throw new IllegalArgumentException("Argument 'directiveType' must not be null");
        }
        this.type = type;
        this.directiveType = directiveType;
        this.name = directiveType.toString();
        this.value = value;
    }

    /**
     * Gets the token type.
     *
     * @return the token type
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Gets the token directive type.
     *
     * @return the token directive type
     * @see TokenDirectiveType
     */
    public TokenDirectiveType getDirectiveType() {
        return directiveType;
    }

    /**
     * Sets the token directive type.
     *
     * @param directiveType the token directive type
     * @see TokenDirectiveType
     */
    private void setDirectiveType(TokenDirectiveType directiveType) {
        this.directiveType = directiveType;
    }

    /**
     * Gets the token name.
     *
     * @return the token name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the class name of the bean or the classpath of the Properties file,
     * depending on the type of token.
     * For example, if the token type is "bean" and the token name is "class",
     * the value of the token is the class name of the bean.
     * Also, if the token type is "property" and the token name is "classpath",
     * the value of the token is the path to reference in the Properties file.
     *
     * @return the default value or bean's class name
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the class name of the bean or the classpath of the Properties file,
     * depending on the type of the token.
     *
     * @param value the class name of the bean or the classpath of the Properties file
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the name of the property whose value is to be retrieved.
     *
     * @return the name of the property whose value is to be retrieved
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * Gets the alternative value.
     * It is a value corresponding to class name or class path according to token directive.
     *
     * @return the alternative value
     */
    public Object getAlternativeValue() {
        return alternativeValue;
    }

    /**
     * Sets the alternative value.
     * It is a value corresponding to class name or class path according to token directive.
     *
     * @param value the new alternative value
     */
    public void setAlternativeValue(Object value) {
        this.alternativeValue = value;
    }

    /**
     * Sets the name of the property whose value is to be retrieved.
     *
     * @param getterName the name of the property whose value is to be retrieved
     */
    public void setGetterName(String getterName) {
        this.getterName = getterName;
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue the new default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    /**
     * Convert a Token object into a string.
     *
     * @return a string representation of the token
     */
    public String stringify() {
        if (type == TokenType.TEXT) {
            return defaultValue;
        }
        StringBuilder sb = new StringBuilder();
        if (type == TokenType.BEAN) {
            sb.append(BEAN_SYMBOL);
            sb.append(START_BRACKET);
            if (name != null) {
                sb.append(name);
            }
            if (value != null) {
                sb.append(VALUE_SEPARATOR);
                sb.append(value);
            }
            if (getterName != null) {
                sb.append(GETTER_SEPARATOR);
                sb.append(getterName);
            }
        } else if (type == TokenType.TEMPLATE) {
            sb.append(TEMPLATE_SYMBOL);
            sb.append(START_BRACKET);
            if (name != null) {
                sb.append(name);
            }
        } else if (type == TokenType.PARAMETER) {
            sb.append(PARAMETER_SYMBOL);
            sb.append(START_BRACKET);
            if (name != null) {
                sb.append(name);
            }
        } else if (type == TokenType.ATTRIBUTE) {
            sb.append(ATTRIBUTE_SYMBOL);
            sb.append(START_BRACKET);
            if (name != null) {
                sb.append(name);
            }
            if (getterName != null) {
                sb.append(GETTER_SEPARATOR);
                sb.append(getterName);
            }
        } else if (type == TokenType.PROPERTY) {
            sb.append(PROPERTY_SYMBOL);
            sb.append(START_BRACKET);
            if (name != null) {
                sb.append(name);
            }
            if (value != null) {
                sb.append(VALUE_SEPARATOR);
                sb.append(value);
            }
            if (getterName != null) {
                sb.append(GETTER_SEPARATOR);
                sb.append(getterName);
            }
        } else {
            throw new InvalidTokenException("Unknown token type: " + type, this);
        }
        if (defaultValue != null) {
            sb.append(VALUE_SEPARATOR);
            sb.append(defaultValue);
        }
        sb.append(END_BRACKET);
        return sb.toString();
    }

    @Override
    public boolean equals(Object token) {
        return (this == token || (token instanceof Token && deepEquals((Token)token)));
    }

    private boolean deepEquals(Token token) {
        if (type != token.getType()) {
            return false;
        }
        if (name != null) {
            if (!name.equals(token.getName())) {
                return false;
            }
        } else if (token.getName() != null) {
            return false;
        }
        if (value != null) {
            if (!value.equals(token.getValue())) {
                return false;
            }
        } else if (token.getValue() != null) {
            return false;
        }
        if (getterName != null) {
            if (!getterName.equals(token.getGetterName())) {
                return false;
            }
        } else if (token.getGetterName() != null) {
            return false;
        }
        if (defaultValue != null) {
            if (!defaultValue.equals(token.getDefaultValue())) {
                return false;
            }
        } else if (token.getDefaultValue() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 11;
        result = prime * result + type.hashCode();
        result = prime * result + (name != null ? name.hashCode() : 0);
        result = prime * result + (value != null ? value.hashCode() : 0);
        result = prime * result + (getterName != null ? getterName.hashCode() : 0);
        result = prime * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public Token replicate() {
        Token token;
        if (directiveType != null) {
            token = new Token(type, directiveType, value);
            token.setAlternativeValue(alternativeValue);
            token.setGetterName(getterName);
            token.setDefaultValue(defaultValue);
        } else {
            if (type == TokenType.TEXT) {
                token = new Token(defaultValue);
            } else {
                token = new Token(type, name);
                token.setGetterName(getterName);
                token.setDefaultValue(defaultValue);
            }
        }
        return token;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", type);
        tsb.append("name", name);
        tsb.append("value", value);
        tsb.append("alternativeValue", alternativeValue);
        tsb.append("getterName", getterName);
        tsb.append("defaultValue", defaultValue);
        return tsb.toString();
    }

    /**
     * Returns whether a specified character is the token symbol.
     *
     * @param c a character
     * @return true, if a specified character is one of the token symbols
     */
    public static boolean isTokenSymbol(char c) {
        return (c == BEAN_SYMBOL
                || c == TEMPLATE_SYMBOL
                || c == PARAMETER_SYMBOL
                || c == ATTRIBUTE_SYMBOL
                || c == PROPERTY_SYMBOL);
    }

    /**
     * Returns the token type for the specified character.
     *
     * @param symbol the token symbol character
     * @return the token type
     */
    public static TokenType resolveTypeAsSymbol(char symbol) {
        TokenType type;
        if (symbol == Token.BEAN_SYMBOL) {
            type = TokenType.BEAN;
        } else if (symbol == Token.TEMPLATE_SYMBOL) {
            type = TokenType.TEMPLATE;
        } else if (symbol == Token.PARAMETER_SYMBOL) {
            type = TokenType.PARAMETER;
        } else if (symbol == Token.ATTRIBUTE_SYMBOL) {
            type = TokenType.ATTRIBUTE;
        } else if (symbol == Token.PROPERTY_SYMBOL) {
            type = TokenType.PROPERTY;
        } else {
            throw new IllegalArgumentException("Unknown token symbol: " + symbol);
        }
        return type;
    }

}
