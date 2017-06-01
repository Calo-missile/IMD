package edu.wtu.sj.imdemo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.wtu.sj.imdemo.R;
import edu.wtu.sj.imdemo.fragment.ContactFragment;
import edu.wtu.sj.imdemo.fragment.SessionFragment;
import edu.wtu.sj.imdemo.service.ImService;
import edu.wtu.sj.imdemo.utils.ThreadUtils;
import edu.wtu.sj.imdemo.utils.ToolBarUtil;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.main_tv_title)
    TextView mTvTitle;
    @InjectView(R.id.main_viewpager)
    ViewPager mViewpager;
    @InjectView(R.id.main_button)
    LinearLayout mButton;
    @InjectView(R.id.tv_add)
    TextView mTvAdd;

    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private ToolBarUtil mToolBarUtil;
    private String[] mToolBarTitleArr = {"会话", "联系人"};
    private int[] iconArr = {R.drawable.icon_meassage, R.drawable.icon_selfinfo};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactFragment());
        initData();
        mToolBarUtil = new ToolBarUtil();
        mToolBarUtil.createToolBar(mButton, mToolBarTitleArr, iconArr);
        mToolBarUtil.changeColor(0);

        //设置滑动事件处理
        initListener();

        //设置底部工具栏事件处理
        mToolBarUtil.setOnToolBarClickListener(new ToolBarUtil.onToolBarClickListener() {
            @Override
            public void onToolBarClick(int position) {
                mViewpager.setCurrentItem(position);
            }
        });
    }

    private void initListener() {
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mToolBarUtil.changeColor(position);
                if (position == 0) {
                    mTvTitle.setText("会  话");
                } else {
                    mTvTitle.setText("联系人");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initData() {
        mViewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void add(View view) { //添加用户
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入好友用户名：");
        //创建一个EditText对象设置为对话框中显示的View对象
        final EditText et = new EditText(this);
        builder.setView(et);
        //用户选好要选的选项后，点击确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //createEntry 创建联系人 新添加
                            ImService.mRoster.createEntry(et.getText().toString() + "@localhost", et.getText().toString(), null);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        // 取消选择
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}
