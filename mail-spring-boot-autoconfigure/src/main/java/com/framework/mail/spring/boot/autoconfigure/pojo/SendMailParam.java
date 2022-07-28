package com.framework.mail.spring.boot.autoconfigure.pojo;


import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SendMailParam {
    //发送方邮箱
    private String from;
    @NotNull(message = "接收方邮箱集合不可为空")
    private String toUser;
    /**
     * 密送人
     */
    private String ccUser;
    /**
     * 抄送人
     */
    private String bccUser;
    @NotNull(message = "标题不可为空")
    //@ApiModelProperty(value = "标题")
    private String subject;
    //@ApiModelProperty(value = "内容")
    private String content;
    //@ApiModelProperty(value = "图片")
    private Resource[] imageFiles;
    //@ApiModelProperty(value = "附件")
    private Resource[] attachmentFiles;
    //@ApiModelProperty(value = "邮件配置信息")
    private MailBoxParam mailBoxParam;
    //@ApiModelProperty(value = "需要回执")
    private Boolean reply;
    //@ApiModelProperty(value = "内容是否为html")
    private Integer contentType = 1;
    /**
     * mine的标头
     */
    private final Map<String, String> heads = new ConcurrentHashMap<>();


    public Map<String, String> getHeads() {
        return heads;
    }

    public void addHead(String name, String value) {
        heads.put(name, value);
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Boolean getReply() {
        return reply;
    }

    public void setReply(Boolean reply) {
        this.reply = reply;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getCcUser() {
        return ccUser;
    }

    public void setCcUser(String ccUser) {
        this.ccUser = ccUser;
    }

    public String getBccUser() {
        return bccUser;
    }

    public void setBccUser(String bccUser) {
        this.bccUser = bccUser;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MailBoxParam getMailBoxParam() {
        return mailBoxParam;
    }

    public Resource[] getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(Resource[] imageFiles) {
        this.imageFiles = imageFiles;
    }

    public Resource[] getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(Resource[] attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public void setMailBoxParam(MailBoxParam mailBoxParam) {
        this.mailBoxParam = mailBoxParam;
    }

    @Override
    public String toString() {
        return "SendMailParam{" +
                "from='" + from + '\'' +
                ", toUser='" + toUser + '\'' +
                ", ccUser='" + ccUser + '\'' +
                ", bccUser='" + bccUser + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", imageFiles=" + Arrays.toString(imageFiles) +
                ", attachmentFiles=" + Arrays.toString(attachmentFiles) +
                ", mailBoxParam=" + mailBoxParam +
                ", reply=" + reply +
                ", contentType=" + contentType +
                ", heads=" + heads +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendMailParam that = (SendMailParam) o;
        return Objects.equals(from, that.from) && Objects.equals(toUser, that.toUser) && Objects.equals(ccUser, that.ccUser) && Objects.equals(bccUser, that.bccUser) && Objects.equals(subject, that.subject) && Objects.equals(content, that.content) && Arrays.equals(imageFiles, that.imageFiles) && Arrays.equals(attachmentFiles, that.attachmentFiles) && Objects.equals(mailBoxParam, that.mailBoxParam) && Objects.equals(reply, that.reply) && Objects.equals(contentType, that.contentType) && Objects.equals(heads, that.heads);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(from, toUser, ccUser, bccUser, subject, content, mailBoxParam, reply, contentType, heads);
        result = 31 * result + Arrays.hashCode(imageFiles);
        result = 31 * result + Arrays.hashCode(attachmentFiles);
        return result;
    }
}
