package com.thorqq.magictimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thorqq.magictimer.action.TAction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class AdapterAction extends BaseAdapter
{

    public final class ViewHolder
    {
        public TextView actionDetail;
        public Button deleteAction;
    }

    private LayoutInflater mInflater;
    private List<Map<String, Object>> mData;

    public AdapterAction(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mData = new ArrayList<Map<String, Object>>();
    }

    public AdapterAction(Context context, List<Map<String, Object>> data)
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

            convertView = mInflater.inflate(R.layout.timing_action_item, null);
            holder.actionDetail = (TextView) convertView.findViewById(R.id.btnActionDetail);
            holder.deleteAction = (Button) convertView.findViewById(R.id.btnDeleteAction);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        TAction action = (TAction) mData.get(position).get("action");

        //弹出设置界面
        holder.actionDetail.setText(action.getDescription());
        holder.actionDetail.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                //TODO
            }
        });
        
        //删除本条目
//        holder.deleteAction.setText("删除");
        holder.deleteAction.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                mData.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

}
