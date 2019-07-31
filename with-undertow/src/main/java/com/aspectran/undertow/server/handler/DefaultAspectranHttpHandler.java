package com.aspectran.undertow.server.handler;

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.undertow.service.AspectranTowService;

/**
 * <p>Created: 2019-07-27</p>
 */
public class DefaultAspectranHttpHandler extends AbstractAspectranHttpHandler implements InitializableBean {

    private AspectranTowService towService;

    public AspectranTowService getTowService() {
        return towService;
    }

    @Override
    public void initialize() throws Exception {
        towService = AspectranTowService.create(getActivityContext().getRootService());
    }

}
