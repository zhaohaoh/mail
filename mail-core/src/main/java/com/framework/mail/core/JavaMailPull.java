package com.framework.mail.core;


import com.sun.mail.imap.IMAPStore;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.Date;
import java.util.List;

public interface JavaMailPull {
    public Store getStore();
    //获取文件夹
    Folder getFolder(String folderName);

    //搜索邮件根据文件夹名
    Message[] search(String folderName, Date beginDate, Date endDate) throws Exception;

    //更新邮件根据uid
    boolean updateMessagesByUid(String folderName, List<String> uids, Flags.Flag flags, Boolean flag) throws Exception;

    //关闭文件夹
    void closeFolder(Folder folder);

    //关闭整个
    void close();

    //获取文件夹列表
    List<String> getFolderNames() throws Exception;

    //获取邮件根据uids
    Message[] getMessageByUids(String folderName, List<String> uids) throws Exception;

    //排序
    default void fastSort(Message[] messages, int l, int r) throws MessagingException {
        if (l < r) {
            int index = getIndex(messages, l, r);
            fastSort(messages, l, index - 1);
            fastSort(messages, index + 1, r);
        }
    }

    default int getIndex(Message[] arr, int l, int r) throws MessagingException {
        // 基准数据
        Message temp = arr[l];
        Date tempSentDate = temp.getSentDate();
        while (l < r) {
            while (l < r) {
                Date sentDate = arr[r].getSentDate();
                if ((sentDate == null || (sentDate.after(tempSentDate) || sentDate.equals(tempSentDate)))) {
                    r--;
                } else {
                    break;
                }
            }

            arr[l] = arr[r];

            while (l < r) {
                Date sentDate = arr[l].getSentDate();
                if (sentDate != null && (sentDate.before(tempSentDate) || sentDate.equals(tempSentDate))) {
                    l++;
                } else {
                    break;
                }
            }
            arr[r] = arr[l];
        }
        arr[l] = temp;
        return l;
    }
}
