package com.aspectran.web.socket;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;

/**
 * <p>Created: 29/09/2019</p>
 */
public class AbstractEndpoint implements ActivityContextAware {

    private static ActivityContext context;

    public static ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        AbstractEndpoint.context = context;
    }

}
