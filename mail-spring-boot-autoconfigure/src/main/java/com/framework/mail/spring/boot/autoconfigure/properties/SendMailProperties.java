package com.framework.mail.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = SendMailProperties.PREFIX)
public class SendMailProperties {

    public static final String PREFIX = "framework.mail.send";

    //发件服务器地址
    private String host;

    //发送端口
    private Integer port;

    //发件加密
    private String encr;

    //发件协议
    private String protocol="smtp";


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getEncr() {
        return encr;
    }

    public void setEncr(String encr) {
        this.encr = encr;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}