package com.framework.mail.core;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.ListInfo;
import com.framework.mail.core.exception.MailException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import javax.mail.*;
import javax.mail.search.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImapJavaMailPull implements JavaMailPull {
    protected static final Logger log = LoggerFactory.getLogger(ImapJavaMailPull.class);
    /**
     * The default port: -1.
     */
    public static final int DEFAULT_PORT = -1;

    private Properties javaMailProperties = new Properties();

    @Nullable
    private Session session;

    @Nullable
    private String protocol = "imap";

    @Nullable
    private String host;

    private int port = DEFAULT_PORT;

    @Nullable
    private String username;

    @Nullable
    private String password;

    private IMAPStore store;

    private Map<String, IMAPFolder> folderMap = new ConcurrentHashMap<>();

    private List<IMAPFolder> imapFolders = new ArrayList<>();

    public void setStore(IMAPStore store) {
        this.store = store;
    }

    public Map<String, IMAPFolder> getFolderMap() {
        return folderMap;
    }

    public void setFolderMap(Map<String, IMAPFolder> folderMap) {
        this.folderMap = folderMap;
    }

    public List<IMAPFolder> getImapFolders() {
        return imapFolders;
    }

    public void setImapFolders(List<IMAPFolder> imapFolders) {
        this.imapFolders = imapFolders;
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
    public  Folder getFolder(String folderName) {
        IMAPFolder imapFolder = null;
        imapFolder = folderMap.computeIfAbsent(folderName, a -> {
            try {
                return (IMAPFolder) getStore().getFolder(folderName);
            } catch (MessagingException e) {
                log.error("???????????????????????????", e);
                throw new MailException("???????????????????????????");
            }
        });
        return imapFolder;
    }

    @Override
    public Message[] search(String folderName, Date beginDate, Date endDate) throws Exception {
        SearchTerm searchTerm = null;
        SearchTerm beginDateTerm = null;
        SearchTerm endDateTerm = null;
        Message[] messages;
        if (beginDate != null) {
            Date newBeginDate = beginDate;
            //SearchTerm??????????????????????????????????????????????????????????????????????????????????????????????????????SearchSequence???
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
        IMAPFolder folder = (IMAPFolder) getFolder(folderName);
        if (folder == null) {
            log.error("??????:{}.????????????:{}.????????????????????????", username, folderName);
            return new Message[0];
        }
        this.imapFolders.add(folder);
        folder.open(Folder.READ_ONLY);
        if (folder.getMessageCount() <= 0) {
            return new Message[0];
        }
//        if (username.contains("@qq.") || username.contains("@163.")) {
//            messages = folder.getMessages();
//        }
        if (beginDate != null || endDate != null) {
            messages = folder.search(searchTerm);
        } else if (username.contains("@qq.") || username.contains("@163.")) {
            messages = folder.getMessages();
        } else {
            messages = folder.getMessages();
        }


        List<Message> list = new ArrayList<>();
        FetchProfile fp = new FetchProfile(); //????????????????????????
        fp.add(FetchProfile.Item.ENVELOPE); // ??????????????????
//        fp.add(FetchProfile.Item.FLAGS); //??????????????????
//        fp.add(UIDFolder.FetchProfileItem.UID); //??????????????????
//        fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
        fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
//        fp.add(IMAPFolder.FetchProfileItem.CONTENT_INFO);
        if (ArrayUtils.isNotEmpty(messages)) {
            folder.fetch(messages, fp);
        }
        //????????????
        try {
            fastSort(messages, 0, messages.length - 1);
            //????????????,????????????????????????????????????????????????????????????????????????????????????????????????????????????break???
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
            log.error("?????????????????????????????????");
        }
        //???????????????
        Collections.reverse(list);
        Message[] responseMessage = list.toArray(new Message[list.size()]);
        return responseMessage;
    }


    @Override
    public synchronized boolean updateMessagesByUid(String folderName, List<String> uids, Flags.Flag flags, Boolean flag) throws Exception {
        IMAPFolder folder = null;
        if (CollectionUtils.isEmpty(uids)) {
            log.error("imap????????????????????????????????????uid??????");
            return false;
        }
        try {
            folder = (IMAPFolder) getFolder(folderName);
            folder.open(Folder.READ_WRITE);
            long[] longs = uids.stream().map(Long::parseLong).mapToLong(t -> t).toArray();
            Message[] messages = folder.getMessagesByUID(longs);
//            Message message = folder.getMessageByUID(Long.parseLong(uid));
            for (Message message : messages) {
                message.setFlag(flags, flag);
            }
            folder.expunge();
        } finally {
            close();
        }
        return true;
    }

    @Override
    public void closeFolder(Folder folder) {
        try {
            if (folder == null) {
                log.error("??????{}???????????????????????????", username);
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
                log.info("??????{}?????????:{}??????", this.username, folder.getFullName());
                folder.close(false);
            }
        } catch (MessagingException e) {
            log.error("??????{}?????????:{}????????????", this.username, folder.getFullName());
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
            if (!CollectionUtils.isEmpty(this.imapFolders)) {
                for (IMAPFolder imapFolder : this.imapFolders) {
                    if (imapFolder.isOpen()) {
                        imapFolder.close();
                    }
                }
            }
            log.info("??????{} imap???????????????????????????", this.username);
        } catch (MessagingException e) {
            log.error("??????{} imap???????????????????????????????????????", this.username);
        }
    }


    @Override
    public synchronized List<String> getFolderNames() throws Exception {
        IMAPStore store = getStore();
        IMAPFolder defaultFolder = (IMAPFolder) store.getDefaultFolder();
        ListInfo[] li;
        li = (ListInfo[]) defaultFolder.doCommandIgnoreFailure(p -> p.list("", "%"));
        return Arrays.stream(li).map(a -> a.name).collect(Collectors.toList());
    }

    @Override
    public Message[] getMessageByUids(String folderName, List<String> uids) throws Exception {
        if (CollectionUtils.isEmpty(uids)) {
            log.error("imap????????????????????????????????????uid??????");
        }
        IMAPFolder folder = (IMAPFolder) getFolder(folderName);
        folder.open(Folder.READ_WRITE);
        long[] longs = uids.stream().map(Long::parseLong).mapToLong(t -> t).toArray();
        return folder.getMessagesByUID(longs);
    }

    @Override
    public synchronized IMAPStore getStore() {
        try {
            //????????????
            if (this.store == null) {
                if (session == null) {
                    putSession();
                }
                this.store = (IMAPStore) session.getStore(protocol);
            }
            if (!this.store.isConnected()) {
                this.store.connect(host, port, username, password);
            }
            HashMap<String, String> IAM = new HashMap<String, String>();
//??????IMAP ID????????????key???value???????????????name???version???vendor???support-email?????? ???????????????????????????????????????
            if (username != null && username.contains("@163.com")) {
                IAM.put("name", "yonboo");
                IAM.put("version", "1.0.0");
                IAM.put("vendor", "yonbooClient");
                IAM.put("support-email", username);
                store.id(IAM);
            }
        } catch (Exception e) {
            log.error("?????????????????????{}??????store??????", username, e);
            throw new MailException("?????????????????????store??????");
        }
        return this.store;
    }

    /**
     * ?????????15?????????????????????????????????????????????
     */
    @Scheduled(cron = "0 */15 * * * ?")
    protected void noop() {
        log.info("??????????????????,????????????????????????");
        if (CollectionUtils.isEmpty(folderMap)) {
            return;
        }
        folderMap.forEach((k, v) -> {
            try {
                Object val = v.doCommand(p -> {
                    p.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (MessagingException e) {
                log.error("NOOP ??????", e);
            }
        });
        log.info("??????NOOP ??????");
    }


}
