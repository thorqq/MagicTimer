package com.thorqq.magictimer;

import android.view.View;

public interface ChildViewInterface
{
    //
    public View getLayoutView();
    //��ʼ������
    public void initLayout();
    //ע�������
    public void registerListener();
    //���²���
    public void updateLayout();
    //��������
    public void updateData();
}
