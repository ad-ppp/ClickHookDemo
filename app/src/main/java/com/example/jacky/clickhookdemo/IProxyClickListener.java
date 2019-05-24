package com.example.jacky.clickhookdemo;

import android.view.View;

public interface IProxyClickListener {
    boolean onProxyClick(WrapClickListener wrap, View v);

    class WrapClickListener implements View.OnClickListener {

        IProxyClickListener mProxyListener;
        View.OnClickListener mBaseListener;

        public WrapClickListener(View.OnClickListener l, IProxyClickListener proxyListener) {
            mBaseListener = l;
            mProxyListener = proxyListener;
        }

        @Override
        public void onClick(View v) {
            boolean handled = mProxyListener != null && mProxyListener.onProxyClick(WrapClickListener.this, v);
            if (!handled && mBaseListener != null) {
                mBaseListener.onClick(v);
            }
        }
    }
}
