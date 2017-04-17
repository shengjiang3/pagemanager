package com.fbapp.sheng.facebookpagemanager.model;

/**
 * Created by sheng on 4/17/2017.
 */

public class PageItem {
    private String page_id;
    private String name;

    public PageItem(String page_id, String name) {
        this.page_id = page_id;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getPageId() {
        return this.page_id;
    }
}
