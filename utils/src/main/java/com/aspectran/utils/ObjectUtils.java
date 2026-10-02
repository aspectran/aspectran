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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Miscellaneous object utility methods.
 * <p>Provides methods for working with arrays, checking emptiness, null-safe equality,
 * hashing, string conversion, and default values.</p>
 */
public abstract class ObjectUtils {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Determine whether the given object is an array:
     * either an Object array or a primitive array.
     * @param obj the object to check
     * @return {@code true} if the object is an array; {@code false} otherwise
     */
    public static boolean isArray(@Nullable Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * Determine whether the given array is empty:
     * i.e. {@code null} or of zero length.
     * @param array the array to check
     * @return {@code true} if the array is empty or {@code null}
     * @see #isEmpty(Object)
     */
    public static boolean isEmpty(@Nullable Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Determine whether the given object is empty.
     * <p>This method supports the following object types:</p>
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
        return false;
    }

    /**
     * Returns the given object if not {@code null}, or the default value otherwise.
     * @param <T> the type of object
     * @param object the object to check
     * @param defaultValue the default value to return if object is {@code null}
     * @return the object if not {@code null}, or the default value
     */
    public static <T> @NonNull T defaultIfNull(@Nullable T object, @NonNull T defaultValue) {
        Assert.notNull(defaultValue, "defaultValue must not be null");
        return (object != null ? object : defaultValue);
    }

    /**
     * Returns the given object if not {@code null}, or the value provided by the supplier otherwise.
     * @param <T> the type of object
     * @param object the object to check
     * @param defaultSupplier the supplier of the default value
     * @return the object if not {@code null}, or the supplied default value
     */
    public static <T> @NonNull T defaultIfNull(@Nullable T object, @NonNull Supplier<? extends T> defaultSupplier) {
        Assert.notNull(defaultSupplier, "defaultSupplier must not be null");
        if (object != null) {
            return object;
        }
        T supplied = defaultSupplier.get();
        Assert.notNull(supplied, "defaultSupplier.get() must not be null");
        return supplied;
    }

    /**
     * Check whether the given array contains the given element.
     * @param array the array to check (may be {@code null})
     * @param element the element to check for
     * @return {@code true} if the array contains the given element; {@code false} otherwise
     */
    public static boolean containsElement(@Nullable Object[] array, @Nullable Object element) {
        if (array == null) {
            return false;
        }
        for (Object candidate : array) {
            if (nullSafeEquals(candidate, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Append the given object to the given array, returning a new array
     * containing all elements from the original array plus the given object.
     * @param <A> the array component type
     * @param <O> the object type
     * @param array the array to append to (may be {@code null})
     * @param obj the object to append
     * @return a new array with the given object appended
     */
    @SuppressWarnings("unchecked")
    public static <A, O extends A> A @NonNull [] addObjectToArray(@Nullable A[] array, @Nullable O obj) {
        Class<?> compType = Object.class;
        if (array != null) {
            compType = array.getClass().getComponentType();
        } else if (obj != null) {
            compType = obj.getClass();
        }
        int newArrLength = (array != null ? array.length + 1 : 1);
        A[] newArr = (A[]) Array.newInstance(compType, newArrLength);
        if (array != null) {
            System.arraycopy(array, 0, newArr, 0, array.length);
        }
        newArr[newArr.length - 1] = obj;
        return newArr;
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
    public static @NonNull Object[] toObjectArray(@Nullable Object source) {
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
    public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
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
            return Arrays.deepEquals(a1, a2);
        }
        if (o1 instanceof boolean[] a1 && o2 instanceof boolean[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof byte[] a1 && o2 instanceof byte[] a2) {
            return Arrays.equals(a1, a2);
        }
        if (o1 instanceof char[] a1 && o2 instanceof char[] a2) {
            return Arrays.equals(a1, a2);
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
     * @param obj the object
     * @return a hash code value for the given object
     * @see Object#hashCode()
     * @see Arrays
     */
    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[] arr) {
                return Arrays.deepHashCode(arr);
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
     * Return a String representation of the given object.
     * <p>Returns {@code "null"} if {@code obj} is {@code null},
     * the result of {@link Arrays#deepToString(Object[])} if {@code obj} is an Object array,
     * the result of the appropriate {@link Arrays#toString} method if {@code obj} is a primitive array,
     * or {@code obj.toString()} otherwise.</p>
     * @param obj the object to build a String representation for
     * @return a String representation of {@code obj}
     */
    public static @NonNull String nullSafeToString(@Nullable Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof Object[] arr) {
            return Arrays.deepToString(arr);
        }
        if (obj instanceof boolean[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof byte[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof char[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof double[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof float[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof int[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof long[] arr) {
            return Arrays.toString(arr);
        }
        if (obj instanceof short[] arr) {
            return Arrays.toString(arr);
        }
        String str = obj.toString();
        return (str != null ? str : StringUtils.EMPTY);
    }

    /**
     * Return a String representation of an object's overall identity.
     * @param obj the object (may be {@code null})
     * @return the object's identity as String representation,
     * or an empty String if the object was {@code null}
     */
    public static @NonNull String identityToString(@Nullable Object obj) {
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
    public static @NonNull String simpleIdentityToString(@Nullable Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        return (obj.getClass().isAnonymousClass() ? obj.getClass().getName() : obj.getClass().getSimpleName()) +
                "@" + getIdentityHexString(obj);
    }

    /**
     * Return a simple String representation of an object's overall identity, with its name.
     * @param obj the object (may be {@code null})
     * @param name the name to append
     * @return the object's identity as simple String representation,
     * or an empty String if the object was {@code null}
     */
    public static @NonNull String simpleIdentityToString(@Nullable Object obj, @NonNull String name) {
        return simpleIdentityToString(obj) + "(" + name + ")";
    }

    /**
     * Return a hex String form of an object's identity hash code.
     * @param obj the object
     * @return the object's identity code in hex notation
     */
    public static @NonNull String getIdentityHexString(@NonNull Object obj) {
        Assert.notNull(obj, "obj must not be null");
        return Integer.toHexString(System.identityHashCode(obj));
    }

}
