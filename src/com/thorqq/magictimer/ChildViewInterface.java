package com.thorqq.magictimer;

import android.view.View;

public interface ChildViewInterface
{
    //
    public View getLayoutView();
    //
    public void initLayout(View parent);
    //���²���
    public void updateLayout();
    //��������
//    public void updateData();
    //
    public void registerListener();
}
