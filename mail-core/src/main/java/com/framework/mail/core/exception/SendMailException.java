package com.framework.mail.core.exception;

public class SendMailException extends MailException{
    public SendMailException(String message) {
        super(message);
    }

    public SendMailException(Throwable cause) {
        super(cause);
    }
}
