package com.aspectran.web.support.http;

/**
 * Exception thrown from {@link MediaTypeUtils#parseMediaType(String)} in case of
 * encountering an invalid content type specification String.
 *
 * <p>Created: 2019-06-18</p>
 */
@SuppressWarnings("serial")
public class InvalidMediaTypeException extends IllegalArgumentException {

    private final String mediaType;

    /**
     * Create a new InvalidContentTypeException for the given content type.
     *
     * @param mediaType the offending media type
     * @param message   a detail message indicating the invalid part
     */
    public InvalidMediaTypeException(String mediaType, String message) {
        super("Invalid media type \"" + mediaType + "\": " + message);
        this.mediaType = mediaType;
    }

    /**
     * Return the offending media type.
     *
     * @return the media type
     */
    public String getMediaType() {
        return this.mediaType;
    }

}
