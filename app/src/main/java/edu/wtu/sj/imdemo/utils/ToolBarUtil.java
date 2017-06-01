package edu.wtu.sj.imdemo.utils;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.wtu.sj.imdemo.R;


/**
 * Created by admin on 2017/4/26.
 */

public class ToolBarUtil {
    private List<TextView> mTextViews = new ArrayList<TextView>();

    public void createToolBar(LinearLayout mBotton, String[] toolBarTitleArr, int[] iconArr) {
        for (int i = 0; i < toolBarTitleArr.length; i++) {
            TextView tv = (TextView) View.inflate(mBotton.getContext(), R.layout.inflate_toolbar_tn, null);
            tv.setText(toolBarTitleArr[i]);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, iconArr[i], 0, 0);
            int width = 0;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, height);
            param.weight = 1;
            mBotton.addView(tv, param);
            mTextViews.add(tv);
            //需要将toolBarUtil的position传给MainActivity这需要接口回调
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //3.在需要传值的地方，用接口变量调用接口方法
                    mOnToolBarClickListener.onToolBarClick(finalI);
                }
            });
        }
    }

    public void changeColor(int position) {
        for (TextView tv : mTextViews) {
            tv.setSelected(false);
        }
        mTextViews.get(position).setSelected(true);
    }

    //1.定义一个接口
    public interface onToolBarClickListener {
        void onToolBarClick(int position);
    }

    //2.定义一个接口变量
    onToolBarClickListener mOnToolBarClickListener;

    //4.提供一个公共的方法，给需要得到值的对象调用
    public void setOnToolBarClickListener(onToolBarClickListener onToolBarClickListener) {
        this.mOnToolBarClickListener = onToolBarClickListener;
    }
}
