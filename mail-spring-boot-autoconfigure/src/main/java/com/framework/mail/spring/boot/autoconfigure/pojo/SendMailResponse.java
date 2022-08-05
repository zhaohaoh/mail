package com.framework.mail.spring.boot.autoconfigure.pojo;

import java.util.Objects;

public class SendMailResponse {
    //接收人
    private String requestId;
    //接收人
    private String to;

    private String error;

    private boolean success = false;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "SendMailResponse{" +
                "to='" + to + '\'' +
                ", error='" + error + '\'' +
                ", success=" + success +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendMailResponse that = (SendMailResponse) o;
        return success == that.success && Objects.equals(to, that.to) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, error, success);
    }
}
