package com.codeless.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.codeless.tracker.utils.PathUtil;
import com.codeless.tracker.utils.StringEncrypt;
import com.codeless.tracker.utils.ViewHelper;

import java.util.HashMap;
import java.util.Map;

public class PluginAgent {
    public static HashMap<Integer, Pair<Integer, String>> sAliveFragMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    public static void onClick(View view) {
        Context context = view.getContext();
        if (context != null) {
            String pageName = context.getClass().getName();
            String currViewPath = PathUtil.getViewPath(view);
            String eventId = StringEncrypt.Encrypt(pageName + currViewPath, StringEncrypt.DEFAULT);
            final String desc = ViewHelper.getFirstTextViewDescription(view);
            final String componentKey = ViewHelper.getComponentKey(view);

            Map<String, Object> map = new HashMap<>();
            map.put(ConfigConstants.PAGENAME, pageName);
            map.put(ConfigConstants.VIEWPATH, currViewPath);
            map.put(ConfigConstants.VIEWDESC, desc);
            Tracker.instance(context).trackEvent(eventId, map);

//            StringBuilder stringBuilder = new StringBuilder();
//            final String[] split = currViewPath.split("/");
//            if (split.length > 2) {
//                stringBuilder.append(split[split.length - 2]).append("/").append(split[split.length - 1]);
//            } else {
//                stringBuilder.append(currViewPath);
//            }
//
//            MarsIO.addClick(ComponentAgent.activeActivity, PathUtil.getFirstFragment(view),
//                    stringBuilder.toString(), desc, componentKey);
//            if (SharedPreferenceUtil.getInstance(context).needShowVisualTracker()) {
//                TrackerHelper.send(context, ComponentAgent.activeActivity, ActionType.onClick,
//                        PathUtil.getFirstFragment(view), stringBuilder.toString(), desc, componentKey);
//            }
        }
    }

    public static void onClick(Object object, DialogInterface dialogInterface, int which) {
        try {
            Button button = null;
            if (dialogInterface instanceof android.support.v7.app.AlertDialog) {
                android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) dialogInterface;
                button = dialog.getButton(which);
            }

            if (dialogInterface instanceof AlertDialog) {
                AlertDialog dialog2 = (AlertDialog) dialogInterface;
                button = dialog2.getButton(which);
            }


            if (button != null) {
                final String componentKey = ViewHelper.getComponentKey(button);

//                MarsIO.addClick(ComponentAgent.activeActivity,
//                        null,
//                        ComponentAgent.activeActivity + "_dialog",
//                        button.getText().toString(), componentKey);
//
//                if (SharedPreferenceUtil.getInstance(Starter.getContext()).needShowVisualTracker()) {
//                    TrackerHelper.send(Starter.getContext(), ComponentAgent.activeActivity, ActionType.onClick,
//                            null,
//                            ComponentAgent.activeActivity + "_dialog",
//                            button.getText().toString(), componentKey);
//                }
            }
        } catch (Exception e) {
        }
    }

    public static void onItemClick(Object object, AdapterView parent, View view, int position, long id) {

    }

    public static void onItemSelected(Object object, AdapterView parent, View view, int position, long id) {
        onItemClick(object, parent, view, position, id);
    }

    public static void onGroupClick(Object thisObject, ExpandableListView parent, View v, int groupPosition, long id) {

    }

    public static void onChildClick(Object thisObject, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

    }

    public static void onStopTrackingTouch(Object thisObj, SeekBar seekBar) {

    }

    public static void onRatingChanged(Object thisObj, RatingBar ratingBar, float rating, boolean fromUser) {

    }

    public static void onCheckedChanged(Object object, RadioGroup radioGroup, int checkedId) {

    }

    public static void onCheckedChanged(Object object, CompoundButton button, boolean isChecked) {

    }

    public static void onFragmentResume(Object obj) {
        addAliveFragment(obj);
    }

    public static void onFragmentPause(Object obj) {
        removeAliveFragment(obj);
    }

    private static boolean checkFragment(android.support.v4.app.Fragment paramFragment) {
        return true;
    }

    private static boolean checkFragment(Fragment paramFragment) {
        return true;
    }

    public static void setFragmentUserVisibleHint(Object obj, boolean isUserVisibleHint) {
        if (isUserVisibleHint) {
            addAliveFragment(obj);
        } else {
            removeAliveFragment(obj);
        }
    }

    public static void onFragmentHiddenChanged(Object fragment, boolean hidden) {
        setFragmentUserVisibleHint(fragment, !hidden);
    }

    private static void addAliveFragment(Object obj) {
        View view = null;
        if (obj instanceof Fragment) {
            view = ((Fragment) obj).getView();
        } else if (obj instanceof android.support.v4.app.Fragment) {
            view = ((android.support.v4.app.Fragment) obj).getView();
        }
        if (null != view) {
            int viewCode = view.hashCode();
            sAliveFragMap.put(obj.hashCode(), new Pair<>(viewCode, obj.getClass().getSimpleName()));
        }
    }

    private static void removeAliveFragment(Object obj) {
        if (null != obj) {
            sAliveFragMap.remove(obj.hashCode());
        }
    }

    public static void onActivityCreate(Object object, @Nullable Bundle savedInstanceState) {
    }

    public static void onActivityDestroy(Object object) {
    }

    public static void onFragmentCreate(Object obj, Bundle savedInstanceState) {
    }

    public static void onActivityResume(Object object) {
    }

    public static void onActivityPause(Object object) {
    }

    private Activity getActivity(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }

        return null;
    }

}
