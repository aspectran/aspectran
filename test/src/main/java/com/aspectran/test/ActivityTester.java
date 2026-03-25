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
package com.aspectran.test;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.ActivityContext;
import org.jspecify.annotations.NonNull;

/**
 * Helper class for testing Aspectran in a general environment.
 * It provides a convenient way to execute logic within an {@link InstantActivity}.
 *
 * <p>Created: 2026. 3. 24.</p>
 */
public class ActivityTester {

    private final ActivityContext context;

    public ActivityTester(@NonNull ActivityContext context) {
        this.context = context;
    }

    /**
     * Retrieves the {@link ActivityContext} associated with this instance.
     * @return the {@link ActivityContext} used for managing and executing activities
     */
    public ActivityContext getActivityContext() {
        return context;
    }

    /**
     * Executes the given action within an InstantActivity.
     * @param action the action to execute
     * @param <V> the result type
     * @return the result of the action
     * @throws ActivityPerformException if an error occurs during activity performance
     */
    public <V> V perform(ActivityTestAction<V> action) throws ActivityPerformException {
        InstantActivity activity = new InstantActivity(context);
        return activity.perform(() -> action.execute(activity));
    }

    /**
     * Functional interface for testing logic within an activity.
     * @param <V> the result type
     */
    @FunctionalInterface
    public interface ActivityTestAction<V> {
        V execute(Activity activity) throws Exception;
    }

}
