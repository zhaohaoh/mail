package com.framework.mail.core.manager;

import com.framework.mail.core.JavaMailPull;
import com.framework.mail.core.provider.JavaMailProvider;
import com.framework.mail.core.model.JavaMailParamer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;



public class LocalJavaMailManager implements JavaMailManager {

    //可以尝试在每次新增修改邮箱配置的时候就加入或更新缓存
    public static final ConcurrentHashMap<String, JavaMailPull> JAVAMAIL_PULL_MAP = new ConcurrentHashMap<>();

    @Override
    public  JavaMailPull getPull(JavaMailParamer javaMailParamer) {
        return JAVAMAIL_PULL_MAP.computeIfAbsent(getKey(javaMailParamer.getUsername(),javaMailParamer.getProtocol()), pull -> JavaMailProvider.getPull(javaMailParamer));
    }

    @Override
    public   JavaMailPull remove(String username,String protocol) {
        return JAVAMAIL_PULL_MAP.remove(getKey(username,protocol));
    }

    @Override
    public   JavaMailPull get(String username,String protocol) {
        return JAVAMAIL_PULL_MAP.get(getKey(username,protocol));
    }
    private   String  getKey(String username,String protocol){
        return username+":"+protocol;
    }


}
