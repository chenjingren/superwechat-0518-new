/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.ImageUtils;
import cn.ucai.superwechat.utils.Utils;

import com.easemob.exceptions.EaseMobException;

import java.io.File;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
	static final String TAG = RegisterActivity.class.getName();

	Activity mContext;

	private EditText userNameEditText;
	private EditText userNickEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;

	ImageView ivAvatar;

	OnSetAvatarListener mOnSetAvatarlistener;
	private String mAvatarName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext = this;
		initView();
		setListener();
	}

	private void initView() {
		userNameEditText = (EditText) findViewById(R.id.etUserName);
		userNickEditText = (EditText) findViewById(R.id.etNick);
		passwordEditText = (EditText) findViewById(R.id.etPassword);
		confirmPwdEditText = (EditText) findViewById(R.id.et_confirm_password);

		ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
	}

	private void setListener() {
		setOnRegisterListener();
		setOnLoginListener();
		onSetAvatarListener();
	}

	private void onSetAvatarListener() {
		findViewById(R.id.layout_avatar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOnSetAvatarlistener = new OnSetAvatarListener(mContext,R.id.layout_register,getAvatarName(), I.AVATAR_TYPE_USER_PATH);
			}
		});
	}

	private String getAvatarName() {
		mAvatarName = System.currentTimeMillis()+"";
		return mAvatarName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_OK){
			mOnSetAvatarlistener.setAvatar(requestCode,data,ivAvatar);
		}
	}

	private void setOnLoginListener() {
		findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//finish();
				back(v);
			}
		});
	}

	String username;
	String nick;
	String pwd;
	//String avatar;
	ProgressDialog pd;
	private void setOnRegisterListener() {
		findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				username = userNameEditText.getText().toString().trim();
				nick = userNickEditText.getText().toString().trim();
				pwd = passwordEditText.getText().toString().trim();
				String confirm_pwd = confirmPwdEditText.getText().toString().trim();
				//avatar = getAvatarName();
				if (TextUtils.isEmpty(username)) {
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_empty));
					return;
				}else if (!username.matches("[\\w][\\w\\d_]+")){
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
					return;
				}else if (TextUtils.isEmpty(nick)){
					userNickEditText.requestFocus();
					userNickEditText.setError(getResources().getString(R.string.Nick_name_cannot_be_empty));
				} else if (TextUtils.isEmpty(pwd)) {
					passwordEditText.requestFocus();
					passwordEditText.setError(getResources().getString(R.string.Password_cannot_be_empty));
					return;
				} else if (TextUtils.isEmpty(confirm_pwd)) {
					confirmPwdEditText.requestFocus();
					confirmPwdEditText.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
					return;
				} else if (!pwd.equals(confirm_pwd)) {
					confirmPwdEditText.requestFocus();
					confirmPwdEditText.setError(getResources().getString(R.string.Two_input_password));
					//Toast.makeText(mContext, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
					return;
				}

				if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
					pd = new ProgressDialog(mContext);
					pd.setMessage(getResources().getString(R.string.Is_the_registered));
					pd.show();
					registerAppAvatar();
				}
			}
		});
	}

	private void registerAppAvatar(){
		File file = new File(ImageUtils.getAvatarPath(mContext,I.AVATAR_TYPE_USER_PATH),
				mAvatarName+I.AVATAR_SUFFIX_JPG);
		OkHttpUtils<Message> utils = new OkHttpUtils<>();
		utils.url(SuperWeChatApplication.SERVICE_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_REGISTER)
				.addParam(I.User.USER_NAME,username)
				.addParam(I.User.NICK,nick)
				.addParam(I.User.PASSWORD,pwd)
				.addFile(file)
				.targetClass(Message.class)
				.execute(new OkHttpUtils.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (result.isResult()){
							EMServerRegister();
						}else {
							pd.dismiss();
							Utils.showToast(mContext,Utils.getResourceString(mContext,result.getMsg()),
									Toast.LENGTH_LONG);
							Log.e(TAG, "register fail,error:" + result.getMsg());
						}
					}

					@Override
					public void onError(String error) {
						pd.dismiss();
						Utils.showToast(mContext,error,Toast.LENGTH_LONG);
						Log.i(TAG,"register fail error : " +error);
					}
				});
	}

	private void EMServerRegister(){
		new Thread(new Runnable() {
			public void run() {
				try {
					// 调用sdk注册方法
					EMChatManager.getInstance().createAccountOnServer(username, pwd);
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							// 保存用户名
							SuperWeChatApplication.getInstance().setUserName(username);
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
							finish();
						}
					});
				} catch (final EaseMobException e) {
					
					unregisterAppAvatar();

					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							int errorCode=e.getErrorCode();
							if(errorCode==EMError.NONETWORK_ERROR){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.USER_ALREADY_EXISTS){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.UNAUTHORIZED){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.ILLEGAL_USER_NAME){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}).start();
	}
//http://10.0.2.2:8080/SuperWeChatServer/Server?request=unregister&m_user_name=
	private void unregisterAppAvatar() {
		OkHttpUtils<Message> utils = new OkHttpUtils<>();
		utils.url(SuperWeChatApplication.SERVICE_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_UNREGISTER)
				.addParam(I.User.USER_NAME,username)
				.targetClass(Message.class)
				.execute(new OkHttpUtils.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						pd.dismiss();
						Utils.showToast(mContext,R.string.Registration_failed,Toast.LENGTH_SHORT);
					}

					@Override
					public void onError(String error) {
						pd.dismiss();
						Log.e(TAG, error);
					}
				});
	}

	public void back(View view) {
		finish();
	}
}
