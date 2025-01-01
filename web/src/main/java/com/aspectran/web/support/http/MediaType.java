/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.web.support.http;

import com.aspectran.utils.Assert;
import com.aspectran.utils.LinkedCaseInsensitiveMap;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * <p>This class is a clone of org.springframework.http.MediaType</p>
 *
 * Represents an Internet Media Type, as defined in the HTTP specification.
 * <p>This class contain support for the q-parameters used in HTTP content negotiation.</p>
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @author Kazuki Shimizu
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.1">
 * HTTP 1.1: Semantics and Content, section 3.1.1.1</a>
 */
public class MediaType implements Comparable<MediaType>, Serializable {

    @Serial
    private static final long serialVersionUID = 6574317082451901361L;

    /**
     * Public constant media type that includes all media ranges (i.e. "&#42;/&#42;").
     */
    public static final MediaType ALL;

    /**
     * A String equivalent of {@link MediaType#ALL}.
     */
    public static final String ALL_VALUE = "*/*";

    /**
     * Public constant media type for {@code application/atom+xml}.
     */
    public static final MediaType APPLICATION_ATOM_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_ATOM_XML}.
     */
    public static final String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";

    /**
     * Public constant media type for {@code application/cbor}.
     */
    public static final MediaType APPLICATION_CBOR;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_CBOR}.
     */
    public static final String APPLICATION_CBOR_VALUE = "application/cbor";

    /**
     * Public constant media type for {@code application/x-www-form-urlencoded}.
     */
    public static final MediaType APPLICATION_FORM_URLENCODED;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_FORM_URLENCODED}.
     */
    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

    /**
     * Public constant media type for {@code application/graphql}.
     */
    public static final MediaType APPLICATION_GRAPHQL;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_GRAPHQL}.
     */
    public static final String APPLICATION_GRAPHQL_VALUE = "application/graphql";

    /**
     * Public constant media type for {@code application/json}.
     */
    public static final MediaType APPLICATION_JSON;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_JSON}.
     */
    public static final String APPLICATION_JSON_VALUE = "application/json";

    /**
     * Public constant media type for {@code application/x-ndjson}.
     */
    public static final MediaType APPLICATION_NDJSON;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_NDJSON}.
     */
    public static final String APPLICATION_NDJSON_VALUE = "application/x-ndjson";

    /**
     * Public constant media type for {@code application/json-seq}.
     * @see <a href="https://tools.ietf.org/html/rfc7464#section-4">
     *     JavaScript Object Notation (JSON) Text Sequences</a>
     */
    public static final MediaType APPLICATION_JSON_SEQ;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_JSON_SEQ}.
     */
    public static final String APPLICATION_JSON_SEQ_VALUE = "application/json-seq";

    /**
     * Public constant media type for {@code application/apon}.
     */
    public static final MediaType APPLICATION_APON;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_JSON}.
     */
    public static final String APPLICATION_APON_VALUE = "application/apon";

    /**
     * Public constant media type for {@code application/octet-stream}.
     */
    public static final MediaType APPLICATION_OCTET_STREAM;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_OCTET_STREAM}.
     */
    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

    /**
     * Public constant media type for {@code application/pdf}.
     */
    public static final MediaType APPLICATION_PDF;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_PDF}.
     */
    public static final String APPLICATION_PDF_VALUE = "application/pdf";

    /**
     * Public constant media type for {@code application/problem+xml}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807#section-6.2">
     * Problem Details for HTTP APIs, 6.2. application/problem+xml</a>
     */
    public static final MediaType APPLICATION_PROBLEM_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_PROBLEM_XML}.
     */
    public static final String APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml";

    /**
     * Public constant media type for {@code application/rss+xml}.
     */
    public static final MediaType APPLICATION_RSS_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_RSS_XML}.
     */
    public static final String APPLICATION_RSS_XML_VALUE = "application/rss+xml";

    /**
     * Public constant media type for {@code application/xhtml+xml}.
     */
    public static final MediaType APPLICATION_XHTML_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_XHTML_XML}.
     */
    public static final String APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml";

    /**
     * Public constant media type for {@code application/xml}.
     */
    public static final MediaType APPLICATION_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_XML}.
     */
    public static final String APPLICATION_XML_VALUE = "application/xml";

    /**
     * Public constant media type for {@code image/gif}.
     */
    public static final MediaType IMAGE_GIF;

    /**
     * A String equivalent of {@link MediaType#IMAGE_GIF}.
     */
    public static final String IMAGE_GIF_VALUE = "image/gif";

    /**
     * Public constant media type for {@code image/jpeg}.
     */
    public static final MediaType IMAGE_JPEG;

    /**
     * A String equivalent of {@link MediaType#IMAGE_JPEG}.
     */
    public static final String IMAGE_JPEG_VALUE = "image/jpeg";

    /**
     * Public constant media type for {@code image/png}.
     */
    public static final MediaType IMAGE_PNG;

    /**
     * A String equivalent of {@link MediaType#IMAGE_PNG}.
     */
    public static final String IMAGE_PNG_VALUE = "image/png";

    /**
     * Public constant media type for {@code multipart/form-data}.
     */
    public static final MediaType MULTIPART_FORM_DATA;

    /**
     * A String equivalent of {@link MediaType#MULTIPART_FORM_DATA}.
     */
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    /**
     * Public constant media type for {@code multipart/mixed}.
     */
    public static final MediaType MULTIPART_MIXED;

    /**
     * A String equivalent of {@link MediaType#MULTIPART_MIXED}.
     */
    public static final String MULTIPART_MIXED_VALUE = "multipart/mixed";

    /**
     * Public constant media type for {@code multipart/related}.
     */
    public static final MediaType MULTIPART_RELATED;

    /**
     * A String equivalent of {@link MediaType#MULTIPART_RELATED}.
     */
    public static final String MULTIPART_RELATED_VALUE = "multipart/related";

    /**
     * Public constant media type for {@code text/event-stream}.
     *
     * @see <a href="https://www.w3.org/TR/eventsource/">Server-Sent Events W3C recommendation</a>
     */
    public static final MediaType TEXT_EVENT_STREAM;

    /**
     * A String equivalent of {@link MediaType#TEXT_EVENT_STREAM}.
     */
    public static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream";

    /**
     * Public constant media type for {@code text/html}.
     */
    public static final MediaType TEXT_HTML;

    /**
     * A String equivalent of {@link MediaType#TEXT_HTML}.
     */
    public static final String TEXT_HTML_VALUE = "text/html";

    /**
     * Public constant media type for {@code text/markdown}.
     */
    public static final MediaType TEXT_MARKDOWN;

    /**
     * A String equivalent of {@link MediaType#TEXT_MARKDOWN}.
     */
    public static final String TEXT_MARKDOWN_VALUE = "text/markdown";

    /**
     * Public constant media type for {@code text/plain}.
     */
    public static final MediaType TEXT_PLAIN;

    /**
     * A String equivalent of {@link MediaType#TEXT_PLAIN}.
     */
    public static final String TEXT_PLAIN_VALUE = "text/plain";

    /**
     * Public constant media type for {@code text/xml}.
     */
    public static final MediaType TEXT_XML;

    /**
     * A String equivalent of {@link MediaType#TEXT_XML}.
     */
    public static final String TEXT_XML_VALUE = "text/xml";

    private static final String PARAM_QUALITY_FACTOR = "q";

    public static final String WILDCARD_TYPE = "*";

    public static final String PARAM_CHARSET = "charset";

    private static final BitSet TOKEN;

    static {
        // variable names refer to RFC 2616, section 2.2
        BitSet ctl = new BitSet(128);
        for (int i = 0; i <= 31; i++) {
            ctl.set(i);
        }
        ctl.set(127);

        BitSet separators = new BitSet(128);
        separators.set('(');
        separators.set(')');
        separators.set('<');
        separators.set('>');
        separators.set('@');
        separators.set(',');
        separators.set(';');
        separators.set(':');
        separators.set('\\');
        separators.set('\"');
        separators.set('/');
        separators.set('[');
        separators.set(']');
        separators.set('?');
        separators.set('=');
        separators.set('{');
        separators.set('}');
        separators.set(' ');
        separators.set('\t');

        TOKEN = new BitSet(128);
        TOKEN.set(0, 128);
        TOKEN.andNot(ctl);
        TOKEN.andNot(separators);

        // Not using 'valueOf' to avoid static init cost
        ALL = new MediaType("*", "*");
        APPLICATION_ATOM_XML = new MediaType("application", "atom+xml");
        APPLICATION_CBOR = new MediaType("application", "cbor");
        APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
        APPLICATION_GRAPHQL = new MediaType("application", "graphql");
        APPLICATION_JSON = new MediaType("application", "json");
        APPLICATION_NDJSON = new MediaType("application", "x-ndjson");
        APPLICATION_JSON_SEQ = new MediaType("application", "json-seq");
        APPLICATION_APON = new MediaType("application", "apon");
        APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
        APPLICATION_PDF = new MediaType("application", "pdf");
        APPLICATION_PROBLEM_XML = new MediaType("application", "problem+xml");
        APPLICATION_RSS_XML = new MediaType("application", "rss+xml");
        APPLICATION_XHTML_XML = new MediaType("application", "xhtml+xml");
        APPLICATION_XML = new MediaType("application", "xml");
        IMAGE_GIF = new MediaType("image", "gif");
        IMAGE_JPEG = new MediaType("image", "jpeg");
        IMAGE_PNG = new MediaType("image", "png");
        MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
        MULTIPART_MIXED = new MediaType("multipart", "mixed");
        MULTIPART_RELATED = new MediaType("multipart", "related");
        TEXT_EVENT_STREAM = new MediaType("text", "event-stream");
        TEXT_HTML = new MediaType("text", "html");
        TEXT_MARKDOWN = new MediaType("text", "markdown");
        TEXT_PLAIN = new MediaType("text", "plain");
        TEXT_XML = new MediaType("text", "xml");
    }

    private final String type;

    private final String subtype;

    private final Map<String, String> parameters;

    @Nullable
    private transient Charset resolvedCharset;

    @Nullable
    private volatile String toStringValue;

    /**
     * Create a new {@code MediaType} for the given primary type.
     * <p>The {@linkplain #getSubtype() subtype} is set to "&#42;", parameters empty.</p>
     * @param type the primary type
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(String type) {
        this(type, WILDCARD_TYPE);
    }

    /**
     * Create a new {@code MediaType} for the given primary type and subtype.
     * <p>The parameters are empty.</p>
     * @param type the primary type
     * @param subtype the subtype
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(String type, String subtype) {
        this(type, subtype, Collections.emptyMap());
    }

    /**
     * Create a new {@code MediaType} for the given type, subtype, and character set.
     * @param type the primary type
     * @param subtype the subtype
     * @param charset the character set
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(String type, String subtype, @NonNull Charset charset) {
        this(type, subtype, Collections.singletonMap(PARAM_CHARSET, charset.name()));
        this.resolvedCharset = charset;
    }

    /**
     * Create a new {@code MediaType} for the given type, subtype, and quality value.
     * @param type the primary type
     * @param subtype the subtype
     * @param qualityValue the quality value
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(String type, String subtype, double qualityValue) {
        this(type, subtype, Collections.singletonMap(PARAM_QUALITY_FACTOR, Double.toString(qualityValue)));
    }

    /**
     * Copy-constructor that copies the type, subtype and parameters of the given
     * {@code MediaType}, and allows to set the specified character set.
     * @param other the other media type
     * @param charset the character set
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(@NonNull MediaType other, @NonNull Charset charset) {
        this(other.getType(), other.getSubtype(), addCharsetParameter(charset, other.getParameters()));
        this.resolvedCharset = charset;
    }

    /**
     * Copy-constructor that copies the type and subtype of the given {@code MediaType},
     * and allows for different parameter.
     * @param other the other media type
     * @param parameters the parameters, may be {@code null}
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(@NonNull MediaType other, @Nullable Map<String, String> parameters) {
        this(other.getType(), other.getSubtype(), parameters);
    }

    /**
     * Create a new {@code MediaType} for the given type, subtype, and parameters.
     * @param type the primary type
     * @param subtype the subtype
     * @param parameters the parameters, may be {@code null}
     * @throws IllegalArgumentException if any of the parameters contain illegal characters
     */
    public MediaType(String type, String subtype, @Nullable Map<String, String> parameters) {
        Assert.hasLength(type, "'type' must not be empty");
        Assert.hasLength(subtype, "'subtype' must not be empty");
        checkToken(type);
        checkToken(subtype);
        this.type = type.toLowerCase(Locale.ENGLISH);
        this.subtype = subtype.toLowerCase(Locale.ENGLISH);
        if (parameters != null && !parameters.isEmpty()) {
            Map<String, String> map = new LinkedCaseInsensitiveMap<>(parameters.size(), Locale.ENGLISH);
            parameters.forEach((parameter, value) -> {
                checkParameters(parameter, value);
                map.put(parameter, value);
            });
            this.parameters = Collections.unmodifiableMap(map);
        } else {
            this.parameters = Collections.emptyMap();
        }
    }

    /**
     * Checks the given token string for illegal characters, as defined in RFC 2616,
     * section 2.2.
     * @throws IllegalArgumentException in case of illegal characters
     * @see <a href="https://tools.ietf.org/html/rfc2616#section-2.2">HTTP 1.1, section 2.2</a>
     */
    private void checkToken(@NonNull String token) {
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (!TOKEN.get(ch)) {
                throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + token + "\"");
            }
        }
    }

    private void checkParameters(String parameter, String value) {
        Assert.hasLength(parameter, "'parameter' must not be empty");
        Assert.hasLength(value, "'value' must not be empty");
        checkToken(parameter);
        if (PARAM_CHARSET.equals(parameter)) {
            if (this.resolvedCharset == null) {
                this.resolvedCharset = Charset.forName(unquote(value));
            }
        } else if (PARAM_QUALITY_FACTOR.equals(parameter)) {
            value = unquote(value);
            double d = Double.parseDouble(value);
            Assert.isTrue(d >= 0D && d <= 1D,
                    "Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
        } else if (!isQuotedString(value)) {
            checkToken(value);
        }
    }

    private boolean isQuotedString(@NonNull String s) {
        if (s.length() < 2) {
            return false;
        } else {
            return ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")));
        }
    }

    private String unquote(String s) {
        return (isQuotedString(s) ? s.substring(1, s.length() - 1) : s);
    }

    /**
     * Indicates whether the {@linkplain #getType() type} is the wildcard character
     * <code>&#42;</code> or not.
     * @return true if it is a wildcard character; Otherwise false
     */
    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(getType());
    }

    /**
     * Indicates whether the {@linkplain #getSubtype() subtype} is the wildcard
     * character <code>&#42;</code> or the wildcard character followed by a suffix
     * (e.g. <code>&#42;+xml</code>).
     * @return whether the subtype is a wildcard
     */
    public boolean isWildcardSubtype() {
        return (WILDCARD_TYPE.equals(getSubtype()) || getSubtype().startsWith("*+"));
    }

    /**
     * Indicates whether this MIME Type is concrete, i.e. whether neither the type
     * nor the subtype is a wildcard character <code>&#42;</code>.
     * @return whether this MIME Type is concrete
     */
    public boolean isConcrete() {
        return (!isWildcardType() && !isWildcardSubtype());
    }

    /**
     * Return the primary type.
     * @return the primary type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Return the subtype.
     * @return the subtype
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * Return the character set, as indicated by a {@code charset} parameter, if any.
     * @return the character set, or {@code null} if not available
     */
    @Nullable
    public Charset getCharset() {
        return this.resolvedCharset;
    }

    /**
     * Return a generic parameter value, given a parameter name.
     * @param name the parameter name
     * @return the parameter value, or {@code null} if not present
     */
    @Nullable
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * Return all generic parameter values.
     * @return a read-only map (possibly empty, never {@code null})
     */
    public Map<String, String> getParameters() {
        return this.parameters;
    }

    /**
     * Indicate whether this {@code MediaType} includes the given media type.
     * <p>For instance, {@code text/*} includes {@code text/plain} and {@code text/html},
     * and {@code application/*+xml} includes {@code application/soap+xml}, etc.
     * This method is <b>not</b> symmetric.</p>
     * <p>Simply calls {@link #includes(MediaType)} but declared with a
     * {@code MediaType} parameter for binary backwards compatibility.</p>
     * @param other the reference media type with which to compare
     * @return {@code true} if this media type includes the given media type;
     * {@code false} otherwise
     */
    public boolean includes(@Nullable MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType()) {
            // */* includes anything
            return true;
        }
        if (getType().equals(other.getType())) {
            if (getSubtype().equals(other.getSubtype())) {
                return true;
            }
            if (isWildcardSubtype()) {
                // Wildcard with suffix, e.g. application/*+xml
                int thisPlusIdx = getSubtype().lastIndexOf('+');
                if (thisPlusIdx == -1) {
                    return true;
                }
                // application/*+xml includes application/soap+xml
                int otherPlusIdx = other.getSubtype().lastIndexOf('+');
                if (otherPlusIdx != -1) {
                    String thisSubtypeNoSuffix = getSubtype().substring(0, thisPlusIdx);
                    String thisSubtypeSuffix = getSubtype().substring(thisPlusIdx + 1);
                    String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);
                    if (thisSubtypeSuffix.equals(otherSubtypeSuffix) && WILDCARD_TYPE.equals(thisSubtypeNoSuffix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Indicate whether this {@code MediaType} is compatible with the given media type.
     * <p>For instance, {@code text/*} is compatible with {@code text/plain},
     * {@code text/html}, and vice versa. In effect, this method is similar to
     * {@link #includes}, except that it <b>is</b> symmetric.</p>
     * <p>Simply calls {@link #isCompatibleWith(MediaType)} but declared with a
     * {@code MediaType} parameter for binary backwards compatibility.</p>
     * @param other the reference media type with which to compare
     * @return {@code true} if this media type is compatible with the given media type;
     * {@code false} otherwise
     */
    public boolean isCompatibleWith(@Nullable MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType() || other.isWildcardType()) {
            return true;
        }
        if (getType().equals(other.getType())) {
            if (getSubtype().equals(other.getSubtype())) {
                return true;
            }
            // Wildcard with suffix? e.g. application/*+xml
            if (isWildcardSubtype() || other.isWildcardSubtype()) {
                int thisPlusIdx = getSubtype().lastIndexOf('+');
                int otherPlusIdx = other.getSubtype().lastIndexOf('+');
                if (thisPlusIdx == -1 && otherPlusIdx == -1) {
                    return true;
                }
                if (thisPlusIdx != -1 && otherPlusIdx != -1) {
                    String thisSubtypeNoSuffix = getSubtype().substring(0, thisPlusIdx);
                    String otherSubtypeNoSuffix = other.getSubtype().substring(0, otherPlusIdx);
                    String thisSubtypeSuffix = getSubtype().substring(thisPlusIdx + 1);
                    String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);
                    if (thisSubtypeSuffix.equals(otherSubtypeSuffix) &&
                        (WILDCARD_TYPE.equals(thisSubtypeNoSuffix) || WILDCARD_TYPE.equals(otherSubtypeNoSuffix))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Similar to {@link #equals(Object)} but based on the type and subtype
     * only, i.e. ignoring parameters.
     * @param other the other mime type to compare to
     * @return whether the two mime types have the same type and subtype
     */
    public boolean equalsTypeAndSubtype(@Nullable MediaType other) {
        if (other == null) {
            return false;
        }
        return this.type.equalsIgnoreCase(other.type) &&
            this.subtype.equalsIgnoreCase(other.subtype);
    }

    /**
     * Unlike {@link Collection#contains(Object)} which relies on
     * {@link MediaType#equals(Object)}, this method only checks the type and the
     * subtype, but otherwise ignores parameters.
     * @param mediaTypes the list of mime types to perform the check against
     * @return whether the list contains the given mime type
     */
    public boolean isPresentIn(@NonNull Collection<MediaType> mediaTypes) {
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.equalsTypeAndSubtype(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaType otherType)) {
            return false;
        }
        return (this.type.equalsIgnoreCase(otherType.type) &&
            this.subtype.equalsIgnoreCase(otherType.subtype) &&
            parametersAreEqual(otherType));
    }

    /**
     * Determine if the parameters in this {@code MediaType} and the supplied
     * {@code MediaType} are equal, performing case-insensitive comparisons
     * for {@link Charset Charsets}.
     */
    private boolean parametersAreEqual(@NonNull MediaType other) {
        if (this.parameters.size() != other.parameters.size()) {
            return false;
        }
        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            String key = entry.getKey();
            if (!other.parameters.containsKey(key)) {
                return false;
            }
            if (PARAM_CHARSET.equals(key)) {
                if (!ObjectUtils.nullSafeEquals(getCharset(), other.getCharset())) {
                    return false;
                }
            } else if (!ObjectUtils.nullSafeEquals(entry.getValue(), other.parameters.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.subtype.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String value = this.toStringValue;
        if (value == null) {
            StringBuilder builder = new StringBuilder();
            appendTo(builder);
            value = builder.toString();
            this.toStringValue = value;
        }
        return value;
    }

    protected void appendTo(@NonNull StringBuilder builder) {
        builder.append(this.type);
        builder.append('/');
        builder.append(this.subtype);
        appendTo(this.parameters, builder);
    }

    private void appendTo(@NonNull Map<String, String> map, StringBuilder builder) {
        map.forEach((key, val) -> {
            builder.append(';');
            builder.append(key);
            builder.append('=');
            builder.append(val);
        });
    }

    /**
     * Compares this Media Type to another alphabetically.
     * @param other the Media Type to compare to
     */
    @Override
    public int compareTo(@NonNull MediaType other) {
        int comp = getType().compareToIgnoreCase(other.getType());
        if (comp != 0) {
            return comp;
        }
        comp = getSubtype().compareToIgnoreCase(other.getSubtype());
        if (comp != 0) {
            return comp;
        }
        comp = getParameters().size() - other.getParameters().size();
        if (comp != 0) {
            return comp;
        }

        TreeSet<String> thisAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        thisAttributes.addAll(getParameters().keySet());
        TreeSet<String> otherAttributes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        otherAttributes.addAll(other.getParameters().keySet());
        Iterator<String> thisAttributesIterator = thisAttributes.iterator();
        Iterator<String> otherAttributesIterator = otherAttributes.iterator();

        while (thisAttributesIterator.hasNext()) {
            String thisAttribute = thisAttributesIterator.next();
            String otherAttribute = otherAttributesIterator.next();
            comp = thisAttribute.compareToIgnoreCase(otherAttribute);
            if (comp != 0) {
                return comp;
            }
            if (PARAM_CHARSET.equals(thisAttribute)) {
                Charset thisCharset = getCharset();
                Charset otherCharset = other.getCharset();
                if (thisCharset != otherCharset) {
                    if (thisCharset == null) {
                        return -1;
                    }
                    if (otherCharset == null) {
                        return 1;
                    }
                    comp = thisCharset.compareTo(otherCharset);
                    if (comp != 0) {
                        return comp;
                    }
                }
            } else {
                String thisValue = getParameters().get(thisAttribute);
                String otherValue = other.getParameters().get(otherAttribute);
                if (otherValue == null) {
                    otherValue = "";
                }
                comp = thisValue.compareTo(otherValue);
                if (comp != 0) {
                    return comp;
                }
            }
        }
        return 0;
    }

    /**
     * Return the quality factor, as indicated by a {@code q} parameter, if any.
     * Defaults to {@code 1.0}.
     * @return the quality factor as double value
     */
    public double getQualityValue() {
        String qualityFactor = getParameter(PARAM_QUALITY_FACTOR);
        return (qualityFactor != null ? Double.parseDouble(unquote(qualityFactor)) : 1D);
    }

    /**
     * Return a replica of this instance with the quality value of the given {@code MediaType}.
     * @param mediaType the media type
     * @return the same instance if the given MediaType doesn't have a quality value,
     * or a new one otherwise
     */
    public MediaType copyQualityValue(@NonNull MediaType mediaType) {
        if (!mediaType.getParameters().containsKey(PARAM_QUALITY_FACTOR)) {
            return this;
        }
        Map<String, String> params = new LinkedHashMap<>(getParameters());
        params.put(PARAM_QUALITY_FACTOR, mediaType.getParameters().get(PARAM_QUALITY_FACTOR));
        return new MediaType(this, params);
    }

    /**
     * Return a replica of this instance with its quality value removed.
     * @return the same instance if the media type doesn't contain a quality value,
     * or a new one otherwise
     */
    public MediaType removeQualityValue() {
        if (!getParameters().containsKey(PARAM_QUALITY_FACTOR)) {
            return this;
        }
        Map<String, String> params = new LinkedHashMap<>(getParameters());
        params.remove(PARAM_QUALITY_FACTOR);
        return new MediaType(this, params);
    }

    /**
     * Parse the given String into a single {@code MediaType}.
     * @param mediaType the string to parse
     * @return the media type
     * @throws InvalidMediaTypeException if the media type value cannot be parsed
     */
    @NonNull
    public static MediaType parseMediaType(String mediaType) {
        MediaType type = MediaTypeUtils.parseMediaType(mediaType);
        try {
            return new MediaType(type.getType(), type.getSubtype(), type.getParameters());
        } catch (IllegalArgumentException ex) {
            throw new InvalidMediaTypeException(mediaType, ex.getMessage());
        }
    }

    /**
     * Parse the comma-separated string into a list of {@code MediaType} objects.
     * <p>This method can be used to parse an Accept or Content-Type header.</p>
     * @param mediaTypes the string to parse
     * @return the list of media types
     * @throws InvalidMediaTypeException if the media type value cannot be parsed
     */
    @NonNull
    public static List<MediaType> parseMediaTypes(@Nullable String mediaTypes) {
        if (!StringUtils.hasLength(mediaTypes)) {
            return Collections.emptyList();
        }
        // Avoid using java.util.stream.Stream in hot paths
        List<String> tokenizedTypes = MediaTypeUtils.tokenize(mediaTypes);
        List<MediaType> result = new ArrayList<>(tokenizedTypes.size());
        for (String type : tokenizedTypes) {
            if (StringUtils.hasText(type)) {
                result.add(parseMediaType(type));
            }
        }
        return result;
    }

    /**
     * Parse the given list of (potentially) comma-separated strings into a
     * list of {@code MediaType} objects.
     * <p>This method can be used to parse an Accept or Content-Type header.</p>
     * @param mediaTypes the string to parse
     * @return the list of media types
     * @throws InvalidMediaTypeException if the media type value cannot be parsed
     */
    public static List<MediaType> parseMediaTypes(@Nullable List<String> mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty()) {
            return Collections.emptyList();
        } else if (mediaTypes.size() == 1) {
            return parseMediaTypes(mediaTypes.get(0));
        } else {
            List<MediaType> result = new ArrayList<>(8);
            for (String mediaType : mediaTypes) {
                result.addAll(parseMediaTypes(mediaType));
            }
            return result;
        }
    }

    /**
     * Return a string representation of the given list of {@code MediaType} objects.
     * <p>This method can be used to for an {@code Accept} or {@code Content-Type} header.</p>
     * @param mediaTypes the media types to create a string representation for
     * @return the string representation
     */
    @NonNull
    public static String toString(Collection<MediaType> mediaTypes) {
        return MediaTypeUtils.toString(mediaTypes);
    }

    /**
     * Sorts the given list of {@code MediaType} objects by specificity.
     * <p>Given two media types:</p>
     * <ol>
     * <li>if either media type has a {@linkplain #isWildcardType() wildcard type}, then the media type without the
     * wildcard is ordered before the other.</li>
     * <li>if the two media types have different {@linkplain #getType() types}, then they are considered equal and
     * remain their current order.</li>
     * <li>if either media type has a {@linkplain #isWildcardSubtype() wildcard subtype}, then the media type without
     * the wildcard is sorted before the other.</li>
     * <li>if the two media types have different {@linkplain #getSubtype() subtypes}, then they are considered equal
     * and remain their current order.</li>
     * <li>if the two media types have different {@linkplain #getQualityValue() quality value}, then the media type
     * with the highest quality value is ordered before the other.</li>
     * <li>if the two media types have a different amount of {@linkplain #getParameter(String) parameters}, then the
     * media type with the most parameters is ordered before the other.</li>
     * </ol>
     * <p>For example:</p>
     * <blockquote>audio/basic &lt; audio/* &lt; *&#047;*</blockquote>
     * <blockquote>audio/* &lt; audio/*;q=0.7; audio/*;q=0.3</blockquote>
     * <blockquote>audio/basic;level=1 &lt; audio/basic</blockquote>
     * <blockquote>audio/basic == text/html</blockquote>
     * <blockquote>audio/basic == audio/wave</blockquote>
     * @param mediaTypes the list of media types to be sorted
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">HTTP 1.1: Semantics
     * and Content, section 5.3.2</a>
     */
    public static void sortBySpecificity(List<MediaType> mediaTypes) {
        Assert.notNull(mediaTypes, "'mediaTypes' must not be null");
        if (mediaTypes.size() > 1) {
            mediaTypes.sort(SPECIFICITY_COMPARATOR);
        }
    }

    /**
     * Sorts the given list of {@code MediaType} objects by quality value.
     * <p>Given two media types:</p>
     * <ol>
     * <li>if the two media types have different {@linkplain #getQualityValue() quality value}, then the media type
     * with the highest quality value is ordered before the other.</li>
     * <li>if either media type has a {@linkplain #isWildcardType() wildcard type}, then the media type without the
     * wildcard is ordered before the other.</li>
     * <li>if the two media types have different {@linkplain #getType() types}, then they are considered equal and
     * remain their current order.</li>
     * <li>if either media type has a {@linkplain #isWildcardSubtype() wildcard subtype}, then the media type without
     * the wildcard is sorted before the other.</li>
     * <li>if the two media types have different {@linkplain #getSubtype() subtypes}, then they are considered equal
     * and remain their current order.</li>
     * <li>if the two media types have a different amount of {@linkplain #getParameter(String) parameters}, then the
     * media type with the most parameters is ordered before the other.</li>
     * </ol>
     * @param mediaTypes the list of media types to be sorted
     * @see #getQualityValue()
     */
    public static void sortByQualityValue(List<MediaType> mediaTypes) {
        Assert.notNull(mediaTypes, "'mediaTypes' must not be null");
        if (mediaTypes.size() > 1) {
            mediaTypes.sort(QUALITY_VALUE_COMPARATOR);
        }
    }

    /**
     * Sorts the given list of {@code MediaType} objects by specificity as the
     * primary criteria and quality value the secondary.
     * @param mediaTypes the list of media types to sort
     * @see MediaType#sortBySpecificity(List)
     * @see MediaType#sortByQualityValue(List)
     */
    public static void sortBySpecificityAndQuality(List<MediaType> mediaTypes) {
        Assert.notNull(mediaTypes, "'mediaTypes' must not be null");
        if (mediaTypes.size() > 1) {
            mediaTypes.sort(SPECIFICITY_COMPARATOR.thenComparing(QUALITY_VALUE_COMPARATOR));
        }
    }

    /**
     * Parse the given String value into a {@code MediaType} object,
     * with this method name following the 'valueOf' naming convention.
     * @param value the string to parse
     * @return the media type
     * @throws InvalidMediaTypeException if the media type value cannot be parsed
     * @see #parseMediaType(String)
     */
    @NonNull
    public static MediaType valueOf(String value) {
        return parseMediaType(value);
    }

    @NonNull
    private static Map<String, String> addCharsetParameter(@NonNull Charset charset, Map<String, String> parameters) {
        Map<String, String> map = new LinkedHashMap<>(parameters);
        map.put(PARAM_CHARSET, charset.name());
        return map;
    }

    /**
     * Comparator used by {@link #sortByQualityValue(List)}.
     */
    public static final Comparator<MediaType> QUALITY_VALUE_COMPARATOR = (mediaType1, mediaType2) -> {
        double quality1 = mediaType1.getQualityValue();
        double quality2 = mediaType2.getQualityValue();
        int qualityComparison = Double.compare(quality2, quality1);
        if (qualityComparison != 0) {
            return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
        } else if (mediaType1.isWildcardType() && !mediaType2.isWildcardType()) {  // */* < audio/*
            return 1;
        } else if (mediaType2.isWildcardType() && !mediaType1.isWildcardType()) {  // audio/* > */*
            return -1;
        } else if (!mediaType1.getType().equals(mediaType2.getType())) {  // audio/basic == text/html
            return 0;
        } else {  // mediaType1.getType().equals(mediaType2.getType())
            if (mediaType1.isWildcardSubtype() && !mediaType2.isWildcardSubtype()) {  // audio/* < audio/basic
                return 1;
            } else if (mediaType2.isWildcardSubtype() && !mediaType1.isWildcardSubtype()) {  // audio/basic > audio/*
                return -1;
            } else if (!mediaType1.getSubtype().equals(mediaType2.getSubtype())) {  // audio/basic == audio/wave
                return 0;
            } else {
                int paramsSize1 = mediaType1.getParameters().size();
                int paramsSize2 = mediaType2.getParameters().size();
                return Integer.compare(paramsSize2, paramsSize1);  // audio/basic;level=1 < audio/basic
            }
        }
    };


    /**
     * Comparator used by {@link #sortBySpecificity(List)}.
     */
    public static final Comparator<MediaType> SPECIFICITY_COMPARATOR = new SpecificityComparator<>() {
        @Override
        protected int compareParameters(@NonNull MediaType mediaType1, @NonNull MediaType mediaType2) {
            double quality1 = mediaType1.getQualityValue();
            double quality2 = mediaType2.getQualityValue();
            int qualityComparison = Double.compare(quality2, quality1);
            if (qualityComparison != 0) {
                return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
            }
            return super.compareParameters(mediaType1, mediaType2);
        }
    };


    /**
     * Comparator to sort {@link MediaType MediaTypes} in order of specificity.
     * @param <T> the type of mime types that may be compared by this comparator
     */
    public static class SpecificityComparator<T extends MediaType> implements Comparator<T> {
        @Override
        public int compare(@NonNull T MediaType1, T MediaType2) {
            if (MediaType1.isWildcardType() && !MediaType2.isWildcardType()) {  // */* < audio/*
                return 1;
            } else if (MediaType2.isWildcardType() && !MediaType1.isWildcardType()) {  // audio/* > */*
                return -1;
            } else if (!MediaType1.getType().equals(MediaType2.getType())) {  // audio/basic == text/html
                return 0;
            } else {  // mediaType1.getType().equals(mediaType2.getType())
                if (MediaType1.isWildcardSubtype() && !MediaType2.isWildcardSubtype()) {  // audio/* < audio/basic
                    return 1;
                } else if (MediaType2.isWildcardSubtype() && !MediaType1.isWildcardSubtype()) {  // audio/basic > audio/*
                    return -1;
                } else if (!MediaType1.getSubtype().equals(MediaType2.getSubtype())) {  // audio/basic == audio/wave
                    return 0;
                } else {  // mediaType2.getSubtype().equals(mediaType2.getSubtype())
                    return compareParameters(MediaType1, MediaType2);
                }
            }
        }

        protected int compareParameters(@NonNull T mediaType1, @NonNull T mediaType2) {
            int paramsSize1 = mediaType1.getParameters().size();
            int paramsSize2 = mediaType2.getParameters().size();
            return Integer.compare(paramsSize2, paramsSize1);  // audio/basic;level=1 < audio/basic
        }
    }

}
