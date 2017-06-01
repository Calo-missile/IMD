package edu.wtu.sj.imdemo.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import edu.wtu.sj.imdemo.Provider.ContactsProvider;
import edu.wtu.sj.imdemo.Provider.SmsProvider;
import edu.wtu.sj.imdemo.R;
import edu.wtu.sj.imdemo.activity.ChatActivity;
import edu.wtu.sj.imdemo.dbhelper.ContactOpenHelper;
import edu.wtu.sj.imdemo.dbhelper.SmsOpenHelper;
import edu.wtu.sj.imdemo.service.ImService;
import edu.wtu.sj.imdemo.utils.ThreadUtils;

/**
 * 消息界面
 * A simple {@link Fragment} subclass.
 */
public class SessionFragment extends Fragment {


    private CursorAdapter mAdapter;
    private ListView mListView;

    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerContentObserver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterContentObserver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.sessionListView);
    }

    private void initData() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                setOrUpdateAdapter();
            }
        });
    }

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);
                String account = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                String nickname = getNickNameByAccount(account);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.CLICK_ACCOUNT, account);
                intent.putExtra(ChatActivity.CLICK_NICKNAME, nickname);
                startActivity(intent);
            }
        });
    }

    private void setOrUpdateAdapter() {
        if (mAdapter != null) {
            mAdapter.getCursor().requery();
            return;
        }
        final Cursor cursor = getActivity().getContentResolver().query(SmsProvider.URI_SESSION,
                null,
                null,
                new String[]{ImService.mCurrentAccount, ImService.mCurrentAccount},
                null);
        if (cursor.getCount() < 0) {
            return;
        }
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new CursorAdapter(getActivity(), cursor) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        View view = View.inflate(context, R.layout.item_session, null);
                        return view;
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView tv_body = (TextView) view.findViewById(R.id.body);
                        TextView tv_nickname = (TextView) view.findViewById(R.id.nickname);
                        String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                        tv_nickname.setText(getNickNameByAccount(account));
                        tv_body.setText(cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY)));
                    }
                };
                mListView.setAdapter(mAdapter);
            }
        });
    }

    public String getNickNameByAccount(String account) {
        Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT,
                null,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{account},
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        } else {
            return "";
        }
    }

    //手机数据库监听
    MyContxentObsersver mMyContxentOberser = new MyContxentObsersver(new Handler());

    public void registerContentObserver() {
        System.out.println("aaaaaaaa" + SmsProvider.URI_SMS);
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS,
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
