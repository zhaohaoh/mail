package com.framework.mail.core.manager;

import com.framework.mail.core.JavaMailPull;
import com.framework.mail.core.model.JavaMailParamer;

public interface JavaMailManager {
    JavaMailPull getPull(JavaMailParamer javaMailParamer);
    JavaMailPull remove(String username,String protocol);
    JavaMailPull get(String username,String protocol);
}
