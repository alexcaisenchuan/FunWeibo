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

import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.alex.common.OnHttpRequestReturnListener;
import com.alex.common.activities.BaseActivity;
import com.alex.common.activities.BaiduMapActivity;
import com.alex.common.activities.ImageLoadActivity;
import com.alex.common.utils.Misc;
import com.alex.common.utils.ShareUtils;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.alex.common.utils.StringUtils;
import com.alex.common.utils.WeiboUtils;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.model.Comment;
import com.weibo.sdk.android.model.Place;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.User;
import com.weibo.sdk.android.model.WeiboException;
import com.weibo.sdk.android.model.WeiboResponse;
import com.weibo.sdk.android.org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    
    ////////////////request code//////////////////////
    public static final int REQUEST_CODE_SEND_COMMENT = 1;
    ////////////////Activity启动参数///////////////////
    public static final String INTENT_EXTRA_WEIBO_MID = "weibo_mid";
    public static final String INTENT_EXTRA_WEIBO_TEXT = "weibo_text";
    public static final String INTENT_EXTRA_WEIBO_NICKNAME = "weibo_nickname";
    /**序列化方式传递一条微博的信息*/
    public static final String INTENT_EXTRA_WEIBO_STATUS_OBJ = "weibo_status_obj";
    /**评论内容*/
    public static final String INTENT_EXTRA_COMMENT_TEXT = null;
    
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
            mApp.getImageFetcher().loadFormCache(cmt.getUser().getProfileImageURL(), holder.mUserface);
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
                Status status = new Status(arg0);     //使用返回的json字符串构建微博对象
                sendMessageToBaseHandler(MSG_REFRESH_STATUS, 0, 0, status);
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
                List<Comment> commentList = Comment.constructComments(arg0);     //使用返回的json字符串构建评论列表
                int total = WeiboResponse.getTotalNum(arg0);
                sendMessageToBaseHandler(MSG_REFRESH_COMMENTS, total, 0, commentList);
            } catch (WeiboException e) {
                KLog.w(TAG, "WeiboException while build comment list", e);
                showToastOnUIThread(getString(R.string.hint_ret_error) + e.toString());
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException while build comment list", e);
                showToastOnUIThread(getString(R.string.hint_ret_error) + e.toString());
            }
        }
        
    }
    
    /**
     * 发送微博的回调函数
     * @author caisenchuan
     */
    private class SendCommentListener extends OnHttpRequestReturnListener {

        public SendCommentListener(BaseActivity base) {
            super(base);
        }

        @Override
        public void onComplete(String arg0) {
            getComment();       //读取评论
            showToastOnUIThread(R.string.hint_add_comment_success);
        }
        
    }
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**评论API*/
    private CommentsAPI mCommentAPI = null;
    /**评论列表的Adapter*/
    private CommentListAdapter mCommentAdapter = null;
    /**本条微博的mid*/
    private long mWeiboMid = -1L;
    /**全局微博对象*/
    private Status mStatus = null;
    /**全局评论列表*/
    private List<Comment> mCommentList = null;
    /**评论总数，我们不使用status中的数据，因为我们只看本应用的评论*/
    private int mCommentCount = 0;
    /**当前位置*/
    private Position mCurrentPosition = new Position();
    
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
    private TextView mWeiboMore = null;
    private LinearLayout mWeiboPicBorder = null;
    //底部控制栏
    private Button mButtonComment = null;
    private Button mButtonShare = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_comment: {
                //评论
                Intent it = new Intent(ActivityDetailWeibo.this, ActivityComment.class);
                startActivityForResult(it, REQUEST_CODE_SEND_COMMENT);
                break;
            }
            
            case R.id.button_share: {
                //分享
                String title = "";
                String text = "";
                if(mStatus != null) {
                    title = mStatus.getPlace().title;
                    text = mStatus.getText();
                }
                ShareUtils.share(ActivityDetailWeibo.this, title, text, null);
                break;
            }
        
            case R.id.img_map: {
                //打开地图
                Place p = mStatus.getPlace();
                if(p != null) {
                    BaiduMapActivity.openMapWithMarker(this, p.latitude, p.longtitude, p.title);
                }
                break;
            }
            
            case R.id.img_weibo_pic: {
                //查看微博大图
                if(mStatus != null) {
                    String url = mStatus.getOriginal_pic();
                    if(!TextUtils.isEmpty(url)) {
                        ImageLoadActivity.loadImage(this, url);
                    }
                }
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
        MenuItem add=menu.add(0, 0, 0, R.string.menu_checkin);
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //创建新微博
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
        
        //读取当前位置
        mCurrentPosition = mApp.getCurrentLocation();
        
        //设置ActionBar
        mActionBar.setTitle(R.string.title_hot_place);
        
        //绑定界面元素
        mListWeiboContent = (ListView)findViewById(R.id.list_weibo_content);
        mButtonComment = (Button)findViewById(R.id.button_comment);
        mButtonComment.setOnClickListener(this);
        mButtonShare = (Button)findViewById(R.id.button_share);
        mButtonShare.setOnClickListener(this);
        
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
        mWeiboPic.setOnClickListener(this);
        mWeiboPicBorder = (LinearLayout)mHeaderWeiboContent.findViewById(R.id.img_weibo_pic_border);
        mWeiboMore = (TextView)mHeaderWeiboContent.findViewById(R.id.text_more_info);
        
        //设置listview
        mListWeiboContent.addHeaderView(mHeaderWeiboUserInfo);
        mListWeiboContent.addHeaderView(mHeaderWeiboContent);
        mCommentAdapter = new CommentListAdapter(this);
        mListWeiboContent.setAdapter(mCommentAdapter);
        mListWeiboContent.setItemsCanFocus(true);
        
        //根据传入的信息设置初始界面
        Intent intent = getIntent();
        handleIntentExtra(intent);
        
        if(mToken != null) {
            if(mStatus == null) {
                //若微博内容为空，则读取微博信息
                if(mWeiboMid > 0L) {
                    StatusesAPI status = new StatusesAPI(mToken);
                    status.show(mWeiboMid, new GetStatusListener(this));
                }
            } else {
                //否则直接使用mStatus中的内容
                sendMessageToBaseHandler(MSG_REFRESH_STATUS);
            }
            
            //读取评论信息
            mCommentAPI = new CommentsAPI(mToken);
            if(mStatus != null && mStatus.getCommentsCount() > 0) {
                getComment();
            }
        } else {
            SmartToast.showShortToast(this, R.string.hint_auth_invalid, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch(requestCode) {
            case REQUEST_CODE_SEND_COMMENT: {
                if(resultCode == RESULT_OK && data != null) {
                    String comment = data.getStringExtra(INTENT_EXTRA_COMMENT_TEXT);
                    if(!TextUtils.isEmpty(comment)) {
                        //启动网络发出评论
                        if(mCommentAPI != null) {
                            mCommentAPI.create(comment,
                                               mWeiboMid,
                                               false,
                                               new SendCommentListener(ActivityDetailWeibo.this));
                        }
                    }
                }
                break;
            }
            
            default: {
                break;
            }
        }
    }
    
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_REFRESH_STATUS: {
                KLog.d(TAG, "MSG_REFRESH_STATUS , %s", msg.obj);
                Object obj = msg.obj;
                if(obj instanceof Status) {
                    //若传递Status过来，则使用其重置全局变量的数据
                    mStatus = (Status)obj;
                }
                if(mStatus != null) {
                    //设置用户信息
                    User user = mStatus.getUser();
                    if(user != null) {
                        //用户名
                        mUserName.setText(user.getName());
                        //用户头像
                        mApp.getImageFetcher().loadFormCache(user.getProfileImageURL(), mUserFace);
                    }
                    //设置微博信息
                    mWeiboTime.setText(StringUtils.getDateString(mStatus.getCreatedAt()));
                    Place p = mStatus.getPlace();
                    if(p != null) {
                        if(mCurrentPosition.isValid()) {
                            int d = (int)Misc.getDistance(p.latitude,
                                                          p.longtitude, 
                                                          mCurrentPosition.getLat(),
                                                          mCurrentPosition.getLon());
                            String distance = String.format("%s%s", d, getString(R.string.text_m));
                            mWeiboSource.setText(distance);
                        } else {
                            mWeiboSource.setText("");
                        }
                        mActionBar.setTitle(p.title);
                    }
                    //设置微博文字
                    mWeiboContent.setText(mStatus.getText());
                    //设置微博转发等信息
                    setMoreInfoDisplay();
                    //微博配图
                    String pic_url = WeiboUtils.getStatusPicUrlByNetworkStatus(this, mStatus, mApp.getFileCache());
                    if(!TextUtils.isEmpty(pic_url)) {
                        mWeiboPic.setVisibility(View.VISIBLE);
                        mWeiboPicBorder.setVisibility(View.VISIBLE);
                        mApp.getImageFetcher().loadFormCache(pic_url, mWeiboPic);
                    } else {
                        mWeiboPic.setVisibility(View.GONE);
                        mWeiboPicBorder.setVisibility(View.GONE);
                    }
                }
                break;
            }
                
            case MSG_REFRESH_COMMENTS: {
                KLog.d(TAG, "MSG_REFRESH_COMMENTS");
                //评论数
                if(msg.arg1 >= 0) {
                    mCommentCount = msg.arg1;
                }
                setMoreInfoDisplay();
                //评论列表
                Object obj = msg.obj;
                if(obj instanceof List<?>) {
                    mCommentList = (List<Comment>)obj;
                    if(mCommentAdapter != null) {
                        mCommentAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            
            default: {
                KLog.w(TAG, "Unknown msg : " + msg.what);
                super.handleBaseMessage(msg);
                break;
            }
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
    
    /**
     * 读取评论
     */
    private void getComment() {
        if(mCommentAPI != null) {
            mCommentAPI.show(mWeiboMid,
                             0L,
                             0L,
                             100,
                             1,
                             AUTHOR_FILTER.ALL,
                             1,
                             new GetCommentsListener(this));
        }
    }
    
    /**
     * 设置转发数及赞的个数的显示
     */
    private void setMoreInfoDisplay() {
        long like_num = 0;
        if(mStatus != null) {
            like_num = mStatus.getAttitudesCount();
        }
        String more_info = String.format(getString(R.string.text_comment_like_num),
                                         like_num,
                                         mCommentCount);
        mWeiboMore.setText(more_info);
    }
}
