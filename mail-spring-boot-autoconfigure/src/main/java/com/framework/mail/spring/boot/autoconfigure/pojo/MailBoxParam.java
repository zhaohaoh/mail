package com.framework.mail.spring.boot.autoconfigure.pojo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;


/**
 * 邮件配置
 *
 * @author hzh
 * @date 2021-06-09
 */
public class MailBoxParam implements Serializable {

    //服务器地址
    @NotNull(message = "服务器地址不可为空")
    private String host;

    //用户名
    @NotNull(message = "用户名不可为空")
    private String username;

    //密码
    @NotNull(message = "密码不可为空")
    private String password;

    //端口
    @NotNull(message = "端口不可为空")
    private Integer port;

    //加密
    @NotNull(message = "加密类型不可为空")
    private String crypt;
    //协议
    @NotNull(message = "协议不可为空")
    private String protocol;

    public MailBoxParam() {
    }

    private MailBoxParam(Builder builder) {
        setHost(builder.host);
        setUsername(builder.username);
        setPassword(builder.password);
        setPort(builder.port);
        setCrypt(builder.crypt);
        setProtocol(builder.protocol);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "MailBoxParam{" +
                "host='" + host + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", port=" + port +
                ", crypt='" + crypt + '\'' +
                ", protocol='" + protocol + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailBoxParam that = (MailBoxParam) o;
        return Objects.equals(host, that.host) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(port, that.port) && Objects.equals(crypt, that.crypt) && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, username, password, port, crypt, protocol);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getCrypt() {
        return crypt;
    }

    public void setCrypt(String crypt) {
        this.crypt = crypt;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static final class Builder {
        private @NotNull(message = "服务器地址不可为空") String host;
        private @NotNull(message = "用户名不可为空") String username;
        private @NotNull(message = "密码不可为空") String password;
        private @NotNull(message = "端口不可为空") Integer port;
        private @NotNull(message = "加密类型不可为空") String crypt;
        private @NotNull(message = "协议不可为空") String protocol;

        private Builder() {
        }

        public Builder host(@NotNull(message = "服务器地址不可为空") String val) {
            host = val;
            return this;
        }

        public Builder username(@NotNull(message = "用户名不可为空") String val) {
            username = val;
            return this;
        }

        public Builder password(@NotNull(message = "密码不可为空") String val) {
            password = val;
            return this;
        }

        public Builder port(@NotNull(message = "端口不可为空") Integer val) {
            port = val;
            return this;
        }

        public Builder crypt(@NotNull(message = "加密类型不可为空") String val) {
            crypt = val;
            return this;
        }

        public Builder protocol(@NotNull(message = "协议不可为空") String val) {
            protocol = val;
            return this;
        }

        public MailBoxParam build() {
            return new MailBoxParam(this);
        }
    }
}