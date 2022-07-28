package com.framework.mail.spring.boot.autoconfigure.pojo;


import java.util.Date;
import java.util.Objects;

public class PullMailParam {
    /**
     * 开始时间
     */
    private Date beginDate;
    /**
     * 结束时间
     */
    private Date endDate;
    /**
     * 邮件配置信息请求体
     */
    private MailBoxParam mailConfigInfo;
    private Long mailConfigId;
    private Boolean pullAll = false;

    public Boolean getPullAll() {
        return pullAll;
    }

    public void setPullAll(Boolean pullAll) {
        this.pullAll = pullAll;
    }

    public Long getMailConfigId() {
        return mailConfigId;
    }

    public void setMailConfigId(Long mailConfigId) {
        this.mailConfigId = mailConfigId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public MailBoxParam getMailConfigInfo() {
        return mailConfigInfo;
    }

    public void setMailConfigInfo(MailBoxParam mailConfigInfo) {
        this.mailConfigInfo = mailConfigInfo;
    }

    @Override
    public String toString() {
        return "PullMailParam{" +
                "beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", mailConfigInfo=" + mailConfigInfo +
                ", mailConfigId=" + mailConfigId +
                ", pullAll=" + pullAll +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PullMailParam that = (PullMailParam) o;
        return Objects.equals(beginDate, that.beginDate) && Objects.equals(endDate, that.endDate) && Objects.equals(mailConfigInfo, that.mailConfigInfo) && Objects.equals(mailConfigId, that.mailConfigId) && Objects.equals(pullAll, that.pullAll);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginDate, endDate, mailConfigInfo, mailConfigId, pullAll);
    }
}
