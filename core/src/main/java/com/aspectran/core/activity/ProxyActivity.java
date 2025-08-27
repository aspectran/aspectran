package com.aspectran.core.activity;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.ActivityContext;

/**
 * <p>Created: 2025-08-27</p>
 */
public final class ProxyActivity extends AdviceActivity {

    /**
     * Instantiates a new AdviceActivity.
     * @param context the activity context
     */
    public ProxyActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public Mode getMode() {
        return Mode.PROXY;
    }

    @Override
    public void perform() throws ActivityPerformException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        throw new UnsupportedOperationException();
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
    public ProcessResult getProcessResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProcessResult(String actionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActivityData getActivityData() {
        throw new UnsupportedOperationException();
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
