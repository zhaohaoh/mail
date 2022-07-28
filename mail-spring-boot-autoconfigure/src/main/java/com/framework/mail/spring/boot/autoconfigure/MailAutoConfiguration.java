package com.framework.mail.spring.boot.autoconfigure;

import com.framework.mail.core.manager.JavaMailManager;
import com.framework.mail.core.manager.LocalJavaMailManager;
import com.framework.mail.spring.boot.autoconfigure.properties.MailPeoperties;
import com.framework.mail.spring.boot.autoconfigure.properties.PullMailProperties;
import com.framework.mail.spring.boot.autoconfigure.properties.SendMailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author hzh
 * @date 2021/6/10
 * 没有数据库的配置的情况下使用properties
 */
@Configuration
@EnableConfigurationProperties({MailPeoperties.class, PullMailProperties.class, SendMailProperties.class})
public class MailAutoConfiguration {
    @Bean
    public JavaMailManager javaMailManager() {
        return new LocalJavaMailManager();
    }

}
