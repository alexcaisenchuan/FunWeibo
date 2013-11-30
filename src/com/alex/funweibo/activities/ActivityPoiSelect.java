package com.alex.funweibo.activities;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alex.common.utils.KLog;
import com.alex.funweibo.R;
import com.weibo.sdk.android.model.Poi;

public class ActivityPoiSelect extends BasePOIActivity implements OnScrollListener{

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = ActivityPoiSelect.class.getSimpleName();
    
    /**poi id*/
    public static final String INTENT_EXTRA_POI_ID      = "poi_id";
    /**poi名字*/
    public static final String INTENT_EXTRA_POI_TITLE   = "poi_title";
    
    /**刷新列表内容*/
    public static final int MSG_REFRESH_LIST = MSG_POI_ACTIVITY_BASE + 1;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 评论列表Adapter
     * @author caisenchuan
     */
    public class ListAdapter extends BaseAdapter {
        /**
         * 一个条目的ViewHolder
         */
        private class ListItemViewHolder {
            /**标题*/
            public TextView mTitle;
            /**内容*/
            public TextView mAddress;
            /**图片*/
            public ImageView mPic;
            /**签到人数*/
            public TextView mCheckoutNum;
        }
        
        /**
         * inflater
         */
        private LayoutInflater mInflater = null;
        
        /**
         * 构造函数
         */
        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            if(mPoiList != null) {
                return mPoiList.size();
            } else {
                return 0;
            }
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return null;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemViewHolder holder = null;
            
            if(convertView == null) {
                holder = new ListItemViewHolder();
                convertView = mInflater.inflate(R.layout.list_item_poi, null);
                holder.mTitle = (TextView)convertView.findViewById(R.id.poi_title);
                holder.mAddress = (TextView)convertView.findViewById(R.id.poi_content);
                holder.mPic = (ImageView)convertView.findViewById(R.id.poi_pic);
                holder.mCheckoutNum = (TextView)convertView.findViewById(R.id.poi_checkout_num);
                convertView.setTag(holder);
            } else {
                holder = (ListItemViewHolder)convertView.getTag();
            }
            
            //设置条目内容
            Poi poi = getPosItem(position);
            //图片
            
            //其他信息
            if(poi != null) {
                holder.mTitle.setText(poi.title);
                holder.mAddress.setText(poi.address);
                holder.mCheckoutNum.setText(String.format(getString(R.string.text_checkin_num), poi.checkin_num));
            }
            
            return convertView;
        }
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**Poi列表视图*/
    private ListView mPoiListView = null;
    /**列表对应的Adapter*/
    private ListAdapter mAdapter = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    public ActivityPoiSelect() {
        setPoiCountToGetOneTime(30);        //一次设置多一些
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        scrollStateChanged(scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        scroll(firstVisibleItem, visibleItemCount, totalItemCount);
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_select);
        
        //设置ListView
        mPoiListView = (ListView)findViewById(R.id.list_poi);
        mPoiListView.addFooterView(mLoadView);
        mAdapter = new ListAdapter(this);
        mPoiListView.setAdapter(mAdapter);
        
        //监听滑动
        mPoiListView.setOnScrollListener(this);
        //监听点击
        mPoiListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Poi poi = getPosItem(position);
                Intent data = new Intent();
                data.putExtra(INTENT_EXTRA_POI_ID, poi.poiid);
                data.putExtra(INTENT_EXTRA_POI_TITLE, poi.title);
                setResult(RESULT_OK, data);
                finish();       //点击后就返回了
            }
        });
    }
    
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_REFRESH_LIST: {
                if(mPoiList != null) {
                    mCount = mPoiList.size();
                }
                if(mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
            
            default: {
                super.handleBaseMessage(msg);
                break;
            }
        }
    }
    
    @Override
    protected void onGetPoiList(List<Poi> list) {
        sendMessageToBaseHandler(MSG_REFRESH_LIST);
        onLoadFinish();
    }

    @Override
    protected void onCategorySelected(String category_id) {
        sendMessageToBaseHandler(MSG_REFRESH_LIST);
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 读取某个位置上的项目
     */
    private Poi getPosItem(int position) {
        Poi ret = null;
        
        if(mPoiList != null) {
            if(position >= 0 && position < mPoiList.size()) {
                ret = mPoiList.get(position);
            }
        }
        
        return ret;
    }
}
