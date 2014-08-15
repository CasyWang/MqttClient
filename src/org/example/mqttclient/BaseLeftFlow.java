/*
 *   MQTT app on Android
 *   Author: Oliver
 *   Description:
 *    
*/
package org.example.mqttclient;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import org.example.mqttclient.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

//�ӿڽ�������ʵ�ַָ���,ʹ��ͬ��������в�ͬ�Ľӿڷ���ʵ��
public abstract class BaseLeftFlow extends FragmentActivity implements MenuAdapter.MenuListener {

    private static final String STATE_ACTIVE_POSITION =
            "net.simonvt.menudrawer.samples.MainActivity.activePosition";

    protected MenuDrawer mMenuDrawer;
    protected MenuAdapter mAdapter;
    protected ListView mList;

    private int mActivePosition = 0;
    private List<Object> items = null;   
  
    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        if (inState != null) {
            mActivePosition = inState.getInt(STATE_ACTIVE_POSITION);
        }

        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, getDrawerPosition(), getDragMode());

        //�ڴ˴�ָ��Item��Ӧ��ҳ��
        items = new ArrayList<Object>();
        
        items.add(new Category("Client"));
        items.add(new Item("Control", R.drawable.ic_action_select_all_dark, R.layout.activity_clientcontrol));
        items.add(new Item("Sensor", R.drawable.ic_action_select_all_dark, R.layout.chart));
        items.add(new Category("Management"));
        items.add(new Item("Setup", R.drawable.ic_action_select_all_dark, R.layout.activity_windowsample));
        items.add(new Item("Heartbeat", R.drawable.ic_action_refresh_dark, R.layout.activity_rightmenu));

        /*
         * ListView is a view group that displays a list of scrollable items. 
         * The list items are automatically inserted to the list using an Adapter 
         * that pulls content from a source such as an array or database query and 
         * converts each item result into a view that's placed into the list.
        */
        mList = new ListView(this);

        mAdapter = new MenuAdapter(this, items);
        mAdapter.setListener(this);
        mAdapter.setActivePosition(mActivePosition);

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mItemClickListener);

        mMenuDrawer.setMenuView(mList);
    }

    //���󷽷�,��������ʵ��
    protected abstract void onMenuItemClicked(int position, Item item);

    protected abstract int getDragMode();

    protected abstract Position getDrawerPosition();

    //�˵�����ص�����
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mActivePosition = position;
            mMenuDrawer.setActiveView(view, position);
            mAdapter.setActivePosition(position);
            onMenuItemClicked(position, (Item) mAdapter.getItem(position));
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
    }

    //ʵ�ֽӿڵķ���
    @Override
    public void onActiveViewChanged(View v) {
        mMenuDrawer.setActiveView(v, mActivePosition);
    }
        
    public List<Object> getItems(){
    	return this.items;
    }
}
