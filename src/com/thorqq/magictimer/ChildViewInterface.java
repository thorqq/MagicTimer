package com.thorqq.magictimer;

import android.view.View;

public abstract class ChildViewInterface
{
    //��ȡ����
    public abstract View getLayoutView();
    //��ʼ��
    public void initLayout(ChildViewParent parent)
    {
        this.doInitLayout(parent);
        this.updateLayout();
        this.registerListener();
    }
    //���²���
    public abstract void updateLayout();
    
    //
    protected abstract void registerListener();
    protected abstract void doInitLayout(ChildViewParent parent);
}
