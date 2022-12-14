package com.framework.mail.core.provider;


import com.framework.mail.core.ImapJavaMailPull;
import com.framework.mail.core.JavaMailPull;
import com.framework.mail.core.Pop3JavaMailPull;
import com.framework.mail.core.exception.MailException;
import com.framework.mail.core.constant.ProtocolEnum;
import com.framework.mail.core.model.JavaMailParamer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static com.framework.mail.core.constant.MailConstant.CHARSET;
import static com.framework.mail.core.constant.ProtocolEnum.SSL;
import static com.framework.mail.core.constant.ProtocolEnum.TLS;


/**
 * @author hzh
 * @date 2021/6/9
 */
public class JavaMailProvider {

    public static JavaMailSender getSender(JavaMailParamer javaMailParamer) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(javaMailParamer.getHost());
        javaMailSender.setUsername(javaMailParamer.getUsername());
        javaMailSender.setPassword(javaMailParamer.getPassword());
        javaMailSender.setDefaultEncoding(CHARSET);
        if (javaMailParamer.getPort() != null) {
            javaMailSender.setPort(javaMailParamer.getPort());
        }
        javaMailSender.setJavaMailProperties(createProperties(javaMailParamer));
        return javaMailSender;
    }

    public static JavaMailPull getPull(JavaMailParamer javaMailParamer) {
        if (ProtocolEnum.IMAP.getCode().equalsIgnoreCase(javaMailParamer.getProtocol())) {
            ImapJavaMailPull mailPull = new ImapJavaMailPull();
            mailPull.setHost(javaMailParamer.getHost());
            mailPull.setUsername(javaMailParamer.getUsername());
            mailPull.setPassword(javaMailParamer.getPassword());
            mailPull.setDefaultEncoding(CHARSET);
            mailPull.setPort(javaMailParamer.getPort());
            mailPull.setJavaMailProperties(createProperties(javaMailParamer));
            return mailPull;
        } else if (ProtocolEnum.POP3.getCode().equalsIgnoreCase(javaMailParamer.getProtocol())) {
            Pop3JavaMailPull mailPull = new Pop3JavaMailPull();
            mailPull.setHost(javaMailParamer.getHost());
            mailPull.setUsername(javaMailParamer.getUsername());
            mailPull.setPassword(javaMailParamer.getPassword());
            mailPull.setDefaultEncoding(CHARSET);
            mailPull.setPort(javaMailParamer.getPort());
            mailPull.setJavaMailProperties(createProperties(javaMailParamer));
            return mailPull;
        }
        throw new MailException("????????????JavaMailPull???????????????" + javaMailParamer.getUsername() + "," + javaMailParamer.getProtocol());
    }


    private static Properties createProperties(JavaMailParamer javaMailParamer) {
//        if ("pop".equals(javaMailParamer.getProtocol())) {
//            javaMailParamer.setProtocol("pop3");
//        }
        Properties p = new Properties();
        // ???????????????
//        p.setProperty("mail." + javaMailParamer.getProtocol() + ".timeout", String.valueOf(MailConstant.REQUEST_TIMEOUT * 1000));
        p.setProperty("mail." + javaMailParamer.getProtocol() + ".auth", "true");
        p.setProperty("mail.transport.protocol", javaMailParamer.getProtocol());
        //???????????????????????????????????????
        p.setProperty("mail." + javaMailParamer.getProtocol() + ".partialfetch", "false");
//        //????????????
        if (javaMailParamer.getPort() != null && javaMailParamer.getPort() != 0) {
            p.setProperty("mail." + javaMailParamer.getProtocol() + ".port", javaMailParamer.getPort().toString());
        }
        p.setProperty("mail.mime.splitlongparameters", "false");

        /*
         *  ???????????????????????????????????????????????????????????????????????????????????????bean????????? ??????????????????????????????????????????
         */
        if (SSL.getCode().equals(javaMailParamer.getCrypt())) {
            p.setProperty("mail." + javaMailParamer.getProtocol() + ".ssl.enable", "true");
            p.setProperty("mail." + javaMailParamer.getProtocol() + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            //POP3?????????tls ????????????
            if (!ProtocolEnum.POP3.getCode().equalsIgnoreCase(javaMailParamer.getProtocol())) {
                p.setProperty("mail." + javaMailParamer.getProtocol() + ".starttls.enable", "true");
                p.setProperty("mail." + javaMailParamer.getProtocol() + ".starttls.required", "true");
            }
        }
        if (TLS.getCode().equals(javaMailParamer.getCrypt())) {
            //POP3?????????tls ????????????
            if (!ProtocolEnum.POP3.getCode().equalsIgnoreCase(javaMailParamer.getProtocol())) {
                p.setProperty("mail." + javaMailParamer.getProtocol() + ".starttls.enable", "true");
                p.setProperty("mail." + javaMailParamer.getProtocol() + ".starttls.required", "true");
            }
        }
        return p;
    }


}
