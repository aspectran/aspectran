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
package com.aspectran.utils.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpAddressUtilsTest {

    @Test
    void testIsValidIPv6() {
        assertTrue(IpAddressUtils.isValidIPv6("::1"));
        assertTrue(IpAddressUtils.isValidIPv6("::"));
        assertTrue(IpAddressUtils.isValidIPv6("1::"));
        assertTrue(IpAddressUtils.isValidIPv6("1:2:3:4:5:6:7:8"));
        assertTrue(IpAddressUtils.isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(IpAddressUtils.isValidIPv6("2001:db8:85a3::8a2e:370:7334"));
        assertTrue(IpAddressUtils.isValidIPv6("fe80::1"));
        assertTrue(IpAddressUtils.isValidIPv6("::8"));
        assertTrue(IpAddressUtils.isValidIPv6("::7:8"));
        assertTrue(IpAddressUtils.isValidIPv6("1:2:3::8"));
        assertTrue(IpAddressUtils.isValidIPv6("1:2::8"));

        assertFalse(IpAddressUtils.isValidIPv6("1.1.1.1"));
        assertFalse(IpAddressUtils.isValidIPv6("1:2:3:4:5:6:7:8:9"));
        assertFalse(IpAddressUtils.isValidIPv6("1:2:3:::8"));
        assertFalse(IpAddressUtils.isValidIPv6("1:2:3:4:5:6:7:"));
        assertFalse(IpAddressUtils.isValidIPv6(":1:2:3:4:5:6:7"));
        assertFalse(IpAddressUtils.isValidIPv6("1111:4444:7777:aaaa:bbbb:cccc:dddd:ffff:"));
        assertFalse(IpAddressUtils.isValidIPv6("1111:4444:7777:aaaa:bbbb:cccc:dddd::ffff"));
        assertFalse(IpAddressUtils.isValidIPv6("gggg::1"));
        assertFalse(IpAddressUtils.isValidIPv6(null));
        assertFalse(IpAddressUtils.isValidIPv6(""));
    }

    @Test
    void testNormalizeIPv6() {
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", IpAddressUtils.normalizeIPv6("::1"));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0000", IpAddressUtils.normalizeIPv6("::"));
        assertEquals("0001:0000:0000:0000:0000:0000:0000:0000", IpAddressUtils.normalizeIPv6("1::"));
        assertEquals("0001:0002:0003:0004:0005:0006:0007:0008", IpAddressUtils.normalizeIPv6("1:2:3:4:5:6:7:8"));
        assertEquals("0001:0002:0003:0000:0000:0000:0000:0008", IpAddressUtils.normalizeIPv6("1:2:3::8"));
        assertEquals("0001:0002:0000:0000:0000:0000:0000:0008", IpAddressUtils.normalizeIPv6("1:2::8"));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0008", IpAddressUtils.normalizeIPv6("::8"));
        assertEquals("0000:0000:0000:0000:0000:0000:0007:0008", IpAddressUtils.normalizeIPv6("::7:8"));
        assertEquals("1111:4444:7777:aaaa:bbbb:cccc:dddd:ffff",
                IpAddressUtils.normalizeIPv6("1111:4444:7777:aaaa:bbbb:cccc:dddd:ffff"));

        assertNull(IpAddressUtils.normalizeIPv6("1.1.1.1"));
        assertNull(IpAddressUtils.normalizeIPv6("invalid"));
        assertNull(IpAddressUtils.normalizeIPv6(null));
    }

    @Test
    void testIsAllowedIp() {
        assertTrue(IpAddressUtils.isAllowedIp("127.0.0.1", "127.0.0.1"));
        assertTrue(IpAddressUtils.isAllowedIp("192.168.1.50", "192.168.1.*"));
        assertTrue(IpAddressUtils.isAllowedIp("10.0.5.1", "10.0.0.0/8"));
        assertFalse(IpAddressUtils.isAllowedIp("172.16.0.1", "192.168.1.*, 10.0.0.0/8"));
    }

}
