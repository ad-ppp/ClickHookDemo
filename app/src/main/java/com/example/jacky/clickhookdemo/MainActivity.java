package com.example.jacky.clickhookdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private final int mPrivateTagKey = 960000;
    private Button button;
    private Button button2;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setText("click..1");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button2.setText("click..2");
            }
        });


        final ViewGroup rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        if (init()) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    hookViews(rootView, 0);
                }
            });
        }
    }

    private Method sHookMethod;
    private Field sHookField;

    public boolean init() {
        if (sHookMethod == null) {
            try {
                Class viewClass = Class.forName("android.view.View");
                if (viewClass != null) {
                    sHookMethod = viewClass.getDeclaredMethod("getListenerInfo");
                    if (sHookMethod != null) {
                        sHookMethod.setAccessible(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (sHookField == null) {
            try {
                Class listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
                if (listenerInfoClass != null) {
                    sHookField = listenerInfoClass.getDeclaredField("mOnClickListener");
                    if (sHookField != null) {
                        sHookField.setAccessible(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sHookMethod != null && sHookField != null;
    }

    private void hookViews(View view, int recycledContainerDeep) {
        if (view.getVisibility() == View.VISIBLE) {
            boolean forceHook = recycledContainerDeep == 1;
            if (view instanceof ViewGroup) {
                boolean existAncestorRecycle = recycledContainerDeep > 0;
                ViewGroup p = (ViewGroup) view;
                if (!(p instanceof AbsListView || p instanceof RecyclerView) || existAncestorRecycle) {
                    hookClickListener(view, recycledContainerDeep, forceHook);
                    if (existAncestorRecycle) {
                        recycledContainerDeep++;
                    }
                } else {
                    recycledContainerDeep = 1;
                }
                int childCount = p.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = p.getChildAt(i);
                    hookViews(child, recycledContainerDeep);
                }
            } else {
                hookClickListener(view, recycledContainerDeep, forceHook);
            }
        }
    }

    private void hookClickListener(View view, int recycledContainerDeep, boolean forceHook) {
        boolean needHook = forceHook;
        if (!needHook) {
            needHook = view.isClickable();
            if (needHook && recycledContainerDeep == 0) {
                needHook = view.getTag(mPrivateTagKey) == null;
            }
        }
        if (needHook) {
            try {
                Object getListenerInfo = sHookMethod.invoke(view);
                View.OnClickListener baseClickListener = getListenerInfo == null ? null : (View.OnClickListener) sHookField.get(getListenerInfo);//获取已设置过的监听器
                if ((baseClickListener != null && !(baseClickListener instanceof IProxyClickListener.WrapClickListener))) {
                    sHookField.set(getListenerInfo, new IProxyClickListener.WrapClickListener(baseClickListener, mInnerClickProxy));
                    view.setTag(mPrivateTagKey, recycledContainerDeep);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private final IProxyClickListener mInnerClickProxy = new IProxyClickListener() {
        @Override
        public boolean onProxyClick(WrapClickListener wrap, final View v) {
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Button button = (Button) v;
                    button.setText("hook code");
                }
            }, 1000);
            return false;
        }
    };
}
