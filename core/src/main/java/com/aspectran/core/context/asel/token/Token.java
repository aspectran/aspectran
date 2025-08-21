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
package com.aspectran.core.context.asel.token;

import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a parsed token from an Aspectran Expression Language (AsEL) string.
 * <p>A token can be a special token expression that is resolved at runtime or a plain
 * text segment. Special token expressions are identified by their wrapper symbols
 * and are used to access various data within the application context.
 *
 * <p>The supported token expression formats are:
 * <ul>
 *   <li><code>#{...}</code> for beans</li>
 *   <li><code>@{...}</code> for attributes</li>
 *   <li><code>${...}</code> for parameters</li>
 *   <li><code>%{...}</code> for properties</li>
 *   <li><code>~{...}</code> for templates</li>
 * </ul>
 *
 * <p>A special token can also include a name, a directive, a getter, and an optional
 * default value, following the format:
 * <br><code>token_symbol{directive:token_name^getter_name:default_value}</code>
 *
 * <p>The following symbols are used to distinguish the providers of values:</p>
 * <dl>
 *     <dt># - Bean</dt>
 *     <dd>Refers to a bean from the Bean Registry.
 *         <ul>
 *         <li><code>#{beanId}</code></li>
 *         <li><code>#{beanId^propertyName}</code></li>
 *         <li><code>#{class:className^staticPropertyName}</code></li>
 *         <li><code>#{field:className^staticFieldName}</code></li>
 *         </ul>
 *     </dd>
 *     <dt>@ - Attribute</dt>
 *     <dd>Refers to an attribute value in the current activity context.
 *         <ul>
 *         <li><code>@{attributeName}</code></li>
 *         <li><code>@{attributeName^propertyName}</code></li>
 *         <li><code>@{attributeName:defaultValue}</code></li>
 *         </ul>
 *     </dd>
 *     <dt>$ - Parameter</dt>
 *     <dd>Refers to a request parameter value.
 *         <ul>
 *         <li><code>${parameterName}</code></li>
 *         <li><code>${parameterName:defaultValue}</code></li>
 *         </ul>
 *     </dd>
 *     <dt>% - Property</dt>
 *     <dd>Refers to a property from the environment.
 *         <ul>
 *         <li><code>%{propertyName}</code></li>
 *         <li><code>%{system:java.version}</code></li>
 *         <li><code>%{classpath:path/to/file.properties^propName}</code></li>
 *         </ul>
 *     </dd>
 *     <dt>~ - Template</dt>
 *     <dd>Renders a template and includes its output.
 *         <ul>
 *         <li><code>~{templateId}</code></li>
 *         <li><code>~{templateId:defaultContent}</code></li>
 *         </ul>
 *     </dd>
 * </dl>
 *
 * <p>Created: 2008. 03. 27 PM 10:20:06</p>
 */
public class Token implements BeanReferenceable, Replicable<Token> {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.TOKEN;

    /** The character that identifies a bean token. */
    public static final char BEAN_SYMBOL = '#';

    /** The character that identifies a parameter token. */
    public static final char PARAMETER_SYMBOL = '$';

    /** The character that identifies an attribute token. */
    public static final char ATTRIBUTE_SYMBOL = '@';

    /** The character that identifies a property token. */
    public static final char PROPERTY_SYMBOL = '%';

    /** The character that identifies a template token. */
    public static final char TEMPLATE_SYMBOL = '~';

    /** The character that opens a token expression. */
    public static final char BRACKET_OPEN = '{';

    /** The character that closes a token expression. */
    public static final char BRACKET_CLOSE = '}';

    /** The character that separates a token's name from its default value. */
    public static final char VALUE_DELIMITER = ':';

    /** The character that separates a token's name/value from its getter name. */
    public static final char GETTER_DELIMITER = '^';

    private final TokenType type;

    private TokenDirectiveType directiveType;

    private final String name;

    private String value;

    private Object valueProvider;

    private String getterName;

    private String defaultValue;

    /**
     * Constructs a new text token.
     * @param defaultValue the plain text content of the token
     */
    public Token(String defaultValue) {
        this.type = TokenType.TEXT;
        this.name = null;
        this.defaultValue = defaultValue;
    }

    /**
     * Constructs a new special token (e.g., parameter, attribute).
     * @param type the token type
     * @param name the token name
     * @throws UnsupportedOperationException if the token type is {@code TEXT}
     */
    public Token(TokenType type, String name) {
        if (type == TokenType.TEXT) {
            throw new UnsupportedOperationException();
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.type = type;
        this.name = name;
    }

    /**
     * Constructs a new special token with a directive.
     * @param type the token type
     * @param directiveType the token directive type
     * @param value the token value
     * @throws IllegalArgumentException if type or directiveType is null
     */
    public Token(TokenType type, TokenDirectiveType directiveType, String value) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (directiveType == null) {
            throw new IllegalArgumentException("directiveType must not be null");
        }
        this.type = type;
        this.directiveType = directiveType;
        this.name = directiveType.toString();
        this.value = value;
    }

