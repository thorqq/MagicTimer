package com.thorqq.magictimer;

import android.view.View;

public interface ChildViewInterface
{
    //
    public View getLayoutView();
    //
    public void initLayout();
    //更新布局
    public void updateLayout();
    //更新数据
    public void updateData();
}
