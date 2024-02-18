/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.web.support.util;

import com.aspectran.utils.Assert;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This class is a clone of org.springframework.web.util.UriUtils</p>

 * Utility methods for URI encoding and decoding based on RFC 3986.
 *
 * <p>There are two types of encode methods:
 * <ul>
 * <li>{@code "encodeXyz"} -- these encode a specific URI component (e.g. path,
 * query) by percent encoding illegal characters, which includes non-US-ASCII
 * characters, and also characters that are otherwise illegal within the given
 * URI component type, as defined in RFC 3986. The effect of this method, with
 * regards to encoding, is comparable to using the multi-argument constructor
 * of {@link URI}.
 * <li>{@code "encode"} and {@code "encodeUriVariables"} -- these can be used
 * to encode URI variable values by percent encoding all characters that are
 * either illegal, or have any reserved meaning, anywhere within a URI.
 * </ul>
 *
 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
 */
public abstract class UriUtils {

    /**
     * Encode the given URI scheme with the given encoding.
     * @param scheme the scheme to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded scheme
     */
    public static String encodeScheme(String scheme, String encoding) {
        return encode(scheme, encoding, UriComponentsType.SCHEME);
    }

    /**
     * Encode the given URI scheme with the given encoding.
     * @param scheme the scheme to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded scheme
     */
    public static String encodeScheme(String scheme, Charset charset) {
        return encode(scheme, charset, UriComponentsType.SCHEME);
    }

    /**
     * Encode the given URI authority with the given encoding.
     * @param authority the authority to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded authority
     */
    public static String encodeAuthority(String authority, String encoding) {
        return encode(authority, encoding, UriComponentsType.AUTHORITY);
    }

    /**
     * Encode the given URI authority with the given encoding.
     * @param authority the authority to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded authority
     */
    public static String encodeAuthority(String authority, Charset charset) {
        return encode(authority, charset, UriComponentsType.AUTHORITY);
    }

    /**
     * Encode the given URI user info with the given encoding.
     * @param userInfo the user info to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded user info
     */
    public static String encodeUserInfo(String userInfo, String encoding) {
        return encode(userInfo, encoding, UriComponentsType.USER_INFO);
    }

    /**
     * Encode the given URI user info with the given encoding.
     * @param userInfo the user info to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded user info
     */
    public static String encodeUserInfo(String userInfo, Charset charset) {
        return encode(userInfo, charset, UriComponentsType.USER_INFO);
    }

    /**
     * Encode the given URI host with the given encoding.
     * @param host the host to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded host
     */
    public static String encodeHost(String host, String encoding) {
        return encode(host, encoding, UriComponentsType.HOST_IPV4);
    }

    /**
     * Encode the given URI host with the given encoding.
     * @param host the host to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded host
     */
    public static String encodeHost(String host, Charset charset) {
        return encode(host, charset, UriComponentsType.HOST_IPV4);
    }

    /**
     * Encode the given URI port with the given encoding.
     * @param port the port to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded port
     */
    public static String encodePort(String port, String encoding) {
        return encode(port, encoding, UriComponentsType.PORT);
    }

    /**
     * Encode the given URI port with the given encoding.
     * @param port the port to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded port
     */
    public static String encodePort(String port, Charset charset) {
        return encode(port, charset, UriComponentsType.PORT);
    }

    /**
     * Encode the given URI path with the given encoding.
     * @param path the path to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded path
     */
    public static String encodePath(String path, String encoding) {
        return encode(path, encoding, UriComponentsType.PATH);
    }

    /**
     * Encode the given URI path with the given encoding.
     * @param path the path to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded path
     */
    public static String encodePath(String path, Charset charset) {
        return encode(path, charset, UriComponentsType.PATH);
    }

    /**
     * Encode the given URI path segment with the given encoding.
     * @param segment the segment to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded segment
     */
    public static String encodePathSegment(String segment, String encoding) {
        return encode(segment, encoding, UriComponentsType.PATH_SEGMENT);
    }

    /**
     * Encode the given URI path segment with the given encoding.
     * @param segment the segment to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded segment
     */
    public static String encodePathSegment(String segment, Charset charset) {
        return encode(segment, charset, UriComponentsType.PATH_SEGMENT);
    }

    /**
     * Encode the given URI query with the given encoding.
     * @param query the query to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded query
     */
    public static String encodeQuery(String query, String encoding) {
        return encode(query, encoding, UriComponentsType.QUERY);
    }

    /**
     * Encode the given URI query with the given encoding.
     * @param query the query to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded query
     */
    public static String encodeQuery(String query, Charset charset) {
        return encode(query, charset, UriComponentsType.QUERY);
    }

    /**
     * Encode the given URI query parameter with the given encoding.
     * @param queryParam the query parameter to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded query parameter
     */
    public static String encodeQueryParam(String queryParam, String encoding) {
        return encode(queryParam, encoding, UriComponentsType.QUERY_PARAM);
    }

