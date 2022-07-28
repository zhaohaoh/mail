package com.framework.mail.core.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class MailMessageUtils {

    //获取发件人和地址
    public static InternetAddress getFromAddr(Message message) throws MessagingException {
        Address[] from = message.getFrom();
        if (ArrayUtils.isEmpty(from)) {
            return null;
        }
        InternetAddress[] address = (InternetAddress[]) from;
//        String from = address[0].getAddress();
//        String personal = address[0].getPersonal();

//        if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(personal)) {
//            String fromAddr = personal + "<" + from + ">";
//            return fromAddr;
//        }
        return address[0];
    }

    public static boolean isRead(Message message) throws MessagingException {
        boolean isNew = false;
        Flags flags = (message).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isNew = true;
                // break;
            }
        }
        return isNew;
    }


    /**
     * 　*　获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同
     * 　*　"to"----收件人　"cc"---抄送人地址　"bcc"---密送人地址
     */
    public static String getMailAddress(MimeMessage mimeMessage, String type) throws Exception {
        StringBuilder mailAddr = new StringBuilder();
        String addType = type.toUpperCase();

        InternetAddress[] address = null;
        if ("TO".equals(addType) || "CC".equals(addType)
                || "BCC".equals(addType)) {

            if ("TO".equals(addType)) {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.TO);
            } else if ("CC".equals(addType)) {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.CC);
            } else {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.BCC);
            }

            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String emailAddr = address[i].getAddress();
                    if (emailAddr == null) {
                        emailAddr = "";
                    } else {
                        emailAddr = MimeUtility.decodeText(emailAddr);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null) {
                        personal = "";
                    } else {
                        personal = MimeUtility.decodeText(personal);
                    }
                    String compositeto = personal + "<" + emailAddr + ">";
                    mailAddr.append(",").append(compositeto);
                }
                if (StringUtils.isBlank(mailAddr)){
                    return " ";
                }
                    mailAddr = new StringBuilder(mailAddr.substring(1));
            }
        } else {
            throw new Exception("错误的电子邮件类型!");
        }
        return mailAddr.toString();
    }

    /**
     * 　　*　判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
     */
    public static boolean getReply(MimeMessage mimeMessage) throws MessagingException {
        boolean replySign = false;
        String needReply[] = mimeMessage
                .getHeader("Disposition-Notification-To");
        if (needReply != null) {
            replySign = true;
        }
        return replySign;
    }


}
