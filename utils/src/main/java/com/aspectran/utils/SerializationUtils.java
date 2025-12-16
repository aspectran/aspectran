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
package com.aspectran.utils;

import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Static utilities for serialization and deserialization.
 * <p>This class provides methods to serialize an object to a byte array
 * and deserialize a byte array back to an object.</p>
 */
public abstract class SerializationUtils {

    /**
     * Serialize the given object to a byte array.
     * @param object the object to serialize (can be {@code null})
     * @return a byte array representing the serialized object, or {@code null} if the input object is {@code null}
     * @throws IllegalArgumentException if serialization fails
     */
    @Nullable
    public static byte[] serialize(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            oos.flush();
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
        }
        return baos.toByteArray();
    }

    /**
     * Deserialize the given byte array into an object.
     * @param bytes a serialized object (can be {@code null})
     * @return the deserialized object, or {@code null} if the input byte array is {@code null}
     * @throws IllegalArgumentException if deserialization fails due to an I/O error
     * @throws IllegalStateException if the class of a serialized object cannot be found
     */
    @Nullable
    public static Object deserialize(@Nullable byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Failed to deserialize object", ex);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to deserialize object type", ex);
        }
    }

}
