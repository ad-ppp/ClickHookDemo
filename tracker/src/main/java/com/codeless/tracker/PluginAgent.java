package com.codeless.tracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.*;
import com.codeless.tracker.utils.PathUtil;
import com.codeless.tracker.utils.StringEncrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangdan on 17/3/3.
 * <p>
 * todo 在该类中添加要注入的具体代码
 */

public class PluginAgent {
    public static HashMap<Integer, Pair<Integer, String>> sAliveFragMap = new HashMap<>();
    private static final String TAG = "PluginAgent";

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

    public static void onClick(View view) {
        boolean hasBusiness = false;

        Context context = view.getContext();
        if (context instanceof Activity) {
            String pageName = context.getClass().getName();
            String currViewPath = PathUtil.getViewPath(view);
            String eventId = StringEncrypt.Encrypt(pageName + currViewPath, StringEncrypt.DEFAULT);

//            Map<String, Object> configureMap = Tracker.instance(context).getConfigureMap();
//            if (null != configureMap) {
//                JSONArray nodesArr = (JSONArray) configureMap.get(pageName);
//                if (null != nodesArr && nodesArr.size() > 0) {
//                    for (int i = 0; i < nodesArr.size(); i++) {
//                        JSONObject nodeObj = nodesArr.getJSONObject(i);
//                        String viewPath = nodeObj.getString(ConfigConstants.VIEWPATH);
//                        String dataPath = nodeObj.getString(ConfigConstants.DATAPATH);
//                        if (currViewPath.equals(viewPath) || PathUtil.match(currViewPath, viewPath)) {
//                            // 按照路径dataPath搜集数据
//                            Object businessData = PathUtil.getDataObj(view, dataPath);
//                            Map<String, Object> attributes = new HashMap<>();
//                            attributes.put(ConfigConstants.PAGENAME, pageName);
//                            attributes.put(ConfigConstants.VIEWPATH, currViewPath);
//                            JSONArray subPaths = nodeObj.getJSONArray(ConfigConstants.VIEWPATHSUB);
//                            if (null == subPaths || subPaths.size() == 0) {
//                                attributes.put(ConfigConstants.BUSINESSDATA, businessData);
//                            } else {
//                                for (int j = 0; j < subPaths.size(); j++) {
//                                    String subPath = subPaths.getString(j);
//                                    Object obj = PathUtil.getDataObj(businessData, subPath);
//                                    attributes.put(subPath, obj);
//                                }
//                            }
//                            Tracker.instance(context).trackEvent(eventId, attributes);
//                            hasBusiness = true;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (!hasBusiness) {
//                Tracker.instance(context).trackEvent(eventId, null);
//            }

            Map<String, Object> map = new HashMap<>();
            map.put(ConfigConstants.PAGENAME, pageName);
            map.put(ConfigConstants.VIEWPATH, currViewPath);
            Tracker.instance(context).trackEvent(eventId, map);
        }
    }

    public static void onClick(Object object, DialogInterface dialogInterface, int which) {

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

    //just to test asm bytecode
    public static void onFragmentPause(Object obj, Object object) {
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
        ActivityAgent.onActivityCreate(object, savedInstanceState);
    }

    public static void onActivityResume(Object object) {
        ActivityAgent.onActivityResume(object);
    }

    public static void onActivityPause(Object object) {
        ActivityAgent.onActivityPause(object);
    }
}
