package com.framework.mail.spring.boot.autoconfigure.service;


import com.framework.mail.core.JavaMailPull;
import com.framework.mail.spring.boot.autoconfigure.pojo.PullMailParam;
import com.framework.mail.spring.boot.autoconfigure.pojo.SendMailParam;
import com.framework.mail.spring.boot.autoconfigure.pojo.SendMailResponse;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

public interface JavaMailService {
    //发送邮件
    SendMailResponse sendMail(SendMailParam sendMailParam);

    List<SendMailResponse> sendBatchMail(List<SendMailParam> sendMailParams);

    MimeMessage createMessage(SendMailParam sendMailDTO, JavaMailSender sender) throws MessagingException;

    //关闭连接并删除缓存
    void close(String email, String protocol);

    JavaMailSender getJavaMailSender(SendMailParam sendMailDTO);

    //创建或获取javamail从map中
    JavaMailPull getJavaMailPull(PullMailParam pullMailDTO) throws Exception;

}
