package edu.wtu.sj.imdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import edu.wtu.sj.imdemo.R;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        FrameLayout layout = (FrameLayout) findViewById(R.id.ll_test_addemtion);
        /*EmojiLayout emojiLayout = new EmojiLayout(this);
        
        //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams()
        layout.addView(emojiLayout);*/
    }
}
