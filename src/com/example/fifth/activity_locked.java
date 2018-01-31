package com.example.fifth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class activity_locked extends Activity implements OnClickListener{
	private String MD5password;
	private PasswordInputer passwordInputer;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.layout_locked);
		
		MD5password = Settings.getMD5Password();
		
		passwordInputer = (PasswordInputer)findViewById(R.id.password_inputer);
		findViewById(R.id.submit_password).setOnClickListener(this);
	}
	@Override
	public void onBackPressed() {
		
	}
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.submit_password:
			String MD5inputString = MD5Util.MD5(passwordInputer.getInput());
			if(MD5inputString.equals(MD5password)){
				//onRestart触发的
				if(!getIntent().getBooleanExtra("isOnAppStart",true)){
					finish();
				}else{//启动app时
					//将password储存到静态变量中
					Settings.password = passwordInputer.getInput();
					
					startActivity(new Intent(this,activity_1.class));
					finish();
				}
			}else{
				new alert("密码错误！");
				passwordInputer.reset();
			}
			break;
		}
	}
}
