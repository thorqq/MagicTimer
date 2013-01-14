package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thorqq.magictimer.SettingActivityTimer.TimeDefButtonListener;
import com.thorqq.magictimer.core.TTimerDef;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdapterTimeDef extends BaseAdapter
{

    public final class ViewHolder
    {
        public TextView time;
        public TextView name;
                
        public LinearLayout layoutUp;
        public LinearLayout layoutDown;
    }

    private LayoutInflater mInflater;
    private List<Map<String, Object>> mData;
    private Context mContext;

    public AdapterTimeDef(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mData = new ArrayList<Map<String, Object>>();
        mContext = context;
    }

    public AdapterTimeDef(Context context, List<Map<String, Object>> data)
    {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
    }

    public void add(Map<String, Object> map)
    {
        mData.add(map);
    }

    public void removeItem(int position)
    {
        mData.remove(position);
    }
    
    public void updateItem(int position, String key, Object obj)
    {
        if (position < mData.size())
        {
            mData.get(position).put(key, obj);
        }
    }

    public int getCount()
    {
        return mData.size();
    }

    public Object getItem(int position)
    {
        if (position < mData.size())
        {
            return mData.get(position);
        }
        else
        {
            return null;
        }
    }

    public long getItemId(int position)
    {
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
        
        TTimerDef timedef = null;
        ChildViewInterface childView;
        TimeDefButtonListener     lster = null;
        int visibility = View.GONE;
        
        if (convertView == null) 
        {
            holder=new ViewHolder();  
            
            //布局
            convertView = mInflater.inflate(R.layout.timedef_item, null);
            holder.time = (TextView)convertView.findViewById(R.id.tvTime);
            holder.time.setTypeface(((MagicTimerApp)mContext.getApplicationContext()).getNumTypeFace());
            holder.name = (TextView)convertView.findViewById(R.id.tvName);
            
            holder.layoutUp        = (LinearLayout)convertView.findViewById(R.id.LinearLayoutTimeSettingTime);
            holder.layoutDown        = (LinearLayout)convertView.findViewById(R.id.LayoutTimeDefSettingDown);

            //传递过来的数据
            timedef     = (TTimerDef)mData.get(position).get("timedef");
            lster       = (TimeDefButtonListener)mData.get(position).get("listener");
            childView   = (ChildViewInterface)mData.get(position).get("childView");
            visibility  = (Integer)mData.get(position).get("visibility");
            
            //子布局
            childView.initLayout();
            holder.layoutDown.addView(childView.getLayoutView());
            
            //监听器
            holder.layoutUp.setOnClickListener(lster);
            
            convertView.setTag(holder);
        }
        else 
        {
            holder = (ViewHolder)convertView.getTag();
        }
                
        holder.time.setText(timedef.getDescription() + " ");
        holder.name.setText(timedef.getName());
        holder.layoutDown.setVisibility(visibility);
                
        return convertView;    
    }

}
