package com.gj.web.crawler.pool.exc;

/**
 * custom exception
 */
public class ProxyForbiddenException extends RuntimeException{
    public ProxyForbiddenException(String message) {
        super(message);
    }

    public ProxyForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyForbiddenException(Throwable cause) {
        super(cause);
    }

    protected ProxyForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
