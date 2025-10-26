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
package com.aspectran.core.context.converter;

import com.aspectran.core.context.converter.impl.BigDecimalConverter;
import com.aspectran.core.context.converter.impl.BigIntegerConverter;
import com.aspectran.core.context.converter.impl.BooleanConverter;
import com.aspectran.core.context.converter.impl.ByteConverter;
import com.aspectran.core.context.converter.impl.CharacterConverter;
import com.aspectran.core.context.converter.impl.DateConverter;
import com.aspectran.core.context.converter.impl.DoubleConverter;
import com.aspectran.core.context.converter.impl.FloatConverter;
import com.aspectran.core.context.converter.impl.IntegerConverter;
import com.aspectran.core.context.converter.impl.LocalDateConverter;
import com.aspectran.core.context.converter.impl.LocalDateTimeConverter;
import com.aspectran.core.context.converter.impl.LocalTimeConverter;
import com.aspectran.core.context.converter.impl.LongConverter;
import com.aspectran.core.context.converter.impl.ShortConverter;
import com.aspectran.core.context.converter.impl.StringConverter;
import com.aspectran.utils.TypeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for {@link TypeConverter}s.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public class TypeConverterRegistry {

    private static final TypeConverterRegistry instance = new TypeConverterRegistry();

    private final Map<Class<?>, TypeConverter<?>> converters = new ConcurrentHashMap<>();

    private TypeConverterRegistry() {
        registerDefaultConverters();
    }

    /**
     * Returns the singleton instance of the TypeConverterRegistry.
     * @return the TypeConverterRegistry instance
     */
    public static TypeConverterRegistry getInstance() {
        return instance;
    }

    /**
     * Registers a custom {@link TypeConverter} for a specific type.
     * @param type the type to register the converter for
     * @param converter the converter instance
     */
    public static void register(Class<?> type, TypeConverter<?> converter) {
        getInstance().converters.put(type, converter);
    }

    /**
     * Returns a {@link TypeConverter} for the specified type.
     * @param type the type to find a converter for
     * @return the converter, or null if not found
     */
    public TypeConverter<?> getConverter(Class<?> type) {
        if (type == null) {
            return null;
        }
        // Handle primitive types by looking up their wrapper types
        Class<?> typeToUse = (type.isPrimitive() ? TypeUtils.getPrimitiveWrapper(type) : type);
        return converters.get(typeToUse);
    }

    private void addConverter(Class<?> type, TypeConverter<?> converter) {
        this.converters.put(type, converter);
    }

    private void registerDefaultConverters() {
        addConverter(String.class, new StringConverter());
        addConverter(Character.class, new CharacterConverter());
        addConverter(Boolean.class, new BooleanConverter());
        addConverter(Integer.class, new IntegerConverter());
        addConverter(Long.class, new LongConverter());
        addConverter(Float.class, new FloatConverter());
        addConverter(Double.class, new DoubleConverter());
        addConverter(Short.class, new ShortConverter());
        addConverter(Byte.class, new ByteConverter());
        addConverter(BigInteger.class, new BigIntegerConverter());
        addConverter(BigDecimal.class, new BigDecimalConverter());
        addConverter(Date.class, new DateConverter());
        addConverter(LocalDate.class, new LocalDateConverter());
        addConverter(LocalDateTime.class, new LocalDateTimeConverter());
        addConverter(LocalTime.class, new LocalTimeConverter());
    }

}
