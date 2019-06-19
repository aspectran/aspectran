package com.aspectran.web.support.http;

import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.StringUtils;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Miscellaneous {@link MediaType} utility methods.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Dimitrios Liapis
 * @author Brian Clozel
 * @since 4.0
 */
public abstract class MediaTypeUtils {

    private static final byte[] BOUNDARY_CHARS =
        new byte[]{'-', '_', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
            'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z'};

    /**
     * Comparator used by {@link #sortBySpecificity(List)}.
     */
    public static final Comparator<MediaType> SPECIFICITY_COMPARATOR = new MediaType.SpecificityComparator<>();

    /**
     * Public constant media type that includes all media ranges (i.e. "&#42;/&#42;").
     */
    public static final MediaType ALL;

    /**
     * A String equivalent of {@link MediaTypeUtils#ALL}.
     */
    public static final String ALL_VALUE = "*/*";

    /**
     * Public constant media type for {@code application/json}.
     */
    public static final MediaType APPLICATION_JSON;

    /**
     * A String equivalent of {@link MediaTypeUtils#APPLICATION_JSON}.
     */
    public static final String APPLICATION_JSON_VALUE = "application/json";

    /**
     * Public constant media type for {@code application/octet-stream}.
     */
    public static final MediaType APPLICATION_OCTET_STREAM;

    /**
     * A String equivalent of {@link MediaTypeUtils#APPLICATION_OCTET_STREAM}.
     */
    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

    /**
     * Public constant media type for {@code application/xml}.
     */
    public static final MediaType APPLICATION_XML;

    /**
     * A String equivalent of {@link MediaTypeUtils#APPLICATION_XML}.
     */
    public static final String APPLICATION_XML_VALUE = "application/xml";

    /**
     * Public constant media type for {@code image/gif}.
     */
    public static final MediaType IMAGE_GIF;

    /**
     * A String equivalent of {@link MediaTypeUtils#IMAGE_GIF}.
     */
    public static final String IMAGE_GIF_VALUE = "image/gif";

    /**
     * Public constant media type for {@code image/jpeg}.
     */
    public static final MediaType IMAGE_JPEG;

    /**
     * A String equivalent of {@link MediaTypeUtils#IMAGE_JPEG}.
     */
    public static final String IMAGE_JPEG_VALUE = "image/jpeg";

    /**
     * Public constant media type for {@code image/png}.
     */
    public static final MediaType IMAGE_PNG;

    /**
     * A String equivalent of {@link MediaTypeUtils#IMAGE_PNG}.
     */
    public static final String IMAGE_PNG_VALUE = "image/png";

    /**
     * Public constant media type for {@code text/html}.
     */
    public static final MediaType TEXT_HTML;

    /**
     * A String equivalent of {@link MediaTypeUtils#TEXT_HTML}.
     */
    public static final String TEXT_HTML_VALUE = "text/html";

    /**
     * Public constant media type for {@code text/plain}.
     */
    public static final MediaType TEXT_PLAIN;

    /**
     * A String equivalent of {@link MediaTypeUtils#TEXT_PLAIN}.
     */
    public static final String TEXT_PLAIN_VALUE = "text/plain";

    /**
     * Public constant media type for {@code text/xml}.
     */
    public static final MediaType TEXT_XML;

    /**
     * A String equivalent of {@link MediaTypeUtils#TEXT_XML}.
     */
    public static final String TEXT_XML_VALUE = "text/xml";


    private static final ConcurrentLruCache<String, MediaType> cachedMediaTypes =
        new ConcurrentLruCache<>(64, MediaTypeUtils::parseMediaTypeInternal);

    @Nullable
    private static volatile Random random;

    static {
        // Not using "parseMediaType" to avoid static init cost
        ALL = new MediaType("*", "*");
        APPLICATION_JSON = new MediaType("application", "json");
        APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
        APPLICATION_XML = new MediaType("application", "xml");
        IMAGE_GIF = new MediaType("image", "gif");
        IMAGE_JPEG = new MediaType("image", "jpeg");
        IMAGE_PNG = new MediaType("image", "png");
        TEXT_HTML = new MediaType("text", "html");
        TEXT_PLAIN = new MediaType("text", "plain");
        TEXT_XML = new MediaType("text", "xml");
    }

