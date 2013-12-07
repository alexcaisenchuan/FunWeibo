/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.weibo.sdk.android.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.net.Uri;
import android.text.TextUtils;

import com.alex.common.utils.KLog;
import com.weibo.sdk.android.WeiboDefines;
import com.weibo.sdk.android.http.Response;
import com.weibo.sdk.android.org.json.JSONArray;
import com.weibo.sdk.android.org.json.JSONException;
import com.weibo.sdk.android.org.json.JSONObject;


/**
 * A data class representing one single status of a user.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Status extends WeiboResponse implements java.io.Serializable {
    
    private static final String TAG = "Status";
    
    /**
     * 类型定义
     * @author caisenchuan
     */
    public enum TypeSpec {
        /**普通微博*/
        NORMAL,
        /**发送中的新微博*/
        NEW_WEIBO_SENDING,
        /**发送失败的新微博*/
        NEW_WEIBO_FAILD
    }
    
    /**
     * 我们自己定义的一些额外参数，并不存在实际微博数据中
     * @author caisenchuan
     */
    public class ExtraParams implements java.io.Serializable{
        /*--------------------------
         * 常量
         *-------------------------*/
        /**
         * 序列化编号
         */
        private static final long serialVersionUID = -7621953453209633274L;
        
        /*--------------------------
         * 成员
         *-------------------------*/
        /**微博的类型*/
        private TypeSpec mType = TypeSpec.NORMAL;
        /**发送微博的编号*/
        private int mTempId = -1;
        
        /*--------------------------
         * 方法
         *-------------------------*/
        /**
         * @return the mType
         */
        public TypeSpec getType() {
            return mType;
        }
        /**
         * @param mType the mType to set
         */
        public void setType(TypeSpec mType) {
            this.mType = mType;
        }
        /**
         * @return the mTempId
         */
        public int getTempId() {
            return mTempId;
        }
        /**
         * @param mTempId the mTempId to set
         */
        public void setTempId(int mTempId) {
            this.mTempId = mTempId;
        }
    }
    
	/**
	 * 序列化编号
	 */
	private static final long serialVersionUID = -8795691786466526420L;

	/**
	 * 我们自己定义的一些额外参数，并不存在实际微博数据中
	 * @author caisenchuan
	 * */
	private ExtraParams extraParams = new ExtraParams();
	
	////////////////////////////微博数据//////////////////////////////

    private User user = null;
	private Place place = null;         //微博对应地点，可以没有
	
	private Date createdAt;             //status创建时间
	private long id;                    //status id
	private String text;                //微博内容
	private String source;              //微博来源
	private boolean isTruncated;        //保留字段
	private long inReplyToStatusId;
	private long inReplyToUserId;
	private boolean isFavorited;        //保留字段，未弃用
	private String inReplyToScreenName;
	private double latitude = -1;       //纬度
	private double longitude = -1;      //经度
	private String thumbnail_pic;       //微博内容中的图片的缩略地址
	private String bmiddle_pic;         //中型图片
	private String original_pic;        //原始图片
	private Status retweeted_status;    //转发的微博内容
	private String mid;                 //mid
	private long attitudes_count;       //赞的个数
	
	///////////////////////////////////////////////////////////////////////
	/**
	 * 无参构造函数
	 */
	private Status() {
	    //...
	}
	
	/*package*/Status(Response res, Weibo weibo) throws WeiboException {
		super(res);
		Element elem = res.asDocument().getDocumentElement();
		init(res, elem, weibo);
	}

	/*package*/Status(Response res, Element elem, Weibo weibo) throws
	WeiboException {
		super(res);
		init(res, elem, weibo);
	}

	Status(Response res)throws WeiboException{
		super(res);
		JSONObject json=res.asJSONObject();
		constructJson(json);
	}
	/* modify by Reilost add some field*/
	private void constructJson(JSONObject json) throws WeiboException {
		try {
			id = json.getLong("id");
			text = json.getString("text");
			source = json.getString("source");
			createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
			inReplyToStatusId = getLong("in_reply_to_status_id", json);
			inReplyToUserId = getLong("in_reply_to_user_id", json);
			isFavorited = getBoolean("favorited", json);
			thumbnail_pic = json.getString("thumbnail_pic");
			bmiddle_pic = json.getString("bmiddle_pic");
			original_pic = json.getString("original_pic");
			if(!json.isNull("user"))
				user = new User(json.getJSONObject("user"));
			inReplyToScreenName=json.getString("inReplyToScreenName");
			if(!json.isNull("retweeted_status")){
				retweeted_status= new Status(json.getJSONObject("retweeted_status"));
			}
			
			mid=json.getString("mid");
			attitudes_count=json.getLong("attitudes_count");
			String geo= json.getString("geo");
			if(geo!=null &&!"".equals(geo) &&!"null".equals(geo)){
				getGeoInfo(geo);
			}
			
			//地点信息
			try {
    			JSONArray arr = json.getJSONArray("annotations");
    			if(arr != null) {
    			    for(int i = 0; i < arr.length(); i++) {
    			        try {
        			        //找到一个place即退出
        			        JSONObject obj = arr.getJSONObject(i);
        			        JSONObject place_t = obj.getJSONObject("place");
        			        if(place_t != null) {
        			            this.place = new Place(place_t);
        			            break;
        			        }
    			        } catch (Exception e) {
    		                KLog.w(TAG, "Exception", e);
    		                continue;
    		            }
    			    }
    			}
			} catch (Exception e) {
                KLog.w(TAG, "Exception", e);
            }
		} catch (JSONException je) {
			throw new WeiboException(je.getMessage() + ":" + json.toString(), je);
		}
	}

	private void getGeoInfo(String geo) {
		StringBuffer value= new StringBuffer();
		for(char c:geo.toCharArray()){
			if(c>45&&c<58){
				value.append(c);
			}
			if(c==44){
				if(value.length()>0){
					latitude=Double.parseDouble(value.toString());
					value.delete(0, value.length());
				}
			}
		}
		longitude=Double.parseDouble(value.toString());
	}


	public Status(JSONObject json)throws WeiboException, JSONException{
		constructJson(json);
	}
	public Status(String str) throws WeiboException, JSONException {
		// StatusStream uses this constructor
		super();
		JSONObject json = new JSONObject(str);
		constructJson(json);
	}

	private void init(Response res, Element elem, Weibo weibo) throws
	WeiboException {
		ensureRootNodeNameIs("status", elem);
		user = new User(res, (Element) elem.getElementsByTagName("user").item(0)
				, weibo);
		id = getChildLong("id", elem);
		text = getChildText("text", elem);
		source = getChildText("source", elem);
		createdAt = getChildDate("created_at", elem);
		isTruncated = getChildBoolean("truncated", elem);
		inReplyToStatusId = getChildLong("in_reply_to_status_id", elem);
		inReplyToUserId = getChildLong("in_reply_to_user_id", elem);
		isFavorited = getChildBoolean("favorited", elem);
		inReplyToScreenName = getChildText("in_reply_to_screen_name", elem);
		NodeList georssPoint = elem.getElementsByTagName("georss:point");
		if(1 == georssPoint.getLength()){
			String[] point = georssPoint.item(0).getFirstChild().getNodeValue().split(" ");
			if(!"null".equals(point[0]))
				latitude = Double.parseDouble(point[0]);
			if(!"null".equals(point[1]))
				longitude = Double.parseDouble(point[1]);
		}
		NodeList retweetDetailsNode = elem.getElementsByTagName("retweet_details");
		if(1 == retweetDetailsNode.getLength()){
			retweeted_status= new Status(res,(Element)retweetDetailsNode.item(0),weibo);
		}
	}

	public void update(Status status) {
	    if(status != null) {
	        this.user = status.user;
	        this.place = status.place;
	        this.createdAt = status.createdAt;
	        this.id = status.id;
	        this.text = status.text;
	        this.source = status.source;
	        this.isTruncated = status.isTruncated;
	        this.inReplyToStatusId = status.inReplyToStatusId;
	        this.inReplyToUserId = status.inReplyToUserId;
	        this.isFavorited = status.isFavorited;
	        this.inReplyToScreenName = status.inReplyToScreenName;
	        this.latitude = status.latitude;
	        this.longitude = status.longitude;
	        this.thumbnail_pic = status.thumbnail_pic;
	        this.bmiddle_pic = status.bmiddle_pic;
	        this.original_pic = status.original_pic;
	        this.retweeted_status = status.retweeted_status;
	        this.mid = status.mid;
	        this.attitudes_count = status.attitudes_count;
	    }
	}
	/**
	 * Return the created_at
	 *
	 * @return created_at
	 * @since Weibo4J 1.1.0
	 */
	public Date getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Returns the id of the status
	 *
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Returns the text of the status
	 *
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Returns the source
	 *
	 * @return the source
	 * @since Weibo4J 1.2.1
	 */
	public String getSource() {
		return this.source;
	}


	/**
	 * Test if the status is truncated
	 *
	 * @return true if truncated
	 * @since Weibo4J 1.2.1
	 */
	public boolean isTruncated() {
		return isTruncated;
	}

	/**
	 * Returns the in_reply_tostatus_id
	 *
	 * @return the in_reply_tostatus_id
	 * @since Weibo4J 1.2.1
	 */
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	/**
	 * Returns the in_reply_user_id
	 *
	 * @return the in_reply_tostatus_id
	 * @since Weibo4J 1.2.1
	 */
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	/**
	 * Returns the in_reply_to_screen_name
	 *
	 * @return the in_in_reply_to_screen_name
	 * @since Weibo4J 1.2.1
	 */
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	/**
	 * returns The location's latitude that this tweet refers to.
	 *
	 * @since Weibo4J 1.2.1
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * returns The location's longitude that this tweet refers to.
	 *
	 * @since Weibo4J 1.2.1
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Test if the status is favorited
	 *
	 * @return true if favorited
	 * @since Weibo4J 1.2.1
	 */
	public boolean isFavorited() {
		return isFavorited;
	}

	public String getThumbnail_pic() {
		return thumbnail_pic;
	}

	public String getBmiddle_pic() {
		return bmiddle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}


	/**
	 * Return the user
	 *
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 *
	 * @since Weibo4J 1.2.1
	 */
	public boolean isRetweet(){
		return null != retweeted_status;
	}

	public Status getRetweeted_status() {
		return retweeted_status;
	}

	public String getMid() {
		return mid;
	}
	
	/**
	 * 读取地点信息
	 * @return
	 */
	public Place getPlace() {
	    return place;
	}

	/**
	 * 读取赞的个数 
	 * @return
	 */
	public long getAttitudesCount() {
	    return attitudes_count;
	}
	
	   
    /**
     * 读取扩展参数
     * @return the mExtraParams
     */
    public ExtraParams getExtraParams() {
        return extraParams;
    }

    /**
     * 设置扩展参数
     * @param extraParams the mExtraParams to set
     */
    public void setExtraParams(ExtraParams extraParams) {
        this.extraParams = extraParams;
    }
    
	/**
	 * 使用返回的json字符串构建微博列表
	 * @param jsonStr json字符串，"statuses"列表
	 * @return 其中的微博列表
	 * @throws JSONException
	 * @throws WeiboException
	 * @author caisenchuan
	 */
	public static List<Status> constructStatuses(String jsonStr)
	                           throws JSONException, WeiboException {
	    List<Status> ret = new ArrayList<Status>();
	    if(TextUtils.isEmpty(jsonStr) || jsonStr.equals(WeiboDefines.RET_EMPTY_ARRAY)) {
	        //返回[]则代表没有数据
	        return ret;
	    }
	    
	    JSONObject json = new JSONObject(jsonStr);
	    JSONArray arr = json.getJSONArray("statuses");
	    if(arr != null) {
	        for(int i = 0; i < arr.length(); i++) {
	            JSONObject obj = arr.getJSONObject(i);
	            Status s = new Status(obj);
	            ret.add(s);
	        }
	    }
	    
	    return ret;
	}
	
	/**
	 * 创建发布过程中的临时微博对象
	 * @param tempId
	 * @param poiid
	 * @param poiTitle
	 * @param statusContent
	 * @param picPath
	 * @return
	 * @author caisenchuan
	 */
	public static Status getNewWeiboTempStatus(int tempId, String poiid, String poiTitle, String statusContent, String picPath) {
	    Status ret = new Status();
	    //File f = new File(picPath);
	    //String imgUri = Uri.fromFile(f).toString();
	    //KLog.d(TAG, "imgUri : %s", imgUri);
	    String imgUri = picPath;
	    
	    //微博数据
	    ret.place = new Place();
	    ret.place.title = poiTitle;
	    ret.place.poiid = poiid;
	    
	    ret.text = statusContent;
	    
	    ret.thumbnail_pic = imgUri;
	    ret.bmiddle_pic = imgUri;
	    ret.original_pic = imgUri;
	    
	    //额外参数
	    ret.extraParams.mType = TypeSpec.NEW_WEIBO_SENDING;
	    ret.extraParams.mTempId = tempId;
	    
	    return ret;
	}
	
	public static List<Status> constructStatuses(Response res, Weibo weibo) throws WeiboException {

		Document doc = res.asDocument();
		if (isRootNodeNilClasses(doc)) {
			return new ArrayList<Status>(0);
		} else {
			try {
				ensureRootNodeNameIs("statuses", doc);
				NodeList list = doc.getDocumentElement().getElementsByTagName(
				"status");
				int size = list.getLength();
				List<Status> statuses = new ArrayList<Status>(size);
				for (int i = 0; i < size; i++) {
					Element status = (Element) list.item(i);
					statuses.add(new Status(res, status, weibo));
				}
				return statuses;
			} catch (WeiboException te) {
				ensureRootNodeNameIs("nil-classes", doc);
				return new ArrayList<Status>(0);
			}
		}

	}

	/*modify by sycheng add json call method*/
	public static List<Status> constructStatuses(Response res) throws WeiboException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<Status> statuses = new ArrayList<Status>(size);

			for (int i = 0; i < size; i++) {
				statuses.add(new Status(list.getJSONObject(i)));
			}
			return statuses;
		} catch (JSONException jsone) {
			throw new WeiboException(jsone);
		} catch (WeiboException te) {
			throw te;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Status other = (Status) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Status [createdAt=" + createdAt + ", id=" + id + ", text="
		+ text + ", source=" + source + ", isTruncated=" + isTruncated
		+ ", inReplyToStatusId=" + inReplyToStatusId
		+ ", inReplyToUserId=" + inReplyToUserId + ", isFavorited="
		+ isFavorited + ", inReplyToScreenName=" + inReplyToScreenName
		+ ", latitude=" + latitude + ", longitude=" + longitude
		+ ", thumbnail_pic=" + thumbnail_pic + ", bmiddle_pic="
		+ bmiddle_pic + ", original_pic=" + original_pic
		+ ",  mid=" + mid + ", user=" + user 
		+", retweeted_status="+(retweeted_status==null?"null":retweeted_status.toString())+ 
		"]";
	}


}
