/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Miscellaneous class utility methods.
 */
public abstract class ClassUtils {

    /** The package separator character '.' */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Method that can be called to try to create an instantiate of
     * specified type. Instantiation is done using default no-argument
     * constructor.
     *
     * @param canFixAccess Whether it is possible to try to change access
     *   rights of the default constructor (in case it is not publicly
     *   accessible) or not.
     *
     * @throws IllegalArgumentException If instantiation fails for any reason;
     *    except for cases where constructor throws an unchecked exception
     *    (which will be passed as is)
     */
    public static <T> T createInstance(Class<T> cls, boolean canFixAccess)
            throws IllegalArgumentException {
        Constructor<T> ctor = findConstructor(cls, canFixAccess);
        if (ctor == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            ExceptionUtils.unwrapAndThrowAsIAE(e, "Failed to instantiate class " + cls.getName() + ", problem: " + e.getMessage());
            return null;
        }
    }

    public static <T> Constructor<T> findConstructor(Class<T> cls, boolean forceAccess)
            throws IllegalArgumentException {
        try {
            Constructor<T> ctor = cls.getDeclaredConstructor();
            if (forceAccess) {
                checkAndFixAccess(ctor, forceAccess);
            } else {
                // Has to be public...
                if (!Modifier.isPublic(ctor.getModifiers())) {
                    throw new IllegalArgumentException("Default constructor for " + cls.getName() + " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
                }
            }
            return ctor;
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (Exception e) {
            ExceptionUtils.unwrapAndThrowAsIAE(e, "Failed to find default constructor of class " + cls.getName() + ", problem: " + e.getMessage());
        }
        return null;
    }

    /**
     * Method that is called if a {@link Member} may need forced access,
     * to force a field, method or constructor to be accessible: this
     * is done by calling {@link AccessibleObject#setAccessible(boolean)}.
     *
     * @param member Accessor to call <code>setAccessible()</code> on.
     * @param force Whether to always try to make accessor accessible (true),
     *   or only if needed as per access rights (false)
     *
     * @since 5.0.0
     */
    public static void checkAndFixAccess(Member member, boolean force) {
        // We know all members are also accessible objects...
        AccessibleObject ao = (AccessibleObject)member;

        /* 14-Jan-2009, tatu: It seems safe and potentially beneficial to
         *   always to make it accessible (latter because it will force
         *   skipping checks we have no use for...), so let's always call it.
         */
        try {
            if (force ||
                    (!Modifier.isPublic(member.getModifiers())
                            || !Modifier.isPublic(member.getDeclaringClass().getModifiers()))) {
                ao.setAccessible(true);
            }
        } catch (SecurityException se) {
            Class<?> declClass = member.getDeclaringClass();
            throw new IllegalArgumentException("Can not access " + member + " (from class " + declClass.getName() + "; failed to set access: " + se.getMessage());
        }
    }

}
