package org.example.mqttclient;

public class Item {

    String mTitle;
    int mIconRes;
    int layoutRes;  //item对应的页面XML资源
    int hashCode;   //Title的Hash值

    Item(String title, int iconRes, int layout) {
        mTitle = title;
        mIconRes = iconRes;
        hashCode = title.hashCode();
        layoutRes = layout;
    }
    
}
