package com.framework.mail.spring.boot.autoconfigure.service.impl;

import com.framework.mail.core.JavaMailPull;
import com.framework.mail.core.constant.MailContentTypeEnum;
import com.framework.mail.core.exception.MailException;
import com.framework.mail.core.exception.QueueFullException;
import com.framework.mail.core.exception.SendMailException;
import com.framework.mail.core.manager.JavaMailManager;
import com.framework.mail.core.model.JavaMailParamer;
import com.framework.mail.core.provider.JavaMailProvider;
import com.framework.mail.spring.boot.autoconfigure.pojo.MailBoxParam;
import com.framework.mail.spring.boot.autoconfigure.pojo.PullMailParam;
import com.framework.mail.spring.boot.autoconfigure.pojo.SendMailParam;
import com.framework.mail.spring.boot.autoconfigure.pojo.SendMailResponse;
import com.framework.mail.spring.boot.autoconfigure.properties.MailPeoperties;
import com.framework.mail.spring.boot.autoconfigure.properties.PullMailProperties;
import com.framework.mail.spring.boot.autoconfigure.properties.SendMailProperties;
import com.framework.mail.spring.boot.autoconfigure.service.JavaMailService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: hzh
 * @Date: 2022/6/13 12:06
 */
@Service
public class JavaMailServiceImpl implements JavaMailService {
    protected static final Logger log = LoggerFactory.getLogger(JavaMailServiceImpl.class);

