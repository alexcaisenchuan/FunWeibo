/**
 * <p>Title: ActivityDetailWeibo.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.funweibo.activities;

import java.util.List;

import com.alex.common.BaseActivity;
import com.alex.funweibo.R;
import com.alex.common.utils.OnHttpRequestReturnListener;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.alex.common.utils.StringUtils;
import com.ta.util.bitmap.TABitmapCacheWork;
import com.ta.util.bitmap.TABitmapCallBackHanlder;
import com.ta.util.bitmap.TADownloadBitmapHandler;
import com.ta.util.extend.draw.DensityUtils;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.model.Comment;
import com.weibo.sdk.android.model.Place;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.User;
import com.weibo.sdk.android.model.WeiboException;
import com.weibo.sdk.android.org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author caisenchuan
 *
 */
public class ActivityDetailWeibo extends BaseActivity implements OnClickListener{
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityDetailWeibo";
    
    ////////////////Activity启动参数///////////////////
    public static final String INTENT_EXTRA_WEIBO_MID = "weibo_mid";
    public static final String INTENT_EXTRA_WEIBO_TEXT = "weibo_text";
    public static final String INTENT_EXTRA_WEIBO_NICKNAME = "weibo_nickname";
    /**序列化方式传递一条微博的信息*/
    public static final String INTENT_EXTRA_WEIBO_STATUS_OBJ = "weibo_status_obj";
    
    ///////////////mBaseHandler msg what//////////////
    /**刷新微博内容*/
    public static final int MSG_REFRESH_STATUS = MSG_EXTEND_BASE + 1;
    /**刷新评论内容*/
    public static final int MSG_REFRESH_COMMENTS = MSG_EXTEND_BASE + 2;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 一条评论的ViewHolder
     */
    public class CommentListItemViewHolder {
        /**用户名*/
        public TextView mUsername;
        /**用户评论*/
        public TextView mComment;
        /**用户大头贴*/
        public ImageView mUserface;
    }
    
    /**
     * 评论列表Adapter
     * @author caisenchuan
     */
    public class CommentListAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        
        /**
         * 构造函数
         */
        public CommentListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            if(mCommentList != null) {
                return mCommentList.size();
            } else {
                return 0;
            }
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommentListItemViewHolder holder = null;
            
            if(convertView == null) {
                holder = new CommentListItemViewHolder();
                
                convertView = mInflater.inflate(R.layout.list_item_weibo_comment, null);
                holder.mUsername = (TextView)convertView.findViewById(R.id.text_username);
                holder.mComment = (TextView)convertView.findViewById(R.id.text_comment);
                holder.mUserface = (ImageView)convertView.findViewById(R.id.img_userface);
                convertView.setTag(holder);
            } else {
                holder = (CommentListItemViewHolder)convertView.getTag();
            }
            
            //设置评论内容
            Comment cmt = getComment(position);
            //用户头像
            mImageFetcher.loadFormCache(cmt.getUser().getProfileImageURL(), holder.mUserface);
            if(cmt != null) {
                holder.mComment.setText(cmt.getText());         //评论内容
                User user = cmt.getUser();
                if(user != null) {
                    holder.mUsername.setText(user.getName());   //用户名
                }
            }
            
