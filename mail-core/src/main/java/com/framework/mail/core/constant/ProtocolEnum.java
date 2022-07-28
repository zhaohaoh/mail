package com.framework.mail.core.constant;

public enum ProtocolEnum {
    //协议类型
    IMAP("imap"),
    POP3("pop3"),
    SMTP("smtp"),

    TLS("tls"),
    SSL("ssl");



    private String code;

    ProtocolEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
