package com.aspectran.undertow.server.handlers.http;

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.undertow.service.AspectranTowService;

/**
 * <p>Created: 2019-07-27</p>
 */
public class DefaultHttpHandler extends AbstractHttpHandler implements InitializableBean {

    private AspectranTowService towService;

    public AspectranTowService getTowService() {
        return towService;
    }

    @Override
    public void initialize() throws Exception {
        towService = AspectranTowService.create(getActivityContext().getRootService());
    }

}
