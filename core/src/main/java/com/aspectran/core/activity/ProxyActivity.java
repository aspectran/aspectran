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
package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.ActivityContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A proxy or decorator for an {@link Activity} instance.
 * <p>This class is primarily used to provide an activity context for AOP advice
 * execution around an {@link InstantAction} without involving a full translet lifecycle.
 * It allows advice to be applied to arbitrary code blocks that are executed
 * as an {@code InstantAction}.</p>
 *
 * <p>Created: 2025-08-27</p>
 */
public final class ProxyActivity extends AdviceActivity {

    private final Activity activity;

    private ActivityData activityData;

    /**
     * Instantiates a new proxy activity without an actual underlying activity.
     * This is useful when only a context is needed for advice execution.
     * @param context the activity context
     */
    public ProxyActivity(ActivityContext context) {
        super(context);
        this.activity = null;
    }

    /**
     * Instantiates a new proxy activity that wraps an actual activity.
     * @param activity the actual activity to proxy
     */
    public ProxyActivity(@NonNull Activity activity) {
        super(activity.getActivityContext());
        this.activity = activity;
    }

    @Override
    public Mode getMode() {
        return Mode.PROXY;
    }

    /**
     * Returns whether this proxy wraps an actual activity.
     * @return true if there is an actual activity, false otherwise
     */
    public boolean hasActualActivity() {
        return (activity != null);
    }

    /**
     * Returns the actual activity that this proxy wraps.
     * @return the actual activity, or {@code null} if none
     */
    public Activity getActualActivity() {
        return activity;
    }

    /**
     * This operation is not supported in a proxy activity.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void perform() throws ActivityPerformException {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes an {@link InstantAction} within the context of this proxy activity.
     * This allows AOP advice to be applied to the action's execution.
     * @param instantAction the action to execute
     * @return the result of the instant action
     * @throws ActivityPerformException if an exception occurs during execution
     */
    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        try {
            saveCurrentActivity();
            return instantAction.execute();
        } catch (ActivityPerformException e) {
            setRaisedException(e);
            throw e;
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActivityPerformException("Failed to perform proxy activity for instant action " +
                    instantAction, e);
        } finally {
            removeCurrentActivity();
        }
    }

    /**
     * This operation is not supported in a proxy activity.
     * @throws UnsupportedOperationException always
     */
    @Override
    public Translet getTranslet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasTranslet() {
        return false;
    }

    @Override
    @Nullable
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public ActivityData getActivityData() {
        if (activity != null) {
            if (activityData == null) {
                activityData = activity.getActivityData();
            }
        } else {
            if (activityData == null) {
                activityData = new ActivityData(this);
            } else {
                activityData.refresh();
            }
        }
        return activityData;
    }

    /**
     * This operation is not supported in a proxy activity.
     * @throws UnsupportedOperationException always
     */
    @Override
    public Response getDeclaredResponse() {
        throw new UnsupportedOperationException();
    }

    /**
     * This operation is not supported in a proxy activity.
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean isResponseReserved() {
        throw new UnsupportedOperationException();
    }

    /**
     * This operation is not supported in a proxy activity.
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean isResponded() {
        throw new UnsupportedOperationException();
    }

}
