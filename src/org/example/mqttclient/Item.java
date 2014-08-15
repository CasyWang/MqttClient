package org.example.mqttclient;

public class Item {

    String mTitle;
    int mIconRes;
    int layoutRes;  //item��Ӧ��ҳ��XML��Դ
    int hashCode;   //Title��Hashֵ

    Item(String title, int iconRes, int layout) {
        mTitle = title;
        mIconRes = iconRes;
        hashCode = title.hashCode();
        layoutRes = layout;
    }
    
}
