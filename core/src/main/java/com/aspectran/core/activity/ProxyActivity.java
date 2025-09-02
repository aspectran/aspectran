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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * <p>Created: 2025-08-27</p>
 */
public final class ProxyActivity extends AdviceActivity {

    private final Activity activity;

    private ActivityData activityData;

    /**
     * Instantiates a new AdviceActivity.
     * @param context the activity context
     */
    public ProxyActivity(ActivityContext context) {
        super(context);
        this.activity = null;
    }
    public ProxyActivity(@NonNull Activity activity) {
        super(activity.getActivityContext());
        this.activity = activity;
    }

    @Override
    public Mode getMode() {
        return Mode.PROXY;
    }

    public boolean hasActualActivity() {
        return (activity != null);
    }

    public Activity getActualActivity() {
        return activity;
    }

    @Override
    public void perform() throws ActivityPerformException {
        throw new UnsupportedOperationException();
    }

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

    @Override
    public Response getDeclaredResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResponseReserved() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResponded() {
        throw new UnsupportedOperationException();
    }

}
