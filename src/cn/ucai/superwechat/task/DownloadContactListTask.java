package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by Administrator on 2016/5/23 0023.
 */
public class DownloadContactListTask extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    Context mContext;
    String userName;
    String path;

    public DownloadContactListTask(Context mContext, String userName) {
        this.mContext = mContext;
        this.userName = userName;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME, userName)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
            Log.e(TAG,"path :"+path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Contact[]>(path,Contact[].class,
                responseDownloadContactListTaskListener(),errorListener()));
    }

    private Response.Listener<Contact[]> responseDownloadContactListTaskListener() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] response) {
                Log.e(TAG,"DownloadContactList");
                if (response!=null){
                    Log.e(TAG,"DownloadContactList contact" + response.length);
                    ArrayList<Contact> contactList = SuperWeChatApplication.getInstance().getContactList();
                    ArrayList<Contact> list = Utils.array2List(response);
                    contactList.clear();
                    contactList.addAll(list);

                    HashMap<String, Contact> userList = SuperWeChatApplication.getInstance().getUserList();
                    userList.clear();
                    for (Contact c:list){
                        userList.put(c.getMContactCname(),c);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_contact_list"));
                }
            }
        };
    }


}
