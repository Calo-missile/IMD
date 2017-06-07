package edu.wtu.sj.imdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import edu.wtu.sj.imdemo.R;
import edu.wtu.sj.imdemo.service.ImService;
import edu.wtu.sj.imdemo.utils.ThreadUtils;
import edu.wtu.sj.imdemo.utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    public static final String HOST = "1111111111";  //ip
    public static final int PORT = 5222;  //服务器端口号
    private EditText mUsername;  //用户名
    private EditText mPassword;  //密码
    private Button mLogin;  //登陆按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
    }

    private void initView() {
        mUsername = (EditText) findViewById(R.id.et_username);
        mPassword = (EditText) findViewById(R.id.et_password);
        mLogin = (Button) findViewById(R.id.btn_login);
    }

    private void initListener() {
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsername.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    mUsername.setError("用户名不能为空");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mUsername.setError("密码不能为空");
                    return;
                }
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
                            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                            config.setDebuggerEnabled(true);
                            XMPPConnection connection = new XMPPConnection(config);  //根据配置连接
                            connection.connect();
                            connection.login(username, password);  //用户登陆
                            ToastUtils.showToastSafe(LoginActivity.this, "登陆成功");
                            //启动service
                            Intent service = new Intent(LoginActivity.this, ImService.class);
                            startService(service);
                            //跳转到MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            //将conn连接保存到静态变量中，实现共享
                            ImService.conn = connection;
                            //将用户名保存到静态变量中，实现共享
                            ImService.mCurrentAccount = username + "@localhost";
                            //销毁登陆界面
                            finish();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                            ToastUtils.showToastSafe(LoginActivity.this, "登陆失败");
                        }
                    }
                });
            }
        });
    }


}
