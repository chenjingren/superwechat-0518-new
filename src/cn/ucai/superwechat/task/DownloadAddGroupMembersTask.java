package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.bean.Member;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by Administrator on 2016/5/31 0031.
 */
public class DownloadAddGroupMembersTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.getName();
    Context mContext;
    String hxid;
    String path;

    public DownloadAddGroupMembersTask(Context mContext, String hxid) {
        this.mContext = mContext;
        this.hxid = hxid;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_HX_ID, hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,
                responseDownloadAllGroupMembersTaskListener(),errorListener()));
    }

    private Response.Listener<Member[]> responseDownloadAllGroupMembersTaskListener() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] response) {
                Log.e(TAG,"responseDownloadAllGroupMembersTaskListener");
                if (response!=null){
                    Log.e(TAG,"DownloadGroupMembers,members size="+response.length);
                    //ArrayList<Group> groupList = SuperWeChatApplication.getInstance().getGroupList();
                    HashMap<String, ArrayList<Member>> groupMembers =
                            SuperWeChatApplication.getInstance().getGroupMembers();
                    ArrayList<Member> members = groupMembers.get(hxid);
                    ArrayList<Member> list = Utils.array2List(response);
                    if (members!=null){
                        members.clear();
                        members.addAll(list);
                    }else {
                        groupMembers.put(hxid,list);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_group_members_list"));
                }
            }
        };
    }
}
