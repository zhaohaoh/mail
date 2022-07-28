package com.framework.mail.core.exception;

public class MailException extends RuntimeException{
    public MailException(String message) {
        super(message);
    }

    public MailException(Throwable cause) {
        super(cause);
    }
}
