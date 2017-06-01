package edu.wtu.sj.imdemo.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.wtu.sj.imdemo.Provider.ContactsProvider;
import edu.wtu.sj.imdemo.Provider.SmsProvider;
import edu.wtu.sj.imdemo.dbhelper.ContactOpenHelper;
import edu.wtu.sj.imdemo.dbhelper.SmsOpenHelper;
import edu.wtu.sj.imdemo.utils.LogUtils;
import edu.wtu.sj.imdemo.utils.ThreadUtils;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by admin on 2017/5/3.
 */

public class ImService extends Service {
    public static XMPPConnection conn;
    public static String mCurrentAccount;  //当前用户
    public static Roster mRoster;
    private ChatManager mChatManager;
    private Map<String, Chat> mChatMap = new HashMap<>();
    private MyMessageListener mMyMessageListener = new MyMessageListener();
    private MyChatManagerListener mMyChatManagerListener = new MyChatManagerListener();
    private Chat mCurChat;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public ImService getService() {
            return ImService.this;
        }
    }
    @Override
    public void onCreate() {
        System.out.println("---------------------------Service onCreate");
        super.onCreate();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mRoster = ImService.conn.getRoster();  //获取联系人列表
                mRoster.addRosterListener(new MyRosterListener());  //联系人变动监听
                Collection<RosterEntry> entries = mRoster.getEntries();  //获取联系人实体集合
                for (RosterEntry entry : entries) {
                    saveOrUpdate(entry);  //在数据库中保存或修改联系人实体信息
                }
                if (mChatManager == null) {
                    mChatManager = ImService.conn.getChatManager();
                    mChatManager.addChatListener(mMyChatManagerListener);
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mChatManager != null) {
            mChatManager.removeChatListener(mMyChatManagerListener);
        }
        if (mCurChat != null) {
            mCurChat.removeMessageListener(mMyMessageListener);
        }
        super.onDestroy();
    }

    private class MyRosterListener implements RosterListener {
        @Override
        public void entriesAdded(Collection<String> collection) {
            System.out.println("----------------entriesAdded-------------------");
            for (String s : collection) {
                RosterEntry entry = mRoster.getEntry(s);
                saveOrUpdate(entry);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> collection) {
            System.out.println("----------------entriesUpdated-------------------");
            for (String s : collection) {
                RosterEntry entry = mRoster.getEntry(s);
                saveOrUpdate(entry);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> collection) {
            System.out.println("----------------entriesDeleted-------------------");
            for (String s : collection) {
                getContentResolver().delete(ContactsProvider.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                        new String[]{s});
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            System.out.println("----------------presenceChanged-------------------");
            //presence = new Presence(Presence.Type.available);
            mPresenceChangedListener.onPresenceChanged(presence);
            LogUtils.d("ImService+presenceChanged:" + presence);
        }
    }

    public static void setPresenceChangedListener(PresenceChangedListener presenceChangedListener) {
        mPresenceChangedListener = presenceChangedListener;
    }

    public static PresenceChangedListener mPresenceChangedListener;

    public interface PresenceChangedListener{
        void onPresenceChanged(Presence presence);
    }

    private void saveOrUpdate(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = entry.getUser();
        String nickname = entry.getName();
        if (nickname == null || "".equals(nickname)) {
            nickname = account.substring(0, account.indexOf("@"));
        }
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME, nickname);
        values.put(ContactOpenHelper.ContactTable.AVATAR, "");
        values.put(ContactOpenHelper.ContactTable.PINYIN, PinyinHelper.convertToPinyinString(nickname, ""));
        int updatecount = getContentResolver().update(ContactsProvider.URI_CONTACT,
                values,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{account});
        if (updatecount <= 0) {
            getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
        }
    }

    private void saveMessage(Message msg) {
        ContentValues values = new ContentValues();
        String from_account = msg.getFrom().substring(0, msg.getFrom().indexOf("@")) + "@localhost";
        String to_account = msg.getTo().substring(0, msg.getTo().indexOf("@")) + "@localhost";
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, from_account);
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, to_account);
        values.put(SmsOpenHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS, "");
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        if (ImService.mCurrentAccount.equals(from_account)) {
            values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, to_account);
        } else {
            values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, from_account);
        }
        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    public void sendMessage(Message msg) throws XMPPException{
        String toAccount = msg.getTo();
        if (mChatMap.containsKey(toAccount)) {
            mCurChat = mChatMap.get(toAccount);  //当前聊天对象
        } else {
            mCurChat = mChatManager.createChat(toAccount, mMyMessageListener);
            mChatMap.put(toAccount, mCurChat);
        }
        mCurChat.sendMessage(msg);
        saveMessage(msg);
    }

    private class MyMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            saveMessage(message);
            //System.out.println("来自livsun1的消息，内容为" + message.getBody());
        }
    }

    private class MyChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean b) {
            System.out.println("----------ChatManagerListener created-----------");
            String participant = chat.getParticipant();  //参与者
            if (!mChatMap.containsKey(participant)) {
                mChatMap.put(participant, chat);
                chat.addMessageListener(mMyMessageListener);
            }
        }
    }

}
