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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMContactManager;

import java.util.HashMap;

import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.UserUtils;

public class AddContactActivity extends BaseActivity{
	Context mContext;

	private EditText editText;
	private LinearLayout searchedUserLayout;
	private TextView nameText,mTextView;

	private NetworkImageView avatar;

	private InputMethodManager inputMethodManager;
	private String toAddUsername;
	private ProgressDialog progressDialog;

	TextView tvNoResult;

	public static final String TAG = AddContactActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		initView();
		setListener();
	}

	private void initView() {

		mContext = this;

		mTextView = (TextView) findViewById(R.id.add_list_friends);
		String strAdd = getResources().getString(R.string.add_friend);
		mTextView.setText(strAdd);

		editText = (EditText) findViewById(R.id.edit_note);
		String strUserName = getResources().getString(R.string.user_name);
		editText.setHint(strUserName);

		searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);

		avatar = (NetworkImageView) findViewById(R.id.avatar);

		tvNoResult = (TextView) findViewById(R.id.tv_no_result);

		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void setListener() {
		setSearchUserListener();
		setAddContactListener();
	}

	private void setSearchUserListener() {
		findViewById(R.id.btn_search_user).setOnClickListener(new View.OnClickListener() {
			/**
			 * 查找contact
			 * @param v
			 */
			@Override
			public void onClick(View v) {
				final String name = editText.getText().toString().trim();
				//String saveText = searchBtn.getText().toString();

				if(TextUtils.isEmpty(name)) {
					String st = getResources().getString(R.string.Please_enter_a_username);
					startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", st));
					return;
				}
				if(SuperWeChatApplication.getInstance().getUserName().equals(name.trim())){
					String str = getString(R.string.not_add_myself);
					startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", str));
					return;
				}
				/*if((SuperWeChatApplication.getInstance()).getContactList().contains(nameText.getText().toString())){
			//提示已在好友列表中，无需添加
			if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
				startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
				return;
			}
					String strin = getString(R.string.This_user_is_already_your_friend);
					startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", strin));
					return;
				}*/
				toAddUsername = name;
				// TODO 从服务器获取此contact,如果不存在提示不存在此用户
				try {
					String path = new ApiParams()
							.with(I.User.USER_NAME,toAddUsername)
							.getRequestUrl(I.REQUEST_FIND_USER);
					executeRequest(new GsonRequest<User>(path,User.class,
							responseFindUserListener(),errorListener()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void setAddContactListener() {
		findViewById(R.id.btn_add_contact).setOnClickListener(new View.OnClickListener() {
			/**
			 *  添加contact按钮点击事件
			 */
			@Override
			public void onClick(View v) {
				/*if(((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().containsKey(nameText.getText().toString())){
					//提示已在好友列表中，无需添加
					if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
						startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
						return;
					}
					String strin = getString(R.string.This_user_is_already_your_friend);
					startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", strin));
					return;
				}*/
				progressDialog = new ProgressDialog(mContext);
				String stri = getResources().getString(R.string.Is_sending_a_request);
				progressDialog.setMessage(stri);
				progressDialog.setCanceledOnTouchOutside(false);
				progressDialog.show();

				new Thread(new Runnable() {
					public void run() {

						try {
							//demo写死了个reason，实际应该让用户手动填入
							String s = getResources().getString(R.string.Add_a_friend);
							EMContactManager.getInstance().addContact(toAddUsername, s);
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.dismiss();
									String s1 = getResources().getString(R.string.send_successful);
									Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
								}
							});
						} catch (final Exception e) {
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.dismiss();
									String s2 = getResources().getString(R.string.Request_add_buddy_failure);
									Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}).start();
			}
		});
	}

	private Response.Listener<User> responseFindUserListener() {
		return new Response.Listener<User>() {
			@Override
			public void onResponse(User user) {
				if (user!=null){
					HashMap<String, Contact> userList =
							SuperWeChatApplication.getInstance().getUserList();
					if (userList.containsKey(user.getMUserName())){
						startActivity(new Intent(mContext,
								UserProfileActivity.class).putExtra("username",user.getMUserName()));
					}else {
						Log.i(TAG,"user :" +user.toString());
						searchedUserLayout.setVisibility(View.VISIBLE);
						UserUtils.setUserBeanAvatar(user,avatar);
						UserUtils.setUserBeanNick(user,nameText);
					}
					//服务器存在此用户，显示此用户的头像和昵称及添加按钮
					//searchedUserLayout.setVisibility(View.VISIBLE);
					tvNoResult.setVisibility(View.GONE);
					Log.i(TAG,"user :" +user.toString());
					//nameText.setText(toAddUsername);
					//UserUtils.setUserBeanAvatar(user,avatar);
					//UserUtils.setUserBeanNick(user,nameText);
				}else {
					searchedUserLayout.setVisibility(View.GONE);
					tvNoResult.setVisibility(View.VISIBLE);
				}
			}
		};
	}

	public void back(View v) {
		finish();
	}
}