    /**
     * Returns the type of this token (e.g., TEXT, PARAMETER, BEAN).
     * @return the token type, never {@code null}
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the directive type for this token, if any.
     * <p>Directives provide special instructions for how to resolve a token's value,
     * such as accessing a bean by class name ({@code CLASS}) or loading a properties
     * file from the classpath ({@code CLASSPATH}).</p>
     * @return the token directive type, or {@code null} if none is specified
     * @see TokenDirectiveType
     */
    public TokenDirectiveType getDirectiveType() {
        return directiveType;
    }

    /**
     * Returns the name of the token, which is used as the key for value retrieval.
     * For a text token, this will be {@code null}.
     * @return the token name, or {@code null} for a text token
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value associated with a token's directive.
     * <p>This is typically a class name for a bean token or a resource path for
     * a property token.</p>
     * @return the directive value, or {@code null} if not applicable
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value associated with a token's directive.
     * @param value the directive value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the name of a getter method or property to be invoked on the resolved token value.
     * @return the getter name, or {@code null} if not specified
     */
    public String getGetterName() {
        return getterName;
    }

    /**
     * Returns a pre-resolved object that can provide the token's value, such as a
     * {@link Method}, {@link Field}, or {@link Class}. This is used for optimization
     * to avoid repeated lookups.
     * @return the value provider, or {@code null} if not resolved
     */
    public Object getValueProvider() {
        return valueProvider;
    }

    /**
     * Sets a pre-resolved object that can provide the token's value, such as a
     * {@link Method}, {@link Field}, or {@link Class}. This is used for optimization
     * to avoid repeated reflection lookups.
     * @param valueProvider the pre-resolved value provider object
     */
    public void setValueProvider(Object valueProvider) {
        this.valueProvider = valueProvider;
    }

    /**
     * Sets the name of a getter method or property to be invoked on the resolved token value.
     * @param getterName the name of the getter method or property to invoke
     */
    public void setGetterName(String getterName) {
        this.getterName = getterName;
    }

    /**
     * Returns the default value to be used if the token cannot be resolved.
     * For a text token, this holds the plain text content.
     * @return the default value, or {@code null} if not specified
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for this token.
     * @param defaultValue the default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the type of the bean referer, which is always {@link BeanRefererType#TOKEN}
     * for this class.
     * @return the bean referer type
     */
    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    /**
     * Converts this token back into its original string representation.
     * <p>This method reconstructs the expression string (e.g., <code>${name:defaultValue}</code>)
     * from the token's properties, effectively reversing the parsing process.</p>
     * @return the string representation of the token
     * @throws InvalidTokenException if the token type is unknown
     */
    public String stringify() {
        if (type == TokenType.TEXT) {
            return defaultValue;
        }
        StringBuilder sb = new StringBuilder();
        if (type == TokenType.BEAN) {
            sb.append(BEAN_SYMBOL);
            sb.append(BRACKET_OPEN);
            if (name != null) {
                sb.append(name);
            }
            if (value != null) {
                sb.append(VALUE_DELIMITER);
                sb.append(value);
            }
            if (getterName != null) {
                sb.append(GETTER_DELIMITER);
                sb.append(getterName);
            }
        } else if (type == TokenType.TEMPLATE) {
            sb.append(TEMPLATE_SYMBOL);
            sb.append(BRACKET_OPEN);
            if (name != null) {
                sb.append(name);
            }
        } else if (type == TokenType.PARAMETER) {
            sb.append(PARAMETER_SYMBOL);
            sb.append(BRACKET_OPEN);
            if (name != null) {
                sb.append(name);
            }
        } else if (type == TokenType.ATTRIBUTE) {
            sb.append(ATTRIBUTE_SYMBOL);
            sb.append(BRACKET_OPEN);
            if (name != null) {
                sb.append(name);
            }
            if (getterName != null) {
                sb.append(GETTER_DELIMITER);
                sb.append(getterName);
            }
        } else if (type == TokenType.PROPERTY) {
            sb.append(PROPERTY_SYMBOL);
            sb.append(BRACKET_OPEN);
            if (name != null) {
                sb.append(name);
            }
            if (value != null) {
                sb.append(VALUE_DELIMITER);
                sb.append(value);
            }
            if (getterName != null) {
                sb.append(GETTER_DELIMITER);
                sb.append(getterName);
            }
        } else {
            throw new InvalidTokenException("Unknown token type: " + type, this);
        }
        if (defaultValue != null) {
            sb.append(VALUE_DELIMITER);
            sb.append(defaultValue);
        }
        sb.append(BRACKET_CLOSE);
        return sb.toString();
    }

    @Override
    public boolean equals(Object token) {
        return (this == token || (token instanceof Token that && deepEquals(that)));
    }

