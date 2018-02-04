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
package com.aspectran.core.context.rule.type;

/**
 * Supported Transform types.
 * 
 * <p>Created: 2008. 04. 25 AM 16:47:38</p>
 */
public enum TransformType {

    XML("transform/xml"),
    XSL("transform/xsl"),
    TEXT("transform/text"),
    JSON("transform/json"),
    APON("transform/apon");

    private final String alias;

    TransformType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code TransformType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the transform type as a {@code String}
     * @return a {@code TransformType}, may be {@code null}
     */
    public static TransformType resolve(String alias) {
        for (TransformType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns a {@code TransformType} with a value corresponding
     * to the specified {@code ContentType}.
     *
     * @param contentType the content type as a {@code ContentType}
     * @return a {@code TransformType}, may be {@code null}
     */
    public static TransformType resolve(ContentType contentType) {
        if (contentType == ContentType.TEXT_PLAIN) {
            return TEXT;
        } else if (contentType == ContentType.TEXT_XML) {
            return XML;
        } else if (contentType == ContentType.TEXT_JSON) {
            return JSON;
        } else if (contentType == ContentType.TEXT_APON) {
            return APON;
        } else {
            return null;
        }
    }

}