            return convertView;
        }
        
        /**
         * 读取某条评论
         * @param position
         * @return
         * @author caisenchuan
         */
        private Comment getComment(int position) {
            Comment ret = null;
            
            if(mCommentList != null) {
                if(position >= 0 && position < mCommentList.size()) {
                    ret = mCommentList.get(position);
                }
            }
            
            return ret;
        }
    }
    
    /**
     * 读取微博信息的回调函数
     * @author caisenchuan
     */
    private class GetStatusListener extends OnHttpRequestReturnListener {

        /**
         * 读取微博信息的回调函数
         * @param base 用于显示Toast的Activity对象
         */
        public GetStatusListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                mStatus = new Status(arg0);     //使用返回的json字符串构建微博对象
                sendMessageToBaseHandler(MSG_REFRESH_STATUS);
            } catch (WeiboException e) {
                KLog.w(TAG, "WeiboException while build status", e);
                showToastOnUIThread(getString(R.string.hint_ret_error) + e.toString());
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException while build status", e);
                showToastOnUIThread(getString(R.string.hint_json_parse_faild));
            }
        }
        
    }
    
    /**
     * 读取微博评论的回调函数
     * @author caisenchuan
     */
    private class GetCommentsListener extends OnHttpRequestReturnListener {

        /**
         * @param base
         */
        public GetCommentsListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                mCommentList = Comment.constructComments(arg0);     //使用返回的json字符串构建评论列表
                sendMessageToBaseHandler(MSG_REFRESH_COMMENTS);
            } catch (WeiboException e) {
                KLog.w(TAG, "WeiboException while build comment list", e);
                showToastOnUIThread(getString(R.string.hint_ret_error) + e.toString());
            }
        }
        
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**评论列表的Adapter*/
    private CommentListAdapter mCommentAdapter = null;
    /**本条微博的mid*/
    private long mWeiboMid = -1L;
    /**全局微博对象*/
    private Status mStatus = null;
    /**全局评论列表*/
    private List<Comment> mCommentList = null;
    
    //////////////界面元素/////////////////
    //listview总体
    private ListView mListWeiboContent = null;
    //listview第一行：用户信息
    private View mHeaderWeiboUserInfo = null;
    private TextView mUserName = null;
    private ImageView mUserFace = null;
    private TextView mWeiboTime = null;
    private TextView mWeiboSource = null;
    private ImageView mImgMap = null;
    //listview第二行：微博信息
    private View mHeaderWeiboContent = null;
    private TextView mWeiboContent = null;
    private ImageView mWeiboPic = null;
    private LinearLayout mWeiboPicBorder = null;
    //图片缓存加载器
    private TABitmapCacheWork mImageFetcher = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.img_map: {
                //打开地图
                break;
            }
                
            default:
                break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add=menu.add(0,0,0,"签到");
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //拍照并创建新微博
                checkinWithCurrentPoi();
                return false;
            }
        });
        return true; 
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see com.alex.wemap.activities.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_weibo);
        
        //设置ActionBar
        mActionBar.setTitle(R.string.text_hot_place);
        
        //绑定界面元素
        mListWeiboContent = (ListView)findViewById(R.id.list_weibo_content);
        
        mHeaderWeiboUserInfo = View.inflate(this, R.layout.header_weibo_userinfo, null);
        mUserName = (TextView)mHeaderWeiboUserInfo.findViewById(R.id.text_username);
        mUserFace = (ImageView)mHeaderWeiboUserInfo.findViewById(R.id.img_userface);
        mWeiboTime = (TextView)mHeaderWeiboUserInfo.findViewById(R.id.text_weibo_time);
        mWeiboSource = (TextView)mHeaderWeiboUserInfo.findViewById(R.id.text_weibo_source);
        mImgMap = (ImageView)mHeaderWeiboUserInfo.findViewById(R.id.img_map);
        mImgMap.setOnClickListener(this);
        
        mHeaderWeiboContent = View.inflate(this, R.layout.header_weibo_content, null);
        mWeiboContent = (TextView)mHeaderWeiboContent.findViewById(R.id.text_weibo_content);
        mWeiboPic = (ImageView)mHeaderWeiboContent.findViewById(R.id.img_weibo_pic);
        mWeiboPicBorder = (LinearLayout)mHeaderWeiboContent.findViewById(R.id.img_weibo_pic_border);
        
        //图片缓存相关
        TADownloadBitmapHandler downloadBitmapFetcher = new TADownloadBitmapHandler(
                this, DensityUtils.dipTopx(this, 128),
                DensityUtils.dipTopx(this, 128));
        TABitmapCallBackHanlder taBitmapCallBackHanlder = new TABitmapCallBackHanlder();
        taBitmapCallBackHanlder.setLoadingImage(this, R.drawable.empty_photo);
        mImageFetcher = new TABitmapCacheWork(this);
        mImageFetcher.setProcessDataHandler(downloadBitmapFetcher);
        mImageFetcher.setCallBackHandler(taBitmapCallBackHanlder);
        mImageFetcher.setFileCache(mApp.getFileCache());
        
        //设置listview
        mListWeiboContent.addHeaderView(mHeaderWeiboUserInfo);
        mListWeiboContent.addHeaderView(mHeaderWeiboContent);
        mCommentAdapter = new CommentListAdapter(this);
        mListWeiboContent.setAdapter(mCommentAdapter);
        mListWeiboContent.setItemsCanFocus(true);
        
        //根据传入的信息设置初始界面
        Intent intent = getIntent();
        handleIntentExtra(intent);
        
        Oauth2AccessToken token = mApp.getAccessToken();
        if(token != null) {
            if(mStatus == null) {
                //若微博内容为空，则读取微博信息
                if(mWeiboMid > 0L) {
                    StatusesAPI status = new StatusesAPI(token);
                    status.show(mWeiboMid, new GetStatusListener(this));
                }
            } else {
                //否则直接使用mStatus中的内容
                sendMessageToBaseHandler(MSG_REFRESH_STATUS);
            }
            
            //读取评论信息
            CommentsAPI comment = new CommentsAPI(token);
            comment.show(mWeiboMid, 0L, 0L, 50, 1, AUTHOR_FILTER.ALL, new GetCommentsListener(this));
        } else {
            if(token == null) {
                SmartToast.showShortToast(this, R.string.hint_auth_invalid, false);
            }
            if(mWeiboMid <= 0L) {
                SmartToast.showShortToast(this, R.string.hint_weibo_param_invalid, false);
            }
        }
    }

    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_REFRESH_STATUS:
                if(mStatus != null) {
                    //设置用户信息
                    User user = mStatus.getUser();
                    if(user != null) {
                        //用户名
                        mUserName.setText(user.getName());
                        //用户头像
                        mImageFetcher.loadFormCache(user.getProfileImageURL(), mUserFace);
                    }
                    //设置微博信息
                    mWeiboTime.setText(StringUtils.getDateString(mStatus.getCreatedAt()));
                    Place p = mStatus.getPlace();
                    if(p != null) {
                        mWeiboSource.setText(p.title);
                        mActionBar.setTitle(p.title);
                    }
                    mWeiboContent.setText(mStatus.getText());
                    //微博配图
                    String pic_url = mStatus.getBmiddle_pic();
                    if(!TextUtils.isEmpty(pic_url)) {
                        mWeiboPic.setVisibility(View.VISIBLE);
                        mWeiboPicBorder.setVisibility(View.VISIBLE);
                        mImageFetcher.loadFormCache(pic_url, mWeiboPic);
                    } else {
                        mWeiboPic.setVisibility(View.GONE);
                        mWeiboPicBorder.setVisibility(View.GONE);
                    }
                }
                break;
                
            case MSG_REFRESH_COMMENTS:
                if(mCommentAdapter != null) {
                    mCommentAdapter.notifyDataSetChanged();
                }
                break;
            
            default:
                KLog.w(TAG, "Unknown msg : " + msg.what);
                super.handleBaseMessage(msg);
                break;
        }
    };
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 处理创建Activity时传入的信息
     */
    private void handleIntentExtra(Intent it) {
        Object obj = it.getSerializableExtra(INTENT_EXTRA_WEIBO_STATUS_OBJ);
        if(obj instanceof Status) {
            KLog.d(TAG, "handleIntentExtra, get status");
            
            mStatus = (Status)obj;
            
            try {
                mWeiboMid = Long.valueOf(mStatus.getMid());
            } catch (Exception e) {
                KLog.w(TAG, "Exception", e);
            }
        } else {
            KLog.d(TAG, "handleIntentExtra, request status");
            
            String nickname = it.getStringExtra(INTENT_EXTRA_WEIBO_NICKNAME);
            mUserName.setText(nickname);
            
            String weiboContent = it.getStringExtra(INTENT_EXTRA_WEIBO_TEXT);
            mWeiboContent.setText(weiboContent);
            
            mWeiboMid = it.getLongExtra(INTENT_EXTRA_WEIBO_MID, -1L);
        }
    }
    
    /**
     * 使用当前地点签到
     */
    private void checkinWithCurrentPoi() {
        Intent it = new Intent(ActivityDetailWeibo.this, ActivityNewWeibo.class);
        if(mStatus != null) {
            Place p = mStatus.getPlace();
            if(p != null) {
                it.putExtra(ActivityNewWeibo.INTENT_EXTRA_POI_ID, p.poiid);
                it.putExtra(ActivityNewWeibo.INTENT_EXTRA_POI_TITLE, p.title);
            }
        }
        startActivity(it);
    }
}
