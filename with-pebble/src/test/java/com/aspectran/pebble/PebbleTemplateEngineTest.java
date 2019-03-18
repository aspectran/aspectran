package com.aspectran.pebble;

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

/**
 * <p>Created: 2019-03-18</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PebbleTemplateEngineTest {

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
        ActivityContext context1 = builder.build("/config/pebble-test-config.xml");
        String result = context1.getTemplateRenderer().render("pebble");
        System.out.println(result);
    }

}