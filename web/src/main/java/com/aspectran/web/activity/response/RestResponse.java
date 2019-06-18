package com.aspectran.web.activity.response;

import com.aspectran.web.support.http.MediaType;

public interface RestResponse {

    Object getData();

    void setData(Object data);

    boolean isPrettyPrint();

    void setPrettyPrint(boolean prettyPrint);

    Object prettyPrint(boolean prettyPrint);

    boolean isFavorPathExtension();

    void setFavorPathExtension(boolean favorPathExtension);

    Object favorPathExtension(boolean favorPathExtension);

    boolean isIgnoreUnknownPathExtensions();

    void setIgnoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    Object ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    boolean isIgnoreAcceptHeader();

    void setIgnoreAcceptHeader(boolean ignoreAcceptHeader);

    Object ignoreAcceptHeader(boolean ignoreAcceptHeader);

    MediaType getDefaultContentType();

    void setDefaultContentType(MediaType defaultContentType);

    void setDefaultContentType(String defaultContentType);

    Object defaultContentType(MediaType defaultContentType);

    Object ok();

    Object created();

    Object created(String location);

    Object accepted();

    Object noContent();

    Object movedPermanently();

    Object seeOther();

    Object notModified();

    Object temporaryRedirect();

    Object badRequest();

    Object unauthorized();

    Object forbidden();

    Object notFound();

    Object methodNotAllowed();

    Object notAcceptable();

    Object conflict();

    Object preconditionFailed();

    Object unsupportedMediaType();

    Object internalServerError();

    int getStatus();

    void setStatus(int status);

    String getLocation();

}
