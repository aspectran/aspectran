package com.aspectran.thymeleaf;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.ResourceAppendRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThymeleafTemplateEngineTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranParameters parameters = aspectranConfig.newContextConfig().newAspectranParameters();
        parameters.setDefaultTemplateEngineBean("thymeleaf");
        parameters.addRule(new ResourceAppendRule("config/thymeleaf-test-config.xml"));

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        aspectran.release();
    }

    @Test
    void testEcho1() {
        Translet translet = aspectran.translate("echo-1");
        assertEquals("1234567890", translet.getWrittenResponse());
    }

    @Test
    void testEcho2() {
//        ParameterMap params = new ParameterMap();
//        params.setParameter("name", "tester");
//        params.setParameter("email", "tester@aspectran.com");
//
//        String result1 = aspectran.render("template-1", params.extractAsMap());
//        String result2 = aspectran.translate("translet-1", params).getWrittenResponse();
//        String result3 = aspectran.translate("translet-2", params).getWrittenResponse();
//
//        //System.out.println(result1);
//        //System.out.println(result2);
//        //System.out.println(result3);
//
//        assertEquals(result1, result2);
//        assertEquals(result1, result3);
    }

}
