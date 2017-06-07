package edu.wtu.sj.imdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import edu.wtu.sj.imdemo.R;

/**
 * Created by 15827 on 2017/6/1.
 */

public class FaceGvAdapter extends BaseAdapter {
    private List<String> list;
    private Context mContext;

    public FaceGvAdapter(List<String> list, Context mContext) {
        super();
        this.list = list;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHodler hodler;
        if (convertView == null) {
            hodler = new ViewHodler();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.emtion_image, null);
            hodler.iv = (ImageView) convertView.findViewById(R.id.face_img);
            hodler.tv = (TextView) convertView.findViewById(R.id.face_text);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHodler) convertView.getTag();
        }
        try {
            Bitmap mBitmap = BitmapFactory.decodeStream(mContext.getAssets().open("png/" + list.get(position)));
            hodler.iv.setImageBitmap(mBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        hodler.tv.setText("png/" + list.get(position));
        //LogUtils.d("FaceGvAdapter---------------"+"png/" + list.get(position));
        return convertView;
    }

    class ViewHodler {
        ImageView iv;
        TextView tv;
    }
}
