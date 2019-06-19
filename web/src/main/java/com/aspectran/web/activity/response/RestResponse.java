package com.aspectran.web.activity.response;

import com.aspectran.web.support.http.MediaType;

public interface RestResponse {

    Object getData();

    void setData(Object data);

    boolean isPrettyPrint();

    /**
     * Sets whether to apply indentations and line breaks
     * when generating response data.
     *
     * @param prettyPrint true if responding with indentations
     *                    and line breaks; otherwise false
     */
    void setPrettyPrint(boolean prettyPrint);

    RestResponse prettyPrint(boolean prettyPrint);

    boolean isFavorPathExtension();

    void setFavorPathExtension(boolean favorPathExtension);

    RestResponse favorPathExtension(boolean favorPathExtension);

    boolean isIgnoreUnknownPathExtensions();

    void setIgnoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    RestResponse ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    boolean isIgnoreAcceptHeader();

    void setIgnoreAcceptHeader(boolean ignoreAcceptHeader);

    RestResponse ignoreAcceptHeader(boolean ignoreAcceptHeader);

    MediaType getDefaultContentType();

    void setDefaultContentType(MediaType defaultContentType);

    void setDefaultContentType(String defaultContentType);

    RestResponse defaultContentType(MediaType defaultContentType);

    RestResponse ok();

    RestResponse created();

    RestResponse created(String location);

    RestResponse accepted();

    RestResponse noContent();

    RestResponse movedPermanently();

    RestResponse seeOther();

    RestResponse notModified();

    RestResponse temporaryRedirect();

    RestResponse badRequest();

    RestResponse unauthorized();

    RestResponse forbidden();

    RestResponse notFound();

    RestResponse methodNotAllowed();

    RestResponse notAcceptable();

    RestResponse conflict();

    RestResponse preconditionFailed();

    RestResponse unsupportedMediaType();

    RestResponse internalServerError();

    int getStatus();

    void setStatus(int status);

    String getLocation();

}
