package com.framework.mail.core;


import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.framework.mail.core.exception.MailException;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import javax.mail.*;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Pop3JavaMailPull implements com.framework.mail.core.JavaMailPull {
    protected static final Logger log = LoggerFactory.getLogger(Pop3JavaMailPull.class);
    /**
     * The default port: -1.
     */
    public static final int DEFAULT_PORT = -1;

    private Properties javaMailProperties = new Properties();

    @Nullable
    private Session session;

    @Nullable
    private String protocol = "pop3";

    @Nullable
    private String host;

    private int port = DEFAULT_PORT;

    @Nullable
    private String username;

    @Nullable
    private String password;

    private POP3Store store;

    private POP3Folder folder;

    private List<POP3Folder> pop3Folders = new ArrayList<>();
    private Map<String, POP3Folder> folderMap = new ConcurrentHashMap<>();

    public Map<String, POP3Folder> getFolderMap() {
        return folderMap;
    }

    public void setFolderMap(Map<String, POP3Folder> folderMap) {
        this.folderMap = folderMap;
    }

    public void setStore(POP3Store store) {
        this.store = store;
    }

    public POP3Folder getFolder() {
        return folder;
    }

    public void setFolder(POP3Folder folder) {
        this.folder = folder;
    }


    public List<POP3Folder> getPop3Folder() {
        return pop3Folders;
    }

    public void setPop3Folder(List<POP3Folder> pop3Folder) {
        this.pop3Folders = pop3Folder;
    }

    public Properties getJavaMailProperties() {
        return javaMailProperties;
    }

    public void setSession(@Nullable Session session) {
        this.session = session;
    }

    @Nullable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(@Nullable String protocol) {
        this.protocol = protocol;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    public void setHost(@Nullable String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    @Nullable
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public void setDefaultEncoding(@Nullable String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    @Nullable
    private String defaultEncoding = "utf-8";

    public void setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
        synchronized (this) {
            putSession();
        }
    }

    /**
     * Return the JavaMail {@code Session},
     * lazily initializing it if hasn't been specified explicitly.
     */
    public Session getSession() {
        if (this.session == null) {
            putSession();
        }
        return this.session;
    }

    private synchronized void putSession() {
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        this.session = Session.getInstance(this.javaMailProperties, authenticator);
    }


    @Override
    public Folder getFolder(String folderName) {
        POP3Folder pop3Folder = null;
        pop3Folder = folderMap.computeIfAbsent(folderName, a -> {
            try {
                return (POP3Folder) getStore().getFolder(folderName);
            } catch (MessagingException e) {
                log.error("获取邮箱文件夹异常", e);
                throw new MailException("获取邮箱文件夹异常");
            }
        });
        return pop3Folder;
    }

    @Override
    public Message[] search(String folderName, Date beginDate, Date endDate) throws Exception {
        SearchTerm searchTerm = null;
        SearchTerm beginDateTerm = null;
        SearchTerm endDateTerm = null;
        Message[] messages;
        if (beginDate != null) {
            Date newBeginDate = beginDate;
            //SearchTerm的时间过滤是以日期为单位没有精准到时分秒。所以只能提前一天。详细查看SearchSequence类
            newBeginDate = DateUtils.addDays(beginDate, -2);
            beginDateTerm = new SentDateTerm(ComparisonTerm.GE, newBeginDate);
            searchTerm = beginDateTerm;
        }
        if (endDate != null) {
            endDateTerm = new SentDateTerm(ComparisonTerm.LE, endDate);
            searchTerm = endDateTerm;
        }
        if (beginDate != null && endDate != null) {
            searchTerm = new AndTerm(beginDateTerm, endDateTerm);
        }
        store = getStore();
        POP3Folder folder = (POP3Folder) store.getFolder(folderName);
        pop3Folders.add(folder);
        if (folder != null) {
            this.folderMap.put(folder.getName(), folder);
        } else {
            log.error("邮箱:{}.文件夹名:{}.获取的文件夹为空", username, folderName);
            return new Message[0];
        }
        folder.open(Folder.READ_ONLY);
        if (folder.getMessageCount() <= 0) {
            return new Message[0];
        }
        // if (username.contains("@qq.") || username.contains("@163.")) {
        //            messages = folder.getMessages();
        //        }

        if (beginDate != null || endDate != null) {
            messages = folder.search(searchTerm);
        } else if (username.contains("@qq.") || username.contains("@163.")) {
            messages = folder.getMessages();
        } else {
            messages = folder.getMessages();
        }

        FetchProfile fp = new FetchProfile(); //新建一个属性对象
        fp.add(FetchProfile.Item.ENVELOPE); // 添加信封属性
        fp.add(FetchProfile.Item.FLAGS); //添加标志属性
        fp.add(UIDFolder.FetchProfileItem.UID); //添加标志属性
        folder.fetch(messages, fp);

        List<Message> list = new ArrayList<>();

        //快速排序
        try {
            fastSort(messages, 0, messages.length - 1);
            //倒叙便利,由于获取日期的操作需要协议进行通信，比较耗时。所以倒叙遍历取出有用数据，break。
            for (int i = messages.length - 1; i >= 0; i--) {
                if (messages[i].getSentDate() == null) {
                    list.add(messages[i]);
                    continue;
                }
                Date receivedDate = messages[i].getSentDate();
                if (beginDate != null && receivedDate != null && receivedDate.compareTo(beginDate) > 0 && endDate != null && receivedDate.compareTo(endDate) <= 0) {
                    list.add(messages[i]);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("获取邮件的发送日期异常");
        }
        //顺序调回去
        Collections.reverse(list);

        return list.toArray(new Message[list.size()]);
    }


    @Override
    public boolean updateMessagesByUid(String FolderName, List<String> uids, Flags.Flag flags, Boolean flag) {
        throw new RuntimeException("pop3不支持修改原消息状态");
    }

    @Override
    public void closeFolder(Folder folder) {
        try {
            if (folder == null) {
                log.error("邮箱{}文件夹为空无法关闭", username);
                return;
            }
//            if (imapFolders != null) {
//                for (IMAPFolder imapFolder : imapFolders) {
//                    if (imapFolder != null&&imapFolder.isOpen()) {
//                        imapFolder.close(false);
//                    }
//                }
//            }
            if (folder.isOpen()) {
                log.info("邮箱{}文件夹:{}关闭", this.username, folder.getFullName());
                folder.close();
            }
        } catch (MessagingException e) {
            log.error("邮箱{}文件夹:{}关闭异常", this.username, folder.getFullName());
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (this.store != null) {
                this.store.close();
            }
            if (!CollectionUtils.isEmpty(this.folderMap)) {
                this.folderMap.clear();
            }
            if (!CollectionUtils.isEmpty(this.pop3Folders)) {
                for (POP3Folder imapFolder : this.pop3Folders) {
                    if (imapFolder.isOpen()) {
                        imapFolder.close();
                    }
                }
            }
            log.info("邮箱{} pop3收件客户端完全关闭", this.username);
        } catch (MessagingException e) {
            log.error("邮箱{} pop3收件客户端完全关闭出现异常", this.username);
        }
    }


    @Override
    public synchronized List<String> getFolderNames() throws Exception {
        POP3Store store = getStore();
        Folder defaultFolder = store.getDefaultFolder();
        return Arrays.stream(defaultFolder.list()).map(Folder::getName).collect(Collectors.toList());
    }

    @Override
    public Message[] getMessageByUids(String folderName, List<String> uids) throws Exception {
        return new Message[0];
    }

    @Override
    public synchronized POP3Store getStore() {
        try {
            //存储对象
            if (this.store == null) {
                if (session == null) {
                    putSession();
                }
                this.store = (POP3Store) session.getStore(protocol);
            }
            if (!this.store.isConnected()) {
                this.store.connect(host, port, username, password);
            }
//            HashMap<String, String> IAM = new HashMap<String, String>();
////带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
//            IAM.put("name", "myname");
//            IAM.put("version", "1.0.0");
//            IAM.put("vendor", "myclient");
//            IAM.put("support-email", username);
//            store.id(IAM);
        } catch (Exception e) {
            log.error("邮件服务器邮箱{}获取store异常", username, e);
            throw new MailException("邮件服务器获取store异常");
        }
        return this.store;
    }





}
