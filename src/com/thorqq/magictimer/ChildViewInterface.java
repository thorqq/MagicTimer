package com.thorqq.magictimer;

import android.view.View;

public abstract class ChildViewInterface
{
    //获取布局
    public abstract View getLayoutView();
    //初始化
    public void initLayout(ChildViewParent parent)
    {
        this.doInitLayout(parent);
        this.updateLayout();
        this.registerListener();
    }
    //更新布局
    public abstract void updateLayout();
    
    //
    protected abstract void registerListener();
    protected abstract void doInitLayout(ChildViewParent parent);
}
