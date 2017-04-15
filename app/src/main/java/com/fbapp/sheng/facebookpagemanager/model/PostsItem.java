package com.fbapp.sheng.facebookpagemanager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostsItem {
    public String id;
    public String content;
    public String from;

    public PostsItem(String id, String content, String from) {
        this.id = id;
        this.content = content;
        this.from = from;
    }

    @Override
    public String toString() {
        return content;
    }
}
