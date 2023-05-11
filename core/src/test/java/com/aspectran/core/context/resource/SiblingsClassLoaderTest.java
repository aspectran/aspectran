package com.aspectran.core.context.resource;

import com.aspectran.core.util.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class SiblingsClassLoaderTest {

    @Test
    void testSiblingsClassLoader() throws IOException, InvalidResourceException, ClassNotFoundException {
//        File current = ResourceUtils.getResourceAsFile(".");
//        File root = new File(current, "../../../../aspectow/app");
//        File reloadable = new File(root, "lib/reloadable");
//        System.out.println(reloadable.getCanonicalPath());
//
//        SiblingsClassLoader scl = new SiblingsClassLoader();
//        scl.setResourceLocations(reloadable.getCanonicalPath());
//        Class<?> helloActivity1 = scl.loadClass("app.demo.examples.hello.HelloActivity");
//        System.out.println(helloActivity1);
//        scl.reload();
//        Class<?> helloActivity2 = scl.loadClass("app.demo.examples.hello.HelloActivity");
//        System.out.println(helloActivity2);
//
//        SiblingsClassLoader scl2 = new SiblingsClassLoader();
//        scl2.setResourceLocations(reloadable.getCanonicalPath());
//        Class<?> helloActivity3 = scl2.loadClass("app.demo.examples.hello.HelloActivity");
//        System.out.println(helloActivity3);

    }

}