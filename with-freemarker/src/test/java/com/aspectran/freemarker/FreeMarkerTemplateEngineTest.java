package com.aspectran.freemarker;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.util.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FreeMarkerTemplateEngineTest {

    private ActivityContextBuilder builder;

    @BeforeAll
    void ready() throws IOException {
        File baseDir = ResourceUtils.getResourceAsFile(".");
        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
    }

    @AfterAll
    void finish() {
        builder.destroy();
    }

    @Test
    void testHybridLoading() throws ActivityContextBuilderException {
        ActivityContext context1 = builder.build("/config/freemarker-test-config.xml");
        ParameterMap params = new ParameterMap();
        params.setParameter("id", "0001");
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");
        String result = context1.getTemplateRenderer().render("sampleSql", params.extractParameters());
        System.out.println(result);
    }

}