    /**
     * Encode the given URI query parameter with the given encoding.
     * @param queryParam the query parameter to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded query parameter
     */
    public static String encodeQueryParam(String queryParam, Charset charset) {
        return encode(queryParam, charset, UriComponentsType.QUERY_PARAM);
    }

    /**
     * Encode the query parameters from the given {@code MultiValueMap} with UTF-8.
     * @param params the parameters to encode
     * @return a new {@code MultiValueMap} with the encoded names and values
     */
    @NonNull
    public static MultiValueMap<String, String> encodeQueryParams(@NonNull MultiValueMap<String, String> params) {
        Charset charset = StandardCharsets.UTF_8;
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(params.size());
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                result.add(encodeQueryParam(entry.getKey(), charset), encodeQueryParam(value, charset));
            }
        }
        return result;
    }

    /**
     * Encode the given URI fragment with the given encoding.
     * @param fragment the fragment to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded fragment
     */
    public static String encodeFragment(String fragment, String encoding) {
        return encode(fragment, encoding, UriComponentsType.FRAGMENT);
    }

    /**
     * Encode the given URI fragment with the given encoding.
     * @param fragment the fragment to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded fragment
     */
    public static String encodeFragment(String fragment, Charset charset) {
        return encode(fragment, charset, UriComponentsType.FRAGMENT);
    }


    /**
     * Variant of {@link #encode(String, Charset)} with a String charset.
     * @param source the String to be encoded
     * @param encoding the character encoding to encode to
     * @return the encoded String
     */
    public static String encode(String source, String encoding) {
        return encode(source, encoding, UriComponentsType.URI);
    }

    /**
     * Encode all characters that are either illegal, or have any reserved
     * meaning, anywhere within a URI, as defined in
     * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>.
     * This is useful to ensure that the given String will be preserved as-is
     * and will not have any impact on the structure or meaning of the URI.
     * @param source the String to be encoded
     * @param charset the character encoding to encode to
     * @return the encoded String
     */
    public static String encode(String source, Charset charset) {
        return encode(source, charset, UriComponentsType.URI);
    }

    /**
     * Convenience method to apply {@link #encode(String, Charset)} to all
     * given URI variable values.
     * @param uriVariables the URI variable values to be encoded
     * @return the encoded String
     */
    @NonNull
    public static Map<String, String> encodeUriVariables(@NonNull Map<String, ?> uriVariables) {
        Map<String, String> result = new LinkedHashMap<>((int) Math.ceil(uriVariables.size() / (double)0.75f));
        uriVariables.forEach((key, value) -> {
            String stringValue = (value != null ? value.toString() : "");
            result.put(key, encode(stringValue, StandardCharsets.UTF_8));
        });
        return result;
    }

    /**
     * Convenience method to apply {@link #encode(String, Charset)} to all
     * given URI variable values.
     * @param uriVariables the URI variable values to be encoded
     * @return the encoded String
     */
    @NonNull
    public static Object[] encodeUriVariables(Object... uriVariables) {
        return Arrays.stream(uriVariables)
                .map(value -> {
                    String stringValue = (value != null ? value.toString() : "");
                    return encode(stringValue, StandardCharsets.UTF_8);
                })
                .toArray();
    }

    private static String encode(String scheme, String encoding, UriComponentsType type) {
        return encodeUriComponent(scheme, encoding, type);
    }

    private static String encode(String scheme, Charset charset, UriComponentsType type) {
        return encodeUriComponent(scheme, charset, type);
    }

    /**
     * Encode the given source into an encoded String using the rules specified
     * by the given component and with the given options.
     * @param source the source String
     * @param encoding the encoding of the source String
     * @param type the URI component for the source
     * @return the encoded URI
     * @throws IllegalArgumentException when the given value is not a valid URI component
     */
    private static String encodeUriComponent(String source, String encoding, UriComponentsType type) {
        return encodeUriComponent(source, Charset.forName(encoding), type);
    }

    /**
     * Encode the given source into an encoded String using the rules specified
     * by the given component and with the given options.
     * @param source the source String
     * @param charset the encoding of the source String
     * @param type the URI component for the source
     * @return the encoded URI
     * @throws IllegalArgumentException when the given value is not a valid URI component
     */
    private static String encodeUriComponent(String source, Charset charset, UriComponentsType type) {
        if (!StringUtils.hasLength(source)) {
            return source;
        }
        Assert.notNull(charset, "Charset must not be null");
        Assert.notNull(type, "Type must not be null");

        byte[] bytes = source.getBytes(charset);
        boolean original = true;
        for (byte b : bytes) {
            if (!type.isAllowed(b)) {
                original = false;
                break;
            }
        }
        if (original) {
            return source;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        for (byte b : bytes) {
            if (type.isAllowed(b)) {
                baos.write(b);
            } else {
                baos.write('%');
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                baos.write(hex1);
                baos.write(hex2);
            }
        }
        return baos.toString(charset);
    }

    /**
     * Decode the given encoded URI component.
     * @param source the encoded String
     * @param encoding the character encoding to use
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see java.net.URLDecoder#decode(String, String)
     */
    public static String decode(String source, String encoding) {
        return decode(source, Charset.forName(encoding));
    }

    /**
     * Decode the given encoded URI component value. Based on the following rules:
     * <ul>
     * <li>Alphanumeric characters {@code "a"} through {@code "z"}, {@code "A"} through {@code "Z"},
     * and {@code "0"} through {@code "9"} stay the same.</li>
     * <li>Special characters {@code "-"}, {@code "_"}, {@code "."}, and {@code "*"} stay the same.</li>
     * <li>A sequence "{@code %<i>xy</i>}" is interpreted as a hexadecimal representation of the character.</li>
     * </ul>
     * @param source the encoded String
     * @param charset the character set
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see java.net.URLDecoder#decode(String, String)
     */
    public static String decode(@NonNull String source, Charset charset) {
        int length = source.length();
        if (length == 0) {
            return source;
        }
        Assert.notNull(charset, "Charset must not be null");

        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int ch = source.charAt(i);
            if (ch == '%') {
                if (i + 2 < length) {
                    char hex1 = source.charAt(i + 1);
                    char hex2 = source.charAt(i + 2);
                    int u = Character.digit(hex1, 16);
                    int l = Character.digit(hex2, 16);
                    if (u == -1 || l == -1) {
                        throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                    }
                    baos.write((char) ((u << 4) + l));
                    i += 2;
                    changed = true;
                } else {
                    throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                }
            } else {
                baos.write(ch);
            }
        }
        return (changed ? baos.toString(charset) : source);
    }

    /**
     * Extract the file extension from the given URI path.
     * @param path the URI path (e.g. "/products/index.html")
     * @return the extracted file extension (e.g. "html")
     */
    @Nullable
    public static String extractFileExtension(@NonNull String path) {
        int end = path.indexOf('?');
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex != -1 && (end == -1 || fragmentIndex < end)) {
            end = fragmentIndex;
        }
        if (end == -1) {
            end = path.length();
        }
        int begin = path.lastIndexOf('/', end) + 1;
        int paramIndex = path.indexOf(';', begin);
        end = (paramIndex != -1 && paramIndex < end ? paramIndex : end);
        int extIndex = path.lastIndexOf('.', end);
        if (extIndex != -1 && extIndex >= begin) {
            return path.substring(extIndex + 1, end);
        }
        return null;
    }

    // Nested types

    /**
     * Enumeration used to identify the allowed characters per URI component.
     * <p>Contains methods to indicate whether a given character is valid in a specific URI component.
     * @see <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>
     */
    enum UriComponentsType {

        SCHEME {
            @Override
            public boolean isAllowed(int c) {
                return isAlpha(c) || isDigit(c) || '+' == c || '-' == c || '.' == c;
            }
        },
        AUTHORITY {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c;
            }
        },
        USER_INFO {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || ':' == c;
            }
        },
        HOST_IPV4 {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c);
            }
        },
        HOST_IPV6 {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c) || isSubDelimiter(c) || '[' == c || ']' == c || ':' == c;
            }
        },
        PORT {
            @Override
            public boolean isAllowed(int c) {
                return isDigit(c);
            }
        },
        PATH {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c;
            }
        },
        PATH_SEGMENT {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c);
            }
        },
        QUERY {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }
        },
        QUERY_PARAM {
            @Override
            public boolean isAllowed(int c) {
                if ('=' == c || '&' == c) {
                    return false;
                } else {
                    return isPchar(c) || '/' == c || '?' == c;
                }
            }
        },
        FRAGMENT {
            @Override
            public boolean isAllowed(int c) {
                return isPchar(c) || '/' == c || '?' == c;
            }
        },
        URI {
            @Override
            public boolean isAllowed(int c) {
                return isUnreserved(c);
            }
        };

        /**
         * Indicates whether the given character is allowed in this URI component.
         * @return {@code true} if the character is allowed; {@code false} otherwise
         */
        public abstract boolean isAllowed(int c);

        /**
         * Indicates whether the given character is in the {@code ALPHA} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isAlpha(int c) {
            return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
        }

        /**
         * Indicates whether the given character is in the {@code DIGIT} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isDigit(int c) {
            return (c >= '0' && c <= '9');
        }

        /**
         * Indicates whether the given character is in the {@code gen-delims} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isGenericDelimiter(int c) {
            return (':' == c || '/' == c || '?' == c || '#' == c || '[' == c || ']' == c || '@' == c);
        }

        /**
         * Indicates whether the given character is in the {@code sub-delims} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isSubDelimiter(int c) {
            return ('!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || '*' == c || '+' == c ||
                    ',' == c || ';' == c || '=' == c);
        }

        /**
         * Indicates whether the given character is in the {@code reserved} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isReserved(int c) {
            return (isGenericDelimiter(c) || isSubDelimiter(c));
        }

        /**
         * Indicates whether the given character is in the {@code unreserved} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isUnreserved(int c) {
            return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
        }

        /**
         * Indicates whether the given character is in the {@code pchar} set.
         * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
         */
        protected boolean isPchar(int c) {
            return (isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c);
        }
    }

}
