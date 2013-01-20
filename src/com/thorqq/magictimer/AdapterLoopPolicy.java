package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thorqq.magictimer.SettingActivityTimer.LoopPolicyButtonListener;
import com.thorqq.magictimer.core.TLoopPolicy;
import com.thorqq.magictimer.util.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdapterLoopPolicy extends BaseAdapter
{

    public final class ViewHolder
    {
        public Button excludeFlag;
        public TextView policyDetail;
        public Button deletePolicy;

        public LinearLayout layoutPolicyDetail;
    }

    private LayoutInflater mInflater;
    private List<Map<String, Object>> mData;
    //private TLoopPolicy mPolicy;

    public AdapterLoopPolicy(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mData = new ArrayList<Map<String, Object>>();
    }

    public AdapterLoopPolicy(Context context, List<Map<String, Object>> data)
    {
        mInflater = LayoutInflater.from(context);
        mData = data;
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
        if (convertView == null)
        {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.loop_item, null);
            holder.excludeFlag  = (Button) convertView.findViewById(R.id.btnExcludeFlag);
            holder.policyDetail = (TextView) convertView.findViewById(R.id.tvPolicyDetail);
            holder.deletePolicy = (Button) convertView.findViewById(R.id.btnDeletePolicy);
            holder.layoutPolicyDetail = (LinearLayout)convertView.findViewById(R.id.LinearLayoutPolicyDetail);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        TLoopPolicy policy = (TLoopPolicy) mData.get(position).get("policy");

        //排除、包含标志
        if (policy.getExcludeFlag() == 1)
        {
            holder.excludeFlag.setBackgroundResource(R.drawable.exclude);
        }
        else
        {
            holder.excludeFlag.setBackgroundResource(R.drawable.include);
        }

        holder.excludeFlag.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                TLoopPolicy policy = (TLoopPolicy) mData.get(position).get("policy");
                int tmp = (policy.getExcludeFlag() == 0 ? 1 : 0);
                policy.setExcludeFlag(tmp);
                
                if (policy.getExcludeFlag() == 1)
                {
                    ((Button)v).setBackgroundResource(R.drawable.exclude);
                }
                else
                {
                    ((Button)v).setBackgroundResource(R.drawable.include);
                }
         
            }
        });
        
        //弹出设置界面
        holder.policyDetail.setText(policy.getDescription());
        holder.layoutPolicyDetail.setOnClickListener((LoopPolicyButtonListener)mData.get(position).get("listener"));
        
        //删除本条目
//        holder.deletePolicy.setText("删除");
        holder.deletePolicy.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                mData.remove(position);
                notifyDataSetChanged();
                Util.log("删除:" + position);
            }
        });

        return convertView;
    }

}
