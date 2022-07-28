package com.framework.mail.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = PullMailProperties.PREFIX)
public class PullMailProperties {
    public static final String PREFIX = "framework.mail.pull";


    //收件服务器地址
    private String host;


    //收件端口
    private Integer port;

    //收件加密
    private String encr;

    //收件协议
    private String protocol;


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