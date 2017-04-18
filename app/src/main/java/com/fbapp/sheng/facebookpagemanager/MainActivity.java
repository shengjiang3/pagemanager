package com.fbapp.sheng.facebookpagemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.fbapp.sheng.facebookpagemanager.model.PageItem;
import com.fbapp.sheng.facebookpagemanager.model.PagePreference;
import com.fbapp.sheng.facebookpagemanager.model.PostsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PostsFragment.OnListFragmentInteractionListener,
    PostMetricsFragment.OnFragmentInteractionListener, PublishPostsFragment.OnFragmentInteractionListener {
    public static final String TAG = "MainActivity";

    private NavigationView drawer;
    private RecyclerView drawerList;
    private List<PageItem> pageItemList;
    private MyPageItemsRecyclerViewAdapter pageItemsRecyclerViewAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Class fragmentClass = null;

            switch (item.getItemId()) {
                case R.id.navigation_posts:
                    fragmentClass = PostsFragment.class;
                case R.id.navigation_metrics:
                    fragmentClass = PostsFragment.class;
                case R.id.navigation_inbox:
                    fragmentClass = PostsFragment.class;
                case R.id.navigation_notifications:
                    fragmentClass = PostsFragment.class;
            }
            loadFragment(fragmentClass, null, false);
            return true;
        }

    };

    private NavigationView.OnNavigationItemSelectedListener mOnNavigationViewItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            DrawerLayout mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            switch(item.getItemId()) {
                case R.id.log_out_selection:
                    LoginManager.getInstance().logOut();
                    mNavigationDrawer.closeDrawers();
                    Intent intent = new Intent(MainActivity.this, FBLoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }

            mNavigationDrawer.closeDrawers();
            return true;
        }
    };

    private MyPageItemsRecyclerViewAdapter.OnItemClickListener mOnPageItemClickListener = new MyPageItemsRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(@NonNull PageItem item) {
            DrawerLayout mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            new PagePreference(MainActivity.this).setPageId(item.getPageId());
            new PagePreference(MainActivity.this).setName(item.getName());
            mNavigationDrawer.closeDrawers();
            Class fragmentClass = PostsFragment.class;
            loadFragment(fragmentClass, null, false);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        LoginManager.getInstance().logInWithPublishPermissions(MainActivity.this, Arrays.asList("publish_pages"));

        Class fragmentClass = PostsFragment.class;
        loadFragment(fragmentClass, null, false);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        drawer = (NavigationView) findViewById(R.id.fixed_menu_navigation_view);
        drawer.setNavigationItemSelectedListener(mOnNavigationViewItemSelectedListener);

        pageItemList = new ArrayList<PageItem>(0);
        pageItemsRecyclerViewAdapter = new MyPageItemsRecyclerViewAdapter(pageItemList, mOnPageItemClickListener);

        drawerList = (RecyclerView) findViewById(R.id.page_drawer_list);
        drawerList.setAdapter(pageItemsRecyclerViewAdapter);

        initializeDrawer();
    }

    @Override
    public void onListFragmentInteraction(PostsItem postsItem) {
        Log.v(TAG, postsItem.id.toString());

        Bundle postArgs = new Bundle();
        postArgs.putString("post_id", postsItem.id.toString());
        Class fragmentClass = PostMetricsFragment.class;
        loadFragment(fragmentClass, postArgs, true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public void loadFragment(Class fragmentClass, Bundle args, boolean shouldAddToBackStack) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(args != null) {
            fragment.setArguments(args);
        }
        if(shouldAddToBackStack) {
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).addToBackStack(null).commit();
        }
        else {
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }
    }

    private void initializeDrawer(){
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if(object != null) {
                            Log.v(TAG, response.toString());
                            try {
                                JSONArray data = object.getJSONObject("accounts").getJSONArray("data");
                                pageItemList.clear();

                                for(int i = 0; i < data.length(); ++i) {
                                    String pageId = data.getJSONObject(i).getString("id");
                                    String name = data.getJSONObject(i).getString("name");
                                    PageItem pageItem = new PageItem(pageId, name);
                                    pageItemList.add(pageItem);

                                    if(i == 0) {
                                        new PagePreference(MainActivity.this).setPageId(pageId);
                                        new PagePreference(MainActivity.this).setName(name);
                                    }
                                }
                                pageItemsRecyclerViewAdapter.notifyDataSetChanged();
                                drawerList.setAdapter(pageItemsRecyclerViewAdapter);
                            }
                            catch(JSONException jsone) {
                                jsone.printStackTrace();
                            }
                        }
                    }
                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "accounts");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
