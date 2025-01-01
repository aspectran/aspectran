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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>This class is a clone of org.springframework.util.ObjectUtils</p>
 *
 * Miscellaneous object utility methods.
 */
public abstract class ObjectUtils {

    private static final String NULL_STRING = "null";

    private static final String ARRAY_START = "[";

    private static final String ARRAY_END = "]";

    private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;

    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Determine whether the given object is an array:
     * either an Object array or a primitive array.
     * @param obj the object to check
     */
    public static boolean isArray(@Nullable Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * Determine whether the given array is empty:
     * i.e. {@code null} or of zero length.
     * @param array the array to check
     * @see #isEmpty(Object)
     */
    public static boolean isEmpty(@Nullable Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Determine whether the given object is empty.
     * <p>This method supports the following object types.</p>
     * <ul>
     * <li>{@code Optional}: considered empty if {@link Optional#empty()}</li>
     * <li>{@code Array}: considered empty if its length is zero</li>
     * <li>{@link CharSequence}: considered empty if its length is zero</li>
     * <li>{@link Collection}: delegates to {@link Collection#isEmpty()}</li>
     * <li>{@link Map}: delegates to {@link Map#isEmpty()}</li>
     * </ul>
     * <p>If the given object is non-null and not one of the aforementioned
     * supported types, this method returns {@code false}.</p>
     * @param obj the object to check
     * @return {@code true} if the object is {@code null} or <em>empty</em>
     * @see Optional#isPresent()
     * @see ObjectUtils#isEmpty(Object[])
     * @see StringUtils#hasLength(CharSequence)
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Optional optional) {
            return optional.isEmpty();
        }
        if (obj instanceof CharSequence charSequence) {
            return charSequence.isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Collection collection) {
            return collection.isEmpty();
        }
        if (obj instanceof Map map) {
            return map.isEmpty();
        }
        // else
        return false;
    }

    /**
     * Convert the given array (which may be a primitive array) to an
     * object array (if necessary of primitive wrapper objects).
     * <p>A {@code null} source value will be converted to an
     * empty Object array.</p>
     * @param source the (potentially primitive) array
     * @return the corresponding object array (never {@code null})
     * @throws IllegalArgumentException if the parameter is not an array
     */
    public static Object[] toObjectArray(@Nullable Object source) {
        if (source instanceof Object[]) {
            return (Object[])source;
        }
        if (source == null) {
            return EMPTY_OBJECT_ARRAY;
        }
        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }
        int length = Array.getLength(source);
        if (length == 0) {
            return EMPTY_OBJECT_ARRAY;
        }
        Class<?> wrapperType = Array.get(source, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }
        return newArray;
    }

    /**
     * Determine if the given objects are equal, returning {@code true} if
     * both are {@code null} or {@code false} if only one is {@code null}.
     * <p>Compares arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.</p>
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }

    /**
     * Compare the given arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     * @param o1 first array to compare
     * @param o2 second array to compare
     * @return whether the given objects are equal
     */
    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] a1 && o2 instanceof Object[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof boolean[] a1 && o2 instanceof boolean[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof byte[] a1 && o2 instanceof byte[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[])o1, (char[])o2);
        }
        if (o1 instanceof double[] a1 && o2 instanceof double[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof float[] a1 && o2 instanceof float[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof int[] a1 && o2 instanceof int[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof long[] a1 && o2 instanceof long[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof short[] a1 && o2 instanceof short[] a2) {
            return Arrays.equals(a1, a2);
        }
        return false;
    }

    /**
     * Return a hash code for the given elements, delegating to
     * {@link #nullSafeHashCode(Object)} for each element. Contrary
     * to {@link Objects#hash(Object...)}, this method can handle an
     * element that is an array.
     * @param elements the elements to be hashed
     * @return a hash value of the elements
     */
    public static int nullSafeHash(@Nullable Object... elements) {
        if (elements == null) {
            return 0;
        }
        int result = 1;
        for (Object element : elements) {
            result = 31 * result + nullSafeHashCode(element);
        }
        return result;
    }

    /**
     * Return a hash code for the given object; typically the value of
     * {@code Object#hashCode()}}. If the object is an array,
     * this method will delegate to any of the {@code Arrays.hashCode}
     * methods. If the object is {@code null}, this method returns 0.
     * @see Object#hashCode()
     * @see Arrays
     */
    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof boolean[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof byte[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof char[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof double[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof float[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof int[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof long[] arr) {
                return Arrays.hashCode(arr);
            }
            if (obj instanceof short[] arr) {
                return Arrays.hashCode(arr);
            }
        }
        return obj.hashCode();
    }

    /**
     * Return a String representation of an object's overall identity.
     * @param obj the object (may be {@code null})
     * @return the object's identity as String representation,
     * or an empty String if the object was {@code null}
     */
    public static String identityToString(@Nullable Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        return obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }

    /**
     * Return a simple String representation of an object's overall identity.
     * @param obj the object (may be {@code null})
     * @return the object's identity as simple String representation,
     * or an empty String if the object was {@code null}
     */
    public static String simpleIdentityToString(@Nullable Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        String name = (obj.getClass().isAnonymousClass() ? obj.getClass().getName() : obj.getClass().getSimpleName());
        return name + "@" + getIdentityHexString(obj);
    }

    /**
     * Return a simple String representation of an object's overall identity, with its name.
     * @param obj the object (may be {@code null})
     * @return the object's identity as simple String representation,
     * or an empty String if the object was {@code null}
     */
    @NonNull
    public static String simpleIdentityToString(@Nullable Object obj, @NonNull String name) {
        return simpleIdentityToString(obj) + "(" + name + ")";
    }

    /**
     * Return a hex String form of an object's identity hash code.
     * @param obj the object
     * @return the object's identity code in hex notation
     */
    @NonNull
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

}
