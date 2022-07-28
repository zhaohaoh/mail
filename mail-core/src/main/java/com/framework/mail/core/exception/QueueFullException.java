package com.framework.mail.core.exception;

public class QueueFullException extends SendMailException{
    public QueueFullException(String message) {
        super(message);
    }
}
