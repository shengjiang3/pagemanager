package com.fbapp.sheng.facebookpagemanager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostsItem {
    public String id;
    public String content;
    public String from;
    public boolean published;

    public PostsItem(String id, String content, String from, boolean published) {
        this.id = id;
        this.content = content;
        this.from = from;
        this.published = published;
    }

    @Override
    public String toString() {
        return content;
    }
}
