package com.aspectran.freemarker;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.rule.ResourceAppendRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FreeMarkerTemplateEngineTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        ContextConfig contextConfig = aspectranConfig.newContextConfig();
        AspectranParameters parameters = contextConfig.newAspectranParameters();
        parameters.setDefaultTemplateEngineBean("freemarker");
        parameters.append(new ResourceAppendRule("config/freemarker-test-config.xml"));

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
    void testSelectStatement() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("select-template", params.extractAsMap());
        Translet translet = aspectran.translate("select-translet", params);

        assertEquals(result1, translet.toString());
    }

    @Test
    void testUpdateStatement() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("update-template", params.extractAsMap());
        Translet translet = aspectran.translate("update-translet", params);

        assertEquals(result1, translet.toString());
    }

}