    /**
     * Parse the given String into a single {@code MediaType}.
     * Recently parsed {@code MediaType} are cached for further retrieval.
     *
     * @param mediaType the string to parse
     * @return the media type
     * @throws InvalidMediaTypeException if the string cannot be parsed
     */
    public static MediaType parseMediaType(String mediaType) {
        return cachedMediaTypes.get(mediaType);
    }

    private static MediaType parseMediaTypeInternal(String mediaType) {
        if (!StringUtils.hasLength(mediaType)) {
            throw new InvalidMediaTypeException(mediaType, "'mediaType' must not be empty");
        }

        int index = mediaType.indexOf(';');
        String fullType = (index >= 0 ? mediaType.substring(0, index) : mediaType).trim();
        if (fullType.isEmpty()) {
            throw new InvalidMediaTypeException(mediaType, "'mediaType' must not be empty");
        }

        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (MediaType.WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new InvalidMediaTypeException(mediaType, "does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new InvalidMediaTypeException(mediaType, "does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1);
        if (MediaType.WILDCARD_TYPE.equals(type) && !MediaType.WILDCARD_TYPE.equals(subtype)) {
            throw new InvalidMediaTypeException(mediaType, "wildcard type is legal only in '*/*' (all media types)");
        }

        Map<String, String> parameters = null;
        do {
            int nextIndex = index + 1;
            boolean quoted = false;
            while (nextIndex < mediaType.length()) {
                char ch = mediaType.charAt(nextIndex);
                if (ch == ';') {
                    if (!quoted) {
                        break;
                    }
                } else if (ch == '"') {
                    quoted = !quoted;
                }
                nextIndex++;
            }
            String parameter = mediaType.substring(index + 1, nextIndex).trim();
            if (parameter.length() > 0) {
                if (parameters == null) {
                    parameters = new LinkedHashMap<>(4);
                }
                int eqIndex = parameter.indexOf('=');
                if (eqIndex >= 0) {
                    String attribute = parameter.substring(0, eqIndex).trim();
                    String value = parameter.substring(eqIndex + 1).trim();
                    parameters.put(attribute, value);
                }
            }
            index = nextIndex;
        }
        while (index < mediaType.length());

        try {
            return new MediaType(type, subtype, parameters);
        } catch (UnsupportedCharsetException ex) {
            throw new InvalidMediaTypeException(mediaType, "unsupported charset '" + ex.getCharsetName() + "'");
        } catch (IllegalArgumentException ex) {
            throw new InvalidMediaTypeException(mediaType, ex.getMessage());
        }
    }

    /**
     * Parse the comma-separated string into a list of {@code MediaType} objects.
     *
     * @param mediaTypes the string to parse
     * @return the list of media types
     * @throws InvalidMediaTypeException if the string cannot be parsed
     */
    public static List<MediaType> parseMediaTypes(String mediaTypes) {
        if (!StringUtils.hasLength(mediaTypes)) {
            return Collections.emptyList();
        }
        return tokenize(mediaTypes).stream()
            .map(MediaTypeUtils::parseMediaType).collect(Collectors.toList());
    }

    /**
     * Tokenize the given comma-separated string of {@code MediaType} objects
     * into a {@code List<String>}. Unlike simple tokenization by ",", this
     * method takes into account quoted parameters.
     *
     * @param mediaTypes the string to tokenize
     * @return the list of tokens
     * @since 5.1.3
     */
    public static List<String> tokenize(String mediaTypes) {
        if (!StringUtils.hasLength(mediaTypes)) {
            return Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        int startIndex = 0;
        int i = 0;
        while (i < mediaTypes.length()) {
            switch (mediaTypes.charAt(i)) {
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case ',':
                    if (!inQuotes) {
                        tokens.add(mediaTypes.substring(startIndex, i));
                        startIndex = i + 1;
                    }
                    break;
                case '\\':
                    i++;
                    break;
            }
            i++;
        }
        tokens.add(mediaTypes.substring(startIndex));
        return tokens;
    }

    /**
     * Return a string representation of the given list of {@code MediaType} objects.
     *
     * @param mediaTypes the string to parse
     * @return the list of media types
     * @throws IllegalArgumentException if the String cannot be parsed
     */
    public static String toString(Collection<MediaType> mediaTypes) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<? extends MediaType> iterator = mediaTypes.iterator(); iterator.hasNext(); ) {
            MediaType MediaType = iterator.next();
            MediaType.appendTo(builder);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Sorts the given list of {@code MediaType} objects by specificity.
     * <p>Given two media types:</p>
     * <ol>
     * <li>if either media type has a {@linkplain MediaType#isWildcardType() wildcard type},
     * then the media type without the wildcard is ordered before the other.</li>
     * <li>if the two media types have different {@linkplain MediaType#getType() types},
     * then they are considered equal and remain their current order.</li>
     * <li>if either media type has a {@linkplain MediaType#isWildcardSubtype() wildcard subtype}
     * , then the media type without the wildcard is sorted before the other.</li>
     * <li>if the two media types have different {@linkplain MediaType#getSubtype() subtypes},
     * then they are considered equal and remain their current order.</li>
     * <li>if the two media types have a different amount of
     * {@linkplain MediaType#getParameter(String) parameters}, then the media type with the most
     * parameters is ordered before the other.</li>
     * </ol>
     * <p>For example:</p>
     * <blockquote>audio/basic &lt; audio/* &lt; *&#047;*</blockquote>
     * <blockquote>audio/basic;level=1 &lt; audio/basic</blockquote>
     * <blockquote>audio/basic == text/html</blockquote>
     * <blockquote>audio/basic == audio/wave</blockquote>
     *
     * @param mediaTypes the list of media types to be sorted
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">HTTP 1.1: Semantics
     * and Content, section 5.3.2</a>
     */
    public static void sortBySpecificity(List<MediaType> mediaTypes) {
        Assert.notNull(mediaTypes, "'mimeTypes' must not be null");
        if (mediaTypes.size() > 1) {
            mediaTypes.sort(SPECIFICITY_COMPARATOR);
        }
    }


    /**
     * Simple Least Recently Used cache, bounded by the maximum size given
     * to the class constructor.
     * <p>This implementation is backed by a {@code ConcurrentHashMap} for storing
     * the cached values and a {@code ConcurrentLinkedQueue} for ordering the keys
     * and choosing the least recently used key when the cache is at full capacity.
     *
     * @param <K> the type of the key used for caching
     * @param <V> the type of the cached values
     */
    private static class ConcurrentLruCache<K, V> {

        private final int maxSize;

        private final ConcurrentLinkedQueue<K> queue = new ConcurrentLinkedQueue<>();

        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        private final Function<K, V> generator;

        public ConcurrentLruCache(int maxSize, Function<K, V> generator) {
            Assert.isTrue(maxSize > 0, "LRU max size should be positive");
            Assert.notNull(generator, "Generator function should not be null");
            this.maxSize = maxSize;
            this.generator = generator;
        }

        public V get(K key) {
            this.lock.readLock().lock();
            try {
                if (this.queue.remove(key)) {
                    this.queue.add(key);
                    return this.cache.get(key);
                }
            } finally {
                this.lock.readLock().unlock();
            }
            this.lock.writeLock().lock();
            try {
                if (this.queue.size() == this.maxSize) {
                    K leastUsed = this.queue.poll();
                    if (leastUsed != null) {
                        this.cache.remove(leastUsed);
                    }
                }
                V value = this.generator.apply(key);
                this.queue.add(key);
                this.cache.put(key, value);
                return value;
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

}
