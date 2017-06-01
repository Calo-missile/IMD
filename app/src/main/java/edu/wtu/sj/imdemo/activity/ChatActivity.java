package edu.wtu.sj.imdemo.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.wtu.sj.imdemo.Provider.SmsProvider;
import edu.wtu.sj.imdemo.R;
import edu.wtu.sj.imdemo.dbhelper.SmsOpenHelper;
import edu.wtu.sj.imdemo.service.ImService;
import edu.wtu.sj.imdemo.utils.ThreadUtils;
import edu.wtu.sj.imdemo.utils.ToastUtils;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String CLICK_ACCOUNT = "click_account";
    public static final String CLICK_NICKNAME = "click_nickname";
    public static final int SEND = 1;
    public static final int RECEIVE = 0;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.lv_chat)
    ListView mLvChat;
    @InjectView(R.id.et_content)
    EditText mEtContent;
    @InjectView(R.id.btn_send)
    Button mBtnSend;
    @InjectView(R.id.ib_emotion)
    ImageView ivEmotion;

    private String mClickaccount;
    private String mClicknickname;
    private CursorAdapter mAdapter;
    private ImService mImService;
    private MyServiceConnection mMyServiceConnection = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);
        init();
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        unRegisterMyContentObserver();
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }

    private void init() {
        registerMyContentObserver();
        Log.d("aaaaa", "init: " + ImService.mCurrentAccount.toString());
        mClickaccount = getIntent().getStringExtra(ChatActivity.CLICK_ACCOUNT);
        mClicknickname = getIntent().getStringExtra(ChatActivity.CLICK_NICKNAME);
        mTitle.setText("与" + mClicknickname + "聊天中");
        //通过bindservice方式调用服务方法
        Intent service = new Intent(ChatActivity.this, ImService.class);  //activity与service绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        //mBtnSend.setOnClickListener(this);
        ivEmotion.setOnClickListener(this);
    }

    private void initData() {
        setOrUpdateAdapter();
    }

    private void setOrUpdateAdapter() {
        if (mAdapter != null) {
            mAdapter.getCursor().requery();
            mLvChat.setSelection(mAdapter.getCount() - 1);
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS,
                        null,
                        "(from_account=? and to_account=?)or(to_account=? and from_account=?)",
                        new String[]{ImService.mCurrentAccount, mClickaccount, ImService.mCurrentAccount, mClickaccount},
                        SmsOpenHelper.SmsTable.TIME + " ASC");
                if (cursor.getCount() < 1) {
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new CursorAdapter(ChatActivity.this, cursor) {

                            @Override
                            public int getItemViewType(int position) {
                                cursor.moveToPosition(position);
                                String from_account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
                                if (ImService.mCurrentAccount.equals(from_account)) {
                                    return SEND;
                                } else {
                                    return RECEIVE;
                                }
                            }

                            @Override
                            public int getViewTypeCount() {
                                return 2;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                ViewHolder holder = null;
                                switch (getItemViewType(position)) {
                                    case SEND:
                                        if (convertView == null) {
                                            convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                                            holder = new ViewHolder();
                                            convertView.setTag(holder);
                                            holder.body = (TextView) convertView.findViewById(R.id.content);
                                            holder.time = (TextView) convertView.findViewById(R.id.time);
                                            holder.head = (ImageView) convertView.findViewById(R.id.head);
                                        } else {
                                            holder = (ViewHolder) convertView.getTag();
                                        }
                                        break;
                                    case RECEIVE:
                                        if (convertView == null) {
                                            convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receive, null);
                                            holder = new ViewHolder();
                                            convertView.setTag(holder);
                                            holder.body = (TextView) convertView.findViewById(R.id.content);
                                            holder.time = (TextView) convertView.findViewById(R.id.time);
                                            holder.head = (ImageView) convertView.findViewById(R.id.head);
                                        } else {
                                            holder = (ViewHolder) convertView.getTag();
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                cursor.moveToPosition(position);
                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String time = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
                                holder.body.setText(body);
                                holder.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time))));
                                return super.getView(position, convertView, parent);
                            }

                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                return null;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {

                            }

                            class ViewHolder {
                                TextView time;
                                TextView body;
                                ImageView head;
                            }
                        };
                        mLvChat.setAdapter(mAdapter);
                        mLvChat.setSelection(mAdapter.getCount() - 1);
                    }
                });
            }
        });
    }

    private void initListener() {

    }

    @OnClick(R.id.btn_send)
    public void send() {
        final String s = mEtContent.getText().toString();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
//                    //1.获取消息管理者
//                    ChatManager chatManager = ImService.conn.getChatManager();
//                    //2.创建聊天对象
//                    Chat chat = chatManager.createChat(mClickaccount, new MyMessageListener());
//                    //3.创建消息对象
                    Message msg = new Message();
                    msg.setFrom(ImService.mCurrentAccount);
                    msg.setTo(mClickaccount);
                    msg.setBody(s);
//                    //4.发送消息
//                    chat.sendMessage(msg);
                    //发送消息
                    mImService.sendMessage(msg);
                    ThreadUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //消息框清空
                            mEtContent.setText("");
                        }
                    });
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //表情
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_emotion:
                ToastUtils.showToastSafe(this, "emtion");
                
                break;
            default:
                break;
        }
    }


    private class MyMessageListener implements MessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {
            ToastUtils.showToastSafe(ChatActivity.this, message.getBody());
        }
    }

    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    public void registerMyContentObserver() {
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    public void unRegisterMyContentObserver() {
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            setOrUpdateAdapter();
            super.onChange(selfChange, uri);
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("----------------onServiceConnected-----------");
            ImService.MyBinder binder = (ImService.MyBinder) service;
            mImService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
