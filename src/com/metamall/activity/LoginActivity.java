package com.metamall.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.metamall.BroadcastReceiver.SMSBroadcastReceiver;
import com.metamall.R;
import com.metamall.Service.FileService;
import com.metamall.Clutter.TimeButton.TimeButton;

import java.util.Map;

/**
 + * 注册和登录Activity
 + *
 + */
public class LoginActivity extends Activity {

    private ImageButton ibBack;
    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;
    private FileService fileService;
    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private CheckBox btnPassYN;
    private static final String TAG = "LoginActivity";
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private LinearLayout linearLayout_password;
    private LinearLayout linearLayout_code;
    private TextView tvSwitch;
    private EditText etPhoneCode;
    private TimeButton v;
    private Integer count;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        v = (TimeButton) findViewById(R.id.getAgain_login);
        v.onCreate(savedInstanceState);
        v.setTextAfter("秒后重新获取").setTextBefore("点击获取验证码").setLenght(15 * 1000);
        v.setOnClickListener(v);

        linearLayout_password=(LinearLayout) findViewById(R.id.password_login_linear);
        linearLayout_code=(LinearLayout) findViewById(R.id.code_login_linear);
        linearLayout_password.setVisibility(View.VISIBLE);
        linearLayout_code.setVisibility(View.GONE);

        initView();
    }
    /**
     * 初始化视图
     */
    private void initView() {
        ibBack = (ImageButton) findViewById(R.id.login_ib_back);
        ibBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        MyWatcher watcher = new MyWatcher();
        etAccount = (EditText) findViewById(R.id.login_et_account);
        etAccount.addTextChangedListener(watcher);
        etPassword = (EditText) findViewById(R.id.login_et_password);
        etPassword.addTextChangedListener(watcher);

        btnPassYN=(CheckBox)findViewById(R.id.passYN);
        btnPassYN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (btnPassYN.isChecked()) {
                    //设置EditText的密码为可见的
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    //设置密码为隐藏的
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        etPhoneCode=(EditText) findViewById(R.id.login_num);
        etPhoneCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvSwitch=(TextView) findViewById(R.id.code_login);
        tvSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if(count%2==1){
                    tvSwitch.setText("密码登录");
                    linearLayout_code.setVisibility(View.VISIBLE);
                    linearLayout_password.setVisibility(View.GONE);
                } else{
                    tvSwitch.setText("验证码登录");
                    linearLayout_code.setVisibility(View.GONE);
                    linearLayout_password.setVisibility(View.VISIBLE);
                }
            }
        });
        btnLogin = (Button) findViewById(R.id.login_btn_login);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:登陆操作
                String accountNo = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                if(tvSwitch.getText().equals("密码登录")){
                    try {
                        Map<String,String> map = fileService.getUserInfo("private.txt");
                        etAccount.setText(map.get("username"));
                        etPassword.setText(map.get("password"));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fileService = new FileService(LoginActivity.this);
                    if(TextUtils.isEmpty(accountNo) || TextUtils.isEmpty(password)){
                        Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    else

                    {
                        Log.i(TAG, "保存用户名和密码，"+accountNo+":"+password);
                        try {
                            boolean result = fileService.saveToRom(password, accountNo, "private.txt");
                            if(result){
                                Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_SHORT).show();
                                Intent intent_login_my=new Intent();
                                intent_login_my.setClass(LoginActivity.this,MyActivity.class);
                                finish();
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                            }else{
                                Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                    }


                } else if(tvSwitch.getText().equals("验证码登录")){


                }





            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();
        //生成广播处理
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();

        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        //注册广播
        this.registerReceiver(mSMSBroadcastReceiver, intentFilter);

        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            @Override
            public void onReceived(String message) {

                etPassword.setText(message);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销短信监听广播
        this.unregisterReceiver(mSMSBroadcastReceiver);
    }



    /**
     +	 * 监听文本框
     +	 */
    class MyWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
            if ("".equals(etAccount.getText().toString())
                    || "".equals(etPassword.getText().toString())) {
                btnLogin.setEnabled(false);
            } else {
                btnLogin.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }
    }
}