    private boolean deepEquals(@NonNull Token token) {
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
        if (defaultValue == null) {
            return (token.getDefaultValue() == null);
        } else {
            return defaultValue.equals(token.getDefaultValue());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 7;
        result = prime * result + type.hashCode();
        result = prime * result + (name != null ? name.hashCode() : 0);
        result = prime * result + (value != null ? value.hashCode() : 0);
        result = prime * result + (getterName != null ? getterName.hashCode() : 0);
        result = prime * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    /**
     * Creates and returns a deep copy of this token.
     * @return a new, replicated {@code Token} instance
     * @see Replicable#replicate()
     */
    @Override
    public Token replicate() {
        Token token;
        if (directiveType != null) {
            token = new Token(type, directiveType, value);
            token.setValueProvider(valueProvider);
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

    /**
     * Creates and returns a deep copy of the given token array.
     * @param tokens the array of tokens to replicate
     * @return a new array containing replicated tokens
     */
    public Token[] replicate(Token[] tokens) {
        if (tokens == null) {
            return null;
        }
        Token[] newTokens = new Token[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            newTokens[i] = tokens[i].replicate();
        }
        return newTokens;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", type);
        tsb.append("name", name);
        tsb.append("value", value);
        tsb.append("getterName", getterName);
        tsb.append("defaultValue", defaultValue);
        return tsb.toString();
    }

    /**
     * Checks if the given character is a special symbol that marks the beginning
     * of a token expression (e.g., '$', '@', '#').
     * @param c the character to check
     * @return {@code true} if the character is a token symbol; {@code false} otherwise
     */
    public static boolean isTokenSymbol(char c) {
        return (c == BEAN_SYMBOL
                || c == TEMPLATE_SYMBOL
                || c == PARAMETER_SYMBOL
                || c == ATTRIBUTE_SYMBOL
                || c == PROPERTY_SYMBOL);
    }

    /**
     * Checks if the given expression string contains any AsEL tokens.
     * @param expression the string to check
     * @return {@code true} if the string contains tokens; {@code false} otherwise
     */
    public static boolean hasToken(@NonNull String expression) {
        char[] ca = expression.toCharArray();
        boolean open = false;
        for (int i = 1; i < ca.length; i++) {
            if (isTokenSymbol(ca[i - 1]) && ca[i] == BRACKET_OPEN) {
                i++;
                open = true;
            } else if (open && ca[i] == BRACKET_CLOSE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves a token symbol character to its corresponding {@link TokenType}.
     * @param symbol the token symbol character
     * @return the token type
     * @throws IllegalArgumentException if the symbol is unknown
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

    /**
     * Resolves and sets the {@code valueProvider} for a bean token by loading the
     * specified class, field, or method. This is a pre-processing step to optimize
     * evaluation by caching reflection lookups.
     * @param token the bean token to resolve
     * @param classLoader the class loader to use for loading classes
     */
    public static void resolveValueProvider(Token token, ClassLoader classLoader) {
        if (token != null && token.getType() == TokenType.BEAN) {
            if (token.getDirectiveType() == TokenDirectiveType.FIELD) {
                if (token.getGetterName() == null) {
                    throw new InvalidTokenException("Target field name is unspecified token", token);
                }
                try {
                    Class<?> cls = classLoader.loadClass(token.getValue());
                    Field field = cls.getField(token.getGetterName());
                    token.setValueProvider(field);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load class: " + token.getValue(), e);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("Could not access field: " + token.getGetterName(), e);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.METHOD) {
                if (token.getGetterName() == null) {
                    throw new InvalidTokenException("Target method name is unspecified token", token);
                }
                try {
                    Class<?> cls = classLoader.loadClass(token.getValue());
                    Method method = cls.getMethod(token.getGetterName());
                    token.setValueProvider(method);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load class: " + token.getValue(), e);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Could not access method: " + token.getGetterName(), e);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.CLASS) {
                try {
                    Class<?> cls = classLoader.loadClass(token.getValue());
                    token.setValueProvider(cls);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load class: " + token.getValue(), e);
                }
            }
        }
    }

    /**
     * Formats a raw expression string into a valid AsEL token string by wrapping
     * it with the appropriate symbols (e.g., "name" becomes "#{name}").
     * @param type the {@link TokenType} to apply
     * @param expression the raw expression content
     * @return the formatted token string
     */
    public static String format(TokenType type, String expression) {
        if (type == null) {
            throw new IllegalArgumentException("Token type must not be null");
        }
        if (type == TokenType.TEXT) {
            return expression;
        }
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case BEAN:
                sb.append(BEAN_SYMBOL);
                break;
            case PARAMETER:
                sb.append(PARAMETER_SYMBOL);
                break;
            case ATTRIBUTE:
                sb.append(ATTRIBUTE_SYMBOL);
                break;
            case PROPERTY:
                sb.append(PROPERTY_SYMBOL);
                break;
            case TEMPLATE:
                sb.append(TEMPLATE_SYMBOL);
                break;
            default:
                throw new IllegalArgumentException("Unknown token type: " + type);
        }
        sb.append(BRACKET_OPEN);
        if (expression != null) {
            sb.append(expression);
        }
        return sb.append(BRACKET_CLOSE).toString();
    }

}
