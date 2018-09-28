package com.aspectran.demo.examples;

import com.aspectran.core.activity.Translet;

import java.io.File;
import java.net.URL;

public class SampleResources {

    public String getSampleResourceFile(Translet translet) {
        ClassLoader classLoader = translet.getEnvironment().getClassLoader();
        URL url = classLoader.getResource("sample_resource.txt");
        String file = url.getFile();
        return file;
    }

}
