package com.aspectran.pebble;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.ResourceAppendRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-03-18</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PebbleTemplateEngineTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranParameters parameters = aspectranConfig.newContextConfig().newAspectranParameters();
        parameters.setDefaultTemplateEngineBean("pebble");
        parameters.addRule(new ResourceAppendRule("config/pebble-test-config.xml"));

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        aspectran.release();
    }

    @Test
    void testEcho1() {
        Translet translet = aspectran.translate("echo-1");
        assertEquals("1234567890", translet.toString());
    }

    @Test
    void testEcho2() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("template-1", params.extractAsMap());
        Translet translet = aspectran.translate("translet-1", params);

        assertEquals(result1, translet.toString());
    }

}