package com.codeless.tracker.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

/**
 * 2018/12/16.
 * GitHub:[https://github.com/jacky1234]
 *
 * @author jacky
 */
public class ViewHelper {

    /**
     * 获取view或者兄弟节点text信息
     *
     * @param view
     * @return
     */
    @NonNull
    public static String getFirstTextViewDescription(View view) {
        TextView firstTextView = getFirstTextView(view);
        if (firstTextView == null) {
            final ViewParent parent = view.getParent();
            if (parent instanceof View) {
                firstTextView = getFirstTextView((View) parent);
            }
        }
        return firstTextView != null ? firstTextView.getText().toString() : "";
    }

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    @Nullable
    public static String getComponentKey(View view) {
        final CharSequence contentDescription = view.getContentDescription();
        return contentDescription != null ?
                contentDescription.toString() : null;
    }

    @Nullable
    private static TextView getFirstTextView(View view) {
        if (view instanceof TextView) {
            return (TextView) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt instanceof TextView) {
                    return (TextView) childAt;
                }

                if (childAt instanceof ViewGroup) {
                    return getFirstTextView(childAt);
                }
            }
        }

        return null;
    }
}
