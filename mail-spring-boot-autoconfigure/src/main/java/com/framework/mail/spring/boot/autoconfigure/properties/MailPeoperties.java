package com.framework.mail.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.framework.mail.spring.boot.autoconfigure.properties.MailPeoperties.PREFIX;


@ConfigurationProperties(prefix =  PREFIX)
public class MailPeoperties {
    public static final String PREFIX = "framework.mail";
    private Boolean enable;
    //用户名
    private String username;

    //密码
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }


}