    private final ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(10, 50,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(2048), new ThreadFactory() {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private static final String NAME_PREFIX = "mail-thread-";

        {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, NAME_PREFIX + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    //发送邮件的模板引擎
    @Autowired
    private FreeMarkerConfigurer configurer;
    @Autowired(required = false)
    private PullMailProperties pullMailProperties;
    @Autowired(required = false)
    private SendMailProperties sendMailProperties;
    @Autowired(required = false)
    private MailPeoperties mailPeoperties;
    @Autowired
    private JavaMailManager javaMailManager;


    private JavaMailParamer getSendJavaMailParamer(MailBoxParam mailConfigInfo) {
        JavaMailParamer javaMailParamer = new JavaMailParamer();
        if (mailConfigInfo != null) {
            BeanUtils.copyProperties(mailConfigInfo, javaMailParamer);
        } else {
            if (mailPeoperties.getEnable() == null || !mailPeoperties.getEnable()) {
                throw new MailException("获取不到邮件配置且系统邮件配置未开启");
            }
            BeanUtils.copyProperties(sendMailProperties, javaMailParamer);
            javaMailParamer.setUsername(mailPeoperties.getUsername());
            javaMailParamer.setPassword(mailPeoperties.getPassword());
        }
        return javaMailParamer;
    }

    private JavaMailParamer getPullJavaMailParamer(MailBoxParam mailConfigInfo) {
        JavaMailParamer javaMailParamer = new JavaMailParamer();
        if (mailConfigInfo != null) {
            BeanUtils.copyProperties(mailConfigInfo, javaMailParamer);
        } else {
            if (mailPeoperties.getEnable() == null || !mailPeoperties.getEnable()) {
                throw new MailException("获取不到邮件配置且系统邮件配置未开启");
            }
            BeanUtils.copyProperties(pullMailProperties, javaMailParamer);
            javaMailParamer.setUsername(mailPeoperties.getUsername());
            javaMailParamer.setPassword(mailPeoperties.getPassword());
        }
        return javaMailParamer;
    }

    @Override
    public SendMailResponse sendMail(SendMailParam sendMailParam) {
        JavaMailParamer javaMailParamer = getSendJavaMailParamer(sendMailParam.getMailBoxParam());
        JavaMailSender sender = JavaMailProvider.getSender(javaMailParamer);
        SendMailResponse sendMailResponse = new SendMailResponse();
        sendMailResponse.setSuccess(true);
        sendMailResponse.setTo(sendMailParam.getToUser());
        try {
            MimeMessage message = createMessage(sendMailParam, sender);
            send(message, sender);
        } catch (Exception e) {
            sendMailResponse.setSuccess(false);
            log.error("send mail failed ", e);
            sendMailResponse.setError(e.getMessage());
        }
        return sendMailResponse;
    }

    @Override
    public MimeMessage createMessage(SendMailParam sendMailParam, JavaMailSender sender) throws MessagingException {
        //定义邮件日志附件类
        //构造消息
        MimeMessage mimeMessage = sender.createMimeMessage();
        // 设置utf-8或GBK编码，否则邮件会有乱码
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        if (sendMailParam.getReply() != null && sendMailParam.getReply()) {
            mimeMessage.addHeader("Disposition-Notification-To", "1");
        }
        if (!CollectionUtils.isEmpty(sendMailParam.getHeads())) {
            sendMailParam.getHeads().forEach((k, v) -> {
                try {
                    mimeMessage.addHeader(k, v);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
        }
        mimeMessage.setSentDate(new Date());
        messageHelper.setFrom(sendMailParam.getMailBoxParam().getUsername());
        //填充消息的发送对象
        if (StringUtils.isNotBlank(sendMailParam.getToUser())) {
            String[] split = sendMailParam.getToUser().split(",");
            messageHelper.setTo(split);
        }
        if (StringUtils.isNotBlank(sendMailParam.getCcUser())) {
            String[] split = sendMailParam.getCcUser().split(",");
            messageHelper.setCc(split);
        }
        if (StringUtils.isNotBlank(sendMailParam.getBccUser())) {
            String[] split = sendMailParam.getBccUser().split(",");
            messageHelper.setBcc(split);
        }
        if (StringUtils.isNotBlank(sendMailParam.getFrom())) {
            messageHelper.setFrom(sendMailParam.getFrom());
        }
        messageHelper.setSubject(sendMailParam.getSubject());
        //构建附件日志
        handlerMailAttachment(sendMailParam.getImageFiles(), sendMailParam.getAttachmentFiles(), messageHelper);
        //处理模板
        if (sendMailParam.getContent() != null) {
            messageHelper.setText(sendMailParam.getContent(), sendMailParam.getContentType().equals(MailContentTypeEnum.HTML.key()));
        }
        //设置是新邮件，虽然设了没用
        mimeMessage.setFlag(Flags.Flag.RECENT, true);
        return mimeMessage;
    }

    @Override
    public void close(String email, String protocol) {
        JavaMailPull remove = javaMailManager.remove(email, protocol);
        if (remove != null) {
            remove.close();
        }
    }

    @Override
    public JavaMailSender getJavaMailSender(SendMailParam sendMailParam) {
        JavaMailParamer javaMailParamer = getSendJavaMailParamer(sendMailParam.getMailBoxParam());
        return JavaMailProvider.getSender(javaMailParamer);
    }

    @Override
    public JavaMailPull getJavaMailPull(PullMailParam pullMailParam) {
        JavaMailParamer javaMailParamer = getPullJavaMailParamer(pullMailParam.getMailConfigInfo());
        return javaMailManager.getPull(javaMailParamer);
    }

    //邮件的日志应该在发送之前就做记录，发送完再更新邮件的发送状态。所以不需要记录附件返回
    private void send(MimeMessage message, JavaMailSender sender) {
        sender.send(message);
        log.info("[Email] mail send success");
    }

    private void handlerMailAttachment(Resource[] imageFiles, Resource[] attachmentFiles, MimeMessageHelper messageHelper) throws MessagingException {
        //添加图片
        if (ArrayUtils.isNotEmpty(imageFiles)) {
            for (Resource file : imageFiles) {
                String name = getName(file);
                //处理中文异常
                String newFileName = "";
                try {
                    newFileName = MimeUtility.encodeWord(name, "utf-8", "B");
                } catch (UnsupportedEncodingException e) {
                    log.error("[Email] Handling Chinese exceptions in email attachment names", e);
                }
                messageHelper.addInline(newFileName, file);
            }
        }
        //添加附件
        if (ArrayUtils.isNotEmpty(attachmentFiles)) {
            for (Resource multipartFile : attachmentFiles) {
                String name = getName(multipartFile);
                //处理中文异常
                String newFileName = "";
                try {
                    newFileName = MimeUtility.encodeWord(name, "utf-8", "B");
                } catch (UnsupportedEncodingException e) {
                    log.error("[Email] Handling Chinese exceptions in email attachment names", e);
                }
                messageHelper.addAttachment(newFileName, multipartFile);
            }
        }
    }

    private void handlerFreemarker(String content, Map<String, Object> model, String mailTemplateName, String mailTemplateContent,
                                   MimeMessageHelper messageHelper, Integer html) throws MessagingException {
        //默认使用内容填充
        if (StringUtils.isNotBlank(mailTemplateContent)) {
            try {
                StringTemplateLoader stl = new StringTemplateLoader();
                stl.putTemplate("mailTemplate", mailTemplateContent);
                Configuration configuration = configurer.getConfiguration();
                configuration.setTemplateLoader(stl);
                //获取模板
                Template template = configuration.getTemplate(mailTemplateName);
                //赋值后的模板邮件内容
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                messageHelper.setText(text, true);//邮件内容
            } catch (Exception e) {
                log.error("[Email]  FreeMarker error", e);
            }
        }
        //判断是否使用模板引擎
        else if (StringUtils.isNotBlank(mailTemplateName)) {
            try {
                Configuration configuration = configurer.getConfiguration();
                //获取模板
                Template template = configuration.getTemplate(mailTemplateName);
                //赋值后的模板邮件内容
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                messageHelper.setText(text, true);//邮件内容
            } catch (Exception e) {
                log.error("[Email]  FreeMarker error", e);
            }
        }
    }

    private String getName(Resource file) {
        return file.getFilename();
    }

    // 阿里云批量发送一次最多支持 100封每秒. 海外邮件服务器最多支持1000一次批量发送.所以这里加锁限制只能一次一次批量发
    // 多台服务器暂时不考虑
    @Override
    public synchronized List<SendMailResponse> sendBatchMail(List<SendMailParam> sendMailParams) {
        boolean can = verifySend();
        if (!can) {
            throw new QueueFullException("sendBatch queue is Full");
        }
        List<SendMailResponse> results = new ArrayList<>();
        List<Future<SendMailResponse>> futures = new ArrayList<>();
        if (sendMailParams.size() > 1000) {
            throw new SendMailException("sendBatch toUser limit 1000");
        }
        for (SendMailParam sendMailParam : sendMailParams) {
            Future<SendMailResponse> future = taskExecutor.submit(() -> {
                SendMailResponse sendMailResponse = new SendMailResponse();
                sendMailResponse.setTo(sendMailParam.getToUser());
                sendMailResponse.setRequestId(sendMailParam.getRequestId());
                try {
                    SendMailResponse mailResponse = sendMail(sendMailParam);
                    if (mailResponse != null) {
                        return mailResponse;
                    }
                } catch (Exception e) {
                    sendMailResponse.setSuccess(false);
                    sendMailResponse.setError(e.getMessage());
                    return sendMailResponse;
                }
                sendMailResponse.setSuccess(true);
                return sendMailResponse;
            });
            futures.add(future);
        }
        for (Future<SendMailResponse> result : futures) {
            SendMailResponse sendMailResponse = new SendMailResponse();
            try {
                sendMailResponse = result.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                sendMailResponse.setError(e.getMessage());
                log.error("send Future error", e);
            }
            results.add(sendMailResponse);
        }
        return results;
    }

    // 加了锁原则上是不会出现.预留代码校验一下
    @Deprecated
    private synchronized boolean verifySend() {
        BlockingQueue<Runnable> queue = taskExecutor.getQueue();
        if (queue.size() > 1024) {
            return false;
        }
        return true;
    }


}
