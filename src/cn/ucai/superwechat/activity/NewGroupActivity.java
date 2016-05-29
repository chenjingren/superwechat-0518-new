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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.data.MultipartRequest;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.ImageUtils;
import cn.ucai.superwechat.utils.Utils;

import com.easemob.exceptions.EaseMobException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class NewGroupActivity extends BaseActivity {

	Activity mContext;

	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer,mllNewGroup;

	private RelativeLayout mrlgroupAvatar;

	private ImageView mivGroupAvatar;

	private EditText metGroupAvatarPath;

	OnSetAvatarListener mOnSetAvatarListener;

	private static final int CREATE_NEW_GROUP = 100;

    public static final String TAG = NewGroupActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);

		initView();
		setListener();
		registerGetDataReceiver();
	}

	@Override
	protected void onDestroy() {
		if (mGetDataReceiver!=null){
			unregisterReceiver(mGetDataReceiver);
		}
		super.onDestroy();
	}

	private void initView() {

		mContext = this;

		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);

		mllNewGroup = (LinearLayout) findViewById(R.id.layout_new_group);

		mrlgroupAvatar = (RelativeLayout) findViewById(R.id.layout_group_avatar);

		mivGroupAvatar = (ImageView) findViewById(R.id.iv_group_avatar);

		metGroupAvatarPath = (EditText) findViewById(R.id.et_group_avatar_path);
	}

	private void setListener() {
		setOnCheckedChangeListener();
		setSaveGroupClickListener();
		setGroupIconClickListener();
	}

	private void setGroupIconClickListener() {
		findViewById(R.id.layout_group_avatar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOnSetAvatarListener = new OnSetAvatarListener(mContext,R.id.layout_new_group,
						getAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
			}
		});
	}

	String avatarName;
	private String getAvatarName(){
		avatarName = System.currentTimeMillis()+"";
		return avatarName;
	}

	private void setSaveGroupClickListener() {
		findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(mContext, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
					startActivityForResult(new Intent(mContext,
                            GroupPickContactsActivity.class), CREATE_NEW_GROUP);
				}
			}
		});
	}

	private void setOnCheckedChangeListener() {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					openInviteContainer.setVisibility(View.INVISIBLE);
				}else{
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void setProgressDialog(){
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(st1);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
        }

        if (requestCode==CREATE_NEW_GROUP){
            createNewGroup();
			/*String newMembersNames = data.getStringExtra("newMembersName");
			String newMembersIds = data.getStringExtra("newMembersId");
			Log.e(TAG,"membersName:"+newMembersNames+"   "+"membersId:"+newMembersIds);
       */ }else {
            mOnSetAvatarListener.setAvatar(requestCode,data,mivGroupAvatar);
        }
	}

	private void createNewGroup() {
        //新建群组
        setProgressDialog();
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// 调用sdk创建群组方法
				String groupName = groupNameEditText.getText().toString().trim();
				String desc = introductionEditText.getText().toString();

				String[] members = newMembersNames.split(",");
                EMGroup emGroup;
				try {
					if(checkBox.isChecked()){
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
                        emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
					}else{
						//创建不公开群
                        emGroup = EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
					}

                    String hxid = emGroup.getGroupId();
                    createServerNewGroup(hxid,groupName,desc);

				} catch (final EaseMobException e) {

					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
	}

	public void back(View view) {
		finish();
	}


	public void createServerNewGroup(final String hxid, String groupName, String description){
        User user = SuperWeChatApplication.getInstance().getUser();
        boolean isPublic = checkBox.isChecked();
        boolean isInvite =  memberCheckbox.isChecked();

        File file = new File(ImageUtils.getAvatarPath(mContext,I.AVATAR_TYPE_GROUP_PATH),
                avatarName+I.AVATAR_SUFFIX_JPG);
        OkHttpUtils<Group> utils = new OkHttpUtils<>();
        utils.url(SuperWeChatApplication.SERVICE_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.HX_ID,hxid)
                .addParam(I.Group.NAME,groupName)
                .addParam(I.Group.DESCRIPTION,description)
                .addParam(I.Group.OWNER,user.getMUserName())
                .addParam(I.Group.IS_PUBLIC,isPublic+"")
                .addParam(I.Group.ALLOW_INVITES,isInvite+"")
                .addParam(I.User.USER_ID,user.getMUserId()+"")
                .addFile(file)
                .targetClass(Group.class)
                .execute(new OkHttpUtils.OnCompleteListener<Group>() {
                    @Override
                    public void onSuccess(Group group) {

                        if (group.isResult()){
                            if (newMembersNames!=null){
                                addGroupMembers(group,newMembersNames,newMembersIds);
                            }else {
                                SuperWeChatApplication.getInstance().getGroupList().add(group);
                                Intent intent = new Intent("update_group_list").putExtra("group",group);
                                setResult(RESULT_OK,intent);
                                progressDialog.dismiss();
								Toast.makeText(mContext,R.string.Create_groups_Success,Toast.LENGTH_LONG).show();
                               /* Utils.showToast(mContext,
                                        Utils.getResourceString(mContext,R.string.Create_groups_Success),Toast.LENGTH_LONG);*/
                                finish();
                            }
                        }else {
                            progressDialog.dismiss();
                            Utils.showToast(mContext,
                                    Utils.getResourceString(mContext,group.getMsg()),Toast.LENGTH_LONG);
                            Log.i(TAG,Utils.getResourceString(mContext,group.getMsg()));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(NewGroupActivity.this,R.string.Failed_to_create_groups, Toast.LENGTH_SHORT).show();
                        Log.e(TAG,error);
                    }
                });
	}

    public void addGroupMembers(Group group,String membersName,String membersId){
        try {
            String path = new ApiParams()
                    .with(I.Member.USER_ID,membersId)
                    .with(I.Member.USER_NAME,membersName)
                    .with(I.Member.GROUP_HX_ID,group.getMGroupHxid())
                    .getRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS);

            executeRequest(new GsonRequest<Message>(path,Message.class,
                            responseAddGroupMembersListener(group),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<Message> responseAddGroupMembersListener(final Group group) {
        return new Response.Listener<Message>() {
            @Override
            public void onResponse(Message message) {
                if (message.isResult()) {
					progressDialog.dismiss();
				/*	Utils.showToast(mContext,
							Utils.getResourceString(mContext, group.getMsg()), Toast.LENGTH_LONG);*/
					SuperWeChatApplication.getInstance().getGroupList().add(group);
                    Intent intent = new Intent("update_group_list").putExtra("group",group);
                    setResult(RESULT_OK,intent);
				}else {
                    progressDialog.dismiss();
                    Utils.showToast(mContext,
                            Utils.getResourceString(mContext,R.string.Failed_to_create_groups),Toast.LENGTH_LONG);
                }
                finish();
            }
        };
    }


	String newMembersIds;
	String newMembersNames;
	class GetDataReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			 newMembersIds = intent.getStringExtra("newMembersId");
			 newMembersNames = intent.getStringExtra("newMembersName");
			 Log.e(TAG,"newMembersIds :" +newMembersIds+",newMembersNames :"+newMembersNames);
		}
	}
	GetDataReceiver mGetDataReceiver;
	public void registerGetDataReceiver(){
		mGetDataReceiver = new GetDataReceiver();
		IntentFilter filter = new IntentFilter("get_data_from_GroupPickContactActivity");
		registerReceiver(mGetDataReceiver,filter);
	}
}
