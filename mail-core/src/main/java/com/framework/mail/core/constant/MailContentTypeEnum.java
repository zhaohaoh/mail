package com.framework.mail.core.constant;

import org.apache.commons.lang3.StringUtils;

public enum MailContentTypeEnum   {
    PLAIN(0, "TEXT/HTML"),
    HTML(1, "TEXT/PALIN"),
    //文本和超文本共存
    ALTERNATIVE(2, "MULTIPART/ALTERNATIVE"),
    //
    MIXED(3,"MULTIPART/MIXED"),
    OTHER(4,"其他");

    private Integer code;
    private String name;

      MailContentTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }


    public Integer key() {
        return code;
    }


    public String value() {
        return name;
    }
    public static MailContentTypeEnum convert(String name){
        for (MailContentTypeEnum value : MailContentTypeEnum.values()) {
            boolean b = StringUtils.containsIgnoreCase(name, value.name);
            if (b){
                return value;
            }
        }
        return null;
    }
}
