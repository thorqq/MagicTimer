package com.thorqq.magictimer;

import android.view.View;

public interface ChildViewInterface
{
    //
    public View getLayoutView();
    //初始化布局
    public void initLayout();
    //注册监听器
    public void registerListener();
    //更新布局
    public void updateLayout();
    //更新数据
    public void updateData();
}
