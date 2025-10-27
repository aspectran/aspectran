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
package com.aspectran.core.component.converter;

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.converter.impl.BigDecimalConverter;
import com.aspectran.core.component.converter.impl.BigIntegerConverter;
import com.aspectran.core.component.converter.impl.BooleanConverter;
import com.aspectran.core.component.converter.impl.ByteConverter;
import com.aspectran.core.component.converter.impl.CharacterConverter;
import com.aspectran.core.component.converter.impl.DateConverter;
import com.aspectran.core.component.converter.impl.DoubleConverter;
import com.aspectran.core.component.converter.impl.FloatConverter;
import com.aspectran.core.component.converter.impl.IntegerConverter;
import com.aspectran.core.component.converter.impl.LocalDateConverter;
import com.aspectran.core.component.converter.impl.LocalDateTimeConverter;
import com.aspectran.core.component.converter.impl.LocalTimeConverter;
import com.aspectran.core.component.converter.impl.LongConverter;
import com.aspectran.core.component.converter.impl.ShortConverter;
import com.aspectran.core.component.converter.impl.StringConverter;
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
 * A component that manages the registration and retrieval of {@link TypeConverter}s.
 * Its lifecycle is managed by the {@link com.aspectran.core.context.ActivityContext}.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public class TypeConverterRegistry extends AbstractComponent {

    private final Map<Class<?>, TypeConverter<?>> converters = new ConcurrentHashMap<>();

    /**
     * Instantiates a new TypeConverterRegistry.
     */
    public TypeConverterRegistry() {
        super();
    }

    /**
     * Registers a custom {@link TypeConverter} for a specific type.
     * This method must be called before the component is initialized.
     * @param type the type to register the converter for
     * @param converter the converter instance
     */
    public void register(Class<?> type, TypeConverter<?> converter) {
        checkInitializable();
        converters.put(type, converter);
    }

    /**
     * Returns a {@link TypeConverter} for the specified type.
     * @param type the type to find a converter for
     * @param <T> the target type of the converter
     * @return the converter, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> type) {
        if (type == null) {
            return null;
        }
        // Handle primitive types by looking up their wrapper types
        Class<?> typeToUse = (type.isPrimitive() ? TypeUtils.getPrimitiveWrapper(type) : type);
        return (TypeConverter<T>)converters.get(typeToUse);
    }

    /**
     * Registers default {@link TypeConverter}s.
     * @throws Exception if an error occurs during initialization
     */
    @Override
    protected void doInitialize() throws Exception {
        register(String.class, new StringConverter());
        register(Character.class, new CharacterConverter());
        register(Boolean.class, new BooleanConverter());
        register(Integer.class, new IntegerConverter());
        register(Long.class, new LongConverter());
        register(Float.class, new FloatConverter());
        register(Double.class, new DoubleConverter());
        register(Short.class, new ShortConverter());
        register(Byte.class, new ByteConverter());
        register(BigInteger.class, new BigIntegerConverter());
        register(BigDecimal.class, new BigDecimalConverter());
        register(Date.class, new DateConverter());
        register(LocalDate.class, new LocalDateConverter());
        register(LocalDateTime.class, new LocalDateTimeConverter());
        register(LocalTime.class, new LocalTimeConverter());
    }

    /**
     * Clears all registered converters.
     */
    @Override
    protected void doDestroy() {
        converters.clear();
    }

}
