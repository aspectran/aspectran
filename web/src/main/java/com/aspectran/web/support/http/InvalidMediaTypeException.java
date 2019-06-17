package com.aspectran.web.support.http;

/**
 * Exception thrown from {@link MediaTypeUtils#parseMediaType(String)} in case of
 * encountering an invalid content type specification String.
 *
 * <p>Created: 2019-06-18</p>
 */
@SuppressWarnings("serial")
public class InvalidMediaTypeException extends IllegalArgumentException {

    private final String mimeType;

    /**
     * Create a new InvalidContentTypeException for the given content type.
     * @param mimeType the offending media type
     * @param message a detail message indicating the invalid part
     */
    public InvalidMediaTypeException(String mimeType, String message) {
        super("Invalid mime type \"" + mimeType + "\": " + message);
        this.mimeType = mimeType;
    }


    /**
     * Return the offending content type.
     */
    public String getMimeType() {
        return this.mimeType;
    }

}
