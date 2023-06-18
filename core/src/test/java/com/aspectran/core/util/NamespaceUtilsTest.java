package com.aspectran.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2023/06/18</p>
 */
class NamespaceUtilsTest {

    @Test
    void splitNamespace() {
        String namespace = ".abc.def.ghi.";
        String[] nameArray = NamespaceUtils.splitNamespace(namespace);
        assertArrayEquals(new String[] {"abc", "def", "ghi"}, nameArray);
    }

    @Test
    void applyNamespace() {
        String namespace = "abc.def.ghi";
        String[] nameArray = NamespaceUtils.splitNamespace(namespace);
        String result = NamespaceUtils.applyNamespace(nameArray, "jkl.mno");
        assertEquals("abc.def.ghi.jkl.mno", result);
    }

    @Test
    void applyNamespaceForTranslet() {
        String namespace = "abc.def.ghi";
        String[] nameArray = NamespaceUtils.splitNamespace(namespace);
        String result = NamespaceUtils.applyNamespaceForTranslet(nameArray, "/jkl/mno");
        assertEquals("abc/def/ghi/jkl/mno", result);
    }

    @Test
    void applyNamespaceForTranslet2() {
        String namespace = "/abc/def.ghi";
        String[] nameArray = NamespaceUtils.splitNamespace(namespace);
        String result = NamespaceUtils.applyNamespaceForTranslet(nameArray, "/jkl/mno/");
        assertEquals("/abc/def/ghi/jkl/mno/", result);
    }

    @Test
    void applyNamespaceForTranslet3() {
        String namespace = "/abc/def.ghi/";
        String[] nameArray = NamespaceUtils.splitNamespace(namespace);
        String result = NamespaceUtils.applyNamespaceForTranslet(nameArray, "");
        assertEquals("/abc/def/ghi/", result);
    }

    @Test
    void applyTransletNamePattern() {
        String prefix = "/abc/def";
        String name = "/ghi";
        String suffix = ".apon";
        String result = NamespaceUtils.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("/abc/def/ghi.apon", result);
    }

    @Test
    void applyTransletNamePattern2() {
        String prefix = "abc/def_";
        String name = "ghi/jkl";
        String suffix = ".apon";
        String result = NamespaceUtils.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("abc/def_ghi/jkl.apon", result);
    }

    @Test
    void applyTransletNamePattern3() {
        String prefix = "/abc/def";
        String name = "/ghi";
        String suffix = "/jkl";
        String result = NamespaceUtils.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("/abc/def/ghi/jkl", result);
    }

    @Test
    void applyTransletNamePattern4() {
        String prefix = "//abc//def";
        String name = "//ghi";
        String suffix = "//jkl//";
        String result = NamespaceUtils.applyTransletNamePattern(prefix, name, suffix, false);
        assertEquals("//abc//def//ghi//jkl//", result);
    }

}
