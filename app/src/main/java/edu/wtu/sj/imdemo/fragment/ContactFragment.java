package edu.wtu.sj.imdemo.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import edu.wtu.sj.imdemo.Provider.ContactsProvider;
import edu.wtu.sj.imdemo.R;
import edu.wtu.sj.imdemo.activity.ChatActivity;
import edu.wtu.sj.imdemo.service.ImService;
import edu.wtu.sj.imdemo.utils.ThreadUtils;

import static edu.wtu.sj.imdemo.R.id.account;

/**
 * 联系人界面
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private ListView mListView;
    CursorAdapter mAdapter;
    private Cursor mCursor;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
    }

    @Override
    public void onDestroy() {
        unregisterContentObserver();  //销毁内容观察者
        super.onDestroy();
    }

    private void init() {
        registerContentObserver();  //注册内容观察者，监听数据库的改变
    }

    private void initData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                setOrUpdateAdapter();
            }
        });
    }

    private void setOrUpdateAdapter() {
        if (mAdapter != null) {
            mAdapter.getCursor().requery();
            return;
        }
        mCursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);
        if (mCursor.getCount() < 0) {
            return;
        }
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                adapter(mCursor);
            }
        });
    }

    private void adapter(final Cursor cursor) {
        mAdapter = new CursorAdapter(getActivity(), cursor) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = View.inflate(context, R.layout.item_contact, null);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView tv_account = (TextView) view.findViewById(account);
                TextView tv_nickname = (TextView) view.findViewById(R.id.nickname);
                tv_account.setText(cursor.getString(1));
                tv_nickname.setText(cursor.getString(2));

                ImageView ivUser = (ImageView) view.findViewById(R.id.head);
                if (!ImService.mRoster.getPresence(cursor.getString(1)).isAvailable()) {//判断是否在线
                    ivUser.setAlpha(0.5f);//离线状态下，联系人头像设置为半透明
                }
            }
        };
        mListView.setAdapter(mAdapter);
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listview);
    }

    private boolean isOpen = true; //点击与长按  避免一起触发

    private void initListener() {
        ImService.setPresenceChangedListener(new ImService.PresenceChangedListener() {
            @Override
            public void onPresenceChanged(Presence presence) {
                mAdapter = null;  //适配器置空，重新获取数据
                setOrUpdateAdapter();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//item点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);
                String account = c.getString(1);
                String nickname = c.getString(2);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.CLICK_ACCOUNT, account);
                intent.putExtra(ChatActivity.CLICK_NICKNAME, nickname);
                if (isOpen) {
                    startActivity(intent);
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {//item长按事件
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);
                final String account = c.getString(1);
                isOpen = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                //设置对话框内的文本
                builder.setMessage("确定删除" + account + "用户么?");
                //设置确定按钮，并给按钮设置一个点击侦听，注意这个OnClickListener使用的是DialogInterface类里的一个内部接口
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    /*//先判断IM用户在服务器是否存在
                                    if (account != null) {
                                    }*/
                                    //删除用户
                                    ImService.mRoster.removeEntry(ImService.mRoster.getEntry(account));
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                //设置取消按钮
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                //使用builder创建出对话框对象
                AlertDialog dialog = builder.create();
                //显示对话框
                dialog.show();
                return isOpen = true;
            }
        });
    }

    //手机数据库监听
    MyContxentObsersver mMyContxentOberser = new MyContxentObsersver(new Handler());

    public void registerContentObserver() {
        System.out.println("aaaaaaaa" + ContactsProvider.URI_CONTACT);
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,
                true, mMyContxentOberser);
    }

    public void unregisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mMyContxentOberser);
    }

    class MyContxentObsersver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContxentObsersver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            setOrUpdateAdapter();
        }
    }

}
