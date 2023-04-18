package com.test.util;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ErrorException extends Exception {
    public HttpResponseStatus status = HttpResponseStatus.BAD_REQUEST;

    public ErrorException() {
        super();
    }

    public ErrorException(String reason) {
        super(reason);
    }

    public ErrorException(String reason, HttpResponseStatus status) {
        super(reason);

        this.status = status;
    }
}
