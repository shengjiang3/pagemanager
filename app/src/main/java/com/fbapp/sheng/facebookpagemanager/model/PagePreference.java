package com.fbapp.sheng.facebookpagemanager.model;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by sheng on 4/11/2017.
 */

public class PagePreference {
    private SharedPreferences prefs;

    public PagePreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getPageId() {
        return prefs.getString("page_id", "-1");
    }

    public String getPageAccessToken() {
        return prefs.getString("access_token", "-1");
    }

    public String getName() {
        return prefs.getString("name", "-1");
    }

    public void setPageId(String pageId) {
        prefs.edit().putString("page_id", pageId).commit();
    }

    public void setPageAccessToken(String accessToken) {
        prefs.edit().putString("access_token", accessToken).commit();
    }

    public void setName(String name) {
        prefs.edit().putString("name", name).commit();
    }
}
