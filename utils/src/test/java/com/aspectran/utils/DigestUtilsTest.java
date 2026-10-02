/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DigestUtilsTest {

    private static final String INPUT = "hello";
    private static final String MD5_HEX = "5d41402abc4b2a76b9719d911017c592";
    private static final String SHA1_HEX = "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d";
    private static final String SHA256_HEX = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
    private static final String SHA512_HEX =
            "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043";

    @Test
    void testMd5Digest() throws IOException {
        assertEquals(MD5_HEX, DigestUtils.md5DigestAsHex(INPUT));
        assertEquals(MD5_HEX, DigestUtils.md5DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8)));

        try (ByteArrayInputStream in = new ByteArrayInputStream(INPUT.getBytes(StandardCharsets.UTF_8))) {
            assertEquals(MD5_HEX, DigestUtils.md5DigestAsHex(in));
        }

        StringBuilder sb = new StringBuilder("prefix-");
        DigestUtils.appendMd5DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8), sb);
        assertEquals("prefix-" + MD5_HEX, sb.toString());
    }

    @Test
    void testSha1Digest() throws IOException {
        assertEquals(SHA1_HEX, DigestUtils.sha1DigestAsHex(INPUT));
        assertEquals(SHA1_HEX, DigestUtils.sha1DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8)));

        try (ByteArrayInputStream in = new ByteArrayInputStream(INPUT.getBytes(StandardCharsets.UTF_8))) {
            assertEquals(SHA1_HEX, DigestUtils.sha1DigestAsHex(in));
        }

        StringBuilder sb = new StringBuilder();
        DigestUtils.appendSha1DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8), sb);
        assertEquals(SHA1_HEX, sb.toString());
    }

    @Test
    void testSha256Digest() throws IOException {
        assertEquals(SHA256_HEX, DigestUtils.sha256DigestAsHex(INPUT));
        assertEquals(SHA256_HEX, DigestUtils.sha256DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8)));

        try (ByteArrayInputStream in = new ByteArrayInputStream(INPUT.getBytes(StandardCharsets.UTF_8))) {
            assertEquals(SHA256_HEX, DigestUtils.sha256DigestAsHex(in));
        }

        StringBuilder sb = new StringBuilder();
        DigestUtils.appendSha256DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8), sb);
        assertEquals(SHA256_HEX, sb.toString());
    }

    @Test
    void testSha512Digest() throws IOException {
        assertEquals(SHA512_HEX, DigestUtils.sha512DigestAsHex(INPUT));
        assertEquals(SHA512_HEX, DigestUtils.sha512DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8)));

        try (ByteArrayInputStream in = new ByteArrayInputStream(INPUT.getBytes(StandardCharsets.UTF_8))) {
            assertEquals(SHA512_HEX, DigestUtils.sha512DigestAsHex(in));
        }

        StringBuilder sb = new StringBuilder();
        DigestUtils.appendSha512DigestAsHex(INPUT.getBytes(StandardCharsets.UTF_8), sb);
        assertEquals(SHA512_HEX, sb.toString());
    }

    @Test
    void testGenericDigest() throws IOException {
        byte[] bytes = INPUT.getBytes(StandardCharsets.UTF_8);
        assertNotNull(DigestUtils.digest(DigestUtils.SHA_256_ALGORITHM_NAME, bytes));
        assertEquals(SHA256_HEX, DigestUtils.digestAsHexString(DigestUtils.SHA_256_ALGORITHM_NAME, bytes));

        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            assertEquals(SHA256_HEX, DigestUtils.digestAsHexString(DigestUtils.SHA_256_ALGORITHM_NAME, in));
        }
    }

    @Test
    void testEncodeHex() {
        byte[] bytes = new byte[] { (byte)0x00, (byte)0x0f, (byte)0x10, (byte)0xff };
        char[] chars = DigestUtils.encodeHex(bytes);
        assertArrayEquals(new char[] { '0', '0', '0', 'f', '1', '0', 'f', 'f' }, chars);
        assertEquals("000f10ff", DigestUtils.encodeHexString(bytes));
    }

}
