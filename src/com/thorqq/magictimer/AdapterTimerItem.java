package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thorqq.magictimer.MagicTimerActivity.ListenerMore;
import com.thorqq.magictimer.MagicTimerActivity.ListenerNameInfo;
import com.thorqq.magictimer.MagicTimerActivity.ListenerTime;
import com.thorqq.magictimer.core.Timer;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdapterTimerItem extends BaseAdapter {

	public final class ViewHolder{
		
        public TextView time;
		public ImageView imageViewToggle;
		public TextView name_info;
		public ImageView more;
				
		public LinearLayout layoutTime;
        public LinearLayout layoutNameAndInfo;
        public LinearLayout layoutDown;
	}

    private LayoutInflater mInflater;
	private List<Map<String, Object>> mData;
	@SuppressWarnings("unused")
    private Context mContext;

	public AdapterTimerItem(Context context)
	{
		mInflater = LayoutInflater.from(context);
		mData = new ArrayList<Map<String, Object>>();
		mContext = context;
	}

	public AdapterTimerItem(Context context, List<Map<String, Object>> data)
	{
		mInflater = LayoutInflater.from(context);
		mData = data;
        mContext = context;
	}

	public void add(Map<String, Object> map)
	{
		mData.add(map);
	}
	
	public void updateItem(int position, String key, Object obj)
	{
		if (position < mData.size())
		{  
            mData.get(position).put(key, obj);  
        } 
	}

	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		if (position < mData.size())
		{  
            return mData.get(position);  
        } 
		else  
        {
            return null;  
        }
	}

	public long getItemId(int position) {
		if (position < mData.size())
		{  
            return position;  
        } 
		else  
        {
            return -1;  
        }
	}

	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder = null;
		
		Timer timer = null;
		ChildViewInterface childView;
		ListenerTime     lstTime = null;
		ListenerNameInfo lstNameInfo = null;
		ListenerMore     lstMore = null;
		int visibility = View.GONE;
        
		if (convertView == null) 
		{
			holder=new ViewHolder();  
			
			//布局
			convertView = mInflater.inflate(R.layout.timer_item, null);
			holder.time      = (TextView)convertView.findViewById(R.id.TimerItemTime);
            holder.imageViewToggle = (ImageView)convertView.findViewById(R.id.ImageViewToggle);
            holder.name_info = (TextView)convertView.findViewById(R.id.TimerItemNameInfo);
            holder.more      = (ImageView)convertView.findViewById(R.id.TimerItemMore);
            
            holder.layoutTime        = (LinearLayout)convertView.findViewById(R.id.RelativeLayoutTime);
            holder.layoutNameAndInfo = (LinearLayout)convertView.findViewById(R.id.RelativeLayoutNameAndInfo);
            holder.layoutDown        = (LinearLayout)convertView.findViewById(R.id.RelativeLayoutDown);

            //传递过来的数据
            timer       = (Timer)mData.get(position).get("timer");
            lstTime     = (ListenerTime)mData.get(position).get("listenerTime");
            lstNameInfo = (ListenerNameInfo)mData.get(position).get("listenerNameInfo");
            lstMore     = (ListenerMore)mData.get(position).get("listenerMore");
            childView   = (ChildViewInterface)mData.get(position).get("childView");
            visibility  = (Integer)mData.get(position).get("visibility");
            
            //子布局
            childView.initLayout();
            holder.layoutDown.addView(childView.getLayoutView());
            
            //监听器
            holder.layoutTime.setOnClickListener(lstTime);
            holder.layoutNameAndInfo.setOnClickListener(lstNameInfo);
            holder.more.setOnClickListener(lstMore);
            
			convertView.setTag(holder);
		}
		else 
		{
			holder = (ViewHolder)convertView.getTag();
		}
				
		holder.time.setText(Util.formatTwoNumber(timer.getTimerDef().getStartHour()) + 
		                    ":" + 
		                    Util.formatTwoNumber(timer.getTimerDef().getStartMinute()) );
        holder.name_info.setText(timer.getName() + "\n" + 
                                 Util.MillisToYYYYMMDD(timer.getNextTime()) + " " + 
                                 Util.MillisToHHMM(timer.getNextTime()));
        holder.layoutDown.setVisibility(visibility);
        
        if(visibility == View.GONE)
        {
            holder.more.setImageResource(R.drawable.btn_more_selector);
        }
        else if(visibility == View.VISIBLE)
        {
            holder.more.setImageResource(R.drawable.btn_more_down_selector);
        }
        
        if(timer.getTimerDef().isEnable() == 0)
        {
            holder.time.setTextColor(Color.GRAY);
            holder.name_info.setTextColor(Color.GRAY);
            
            holder.imageViewToggle.setImageResource(R.drawable.btn_toggle_off);
        }
        else
        {
            holder.time.setTextColor(Color.WHITE);
            holder.name_info.setTextColor(Color.WHITE);
            
            holder.imageViewToggle.setImageResource(R.drawable.btn_toggle_on);
        }       
                
		return convertView;
	}

}
