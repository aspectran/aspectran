package com.aspectran.web.socket.jsr356;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;

/**
 * <p>Created: 29/09/2019</p>
 */
public abstract class ActivityContextAwareEndpoint implements ActivityContextAware {

    private static ActivityContext context;

    public static ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        ActivityContextAwareEndpoint.context = context;
    }

}
