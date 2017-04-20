package com.fbapp.sheng.facebookpagemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
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
    private SharedPreferences sharedPreferences;

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
                    sharedPreferences.edit().putString("page_id", "none").apply();
                    sharedPreferences.edit().putString("access_token", "none").apply();
                    sharedPreferences.edit().putString("name", "none").apply();
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
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("page_id", item.getPageId());
            editor.putString("name", item.getName());
            editor.putString("access_token", "none");
            editor.commit();
            getPageAccessToken();
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

        drawer = (NavigationView) findViewById(R.id.fixed_menu_navigation_view);
        drawer.setNavigationItemSelectedListener(mOnNavigationViewItemSelectedListener);

        pageItemList = new ArrayList<PageItem>(0);
        pageItemsRecyclerViewAdapter = new MyPageItemsRecyclerViewAdapter(pageItemList, mOnPageItemClickListener);

        drawerList = (RecyclerView) findViewById(R.id.page_drawer_list);
        drawerList.setAdapter(pageItemsRecyclerViewAdapter);

        sharedPreferences = getSharedPreferences("PagePreference", MODE_PRIVATE);

        initializeDrawer();

        Class fragmentClass = PostsFragment.class;
        loadFragment(fragmentClass, null, false);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
                                    if(i == 0) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("page_id", pageId);
                                        editor.putString("name", name);
                                        editor.putString("access_token", "none");
                                        editor.commit();
                                    }
                                    pageItemList.add(pageItem);
                                }
                                pageItemsRecyclerViewAdapter.notifyDataSetChanged();
                                drawerList.setAdapter(pageItemsRecyclerViewAdapter);
                                getPageAccessToken();
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

    private void getPageAccessToken() {
        String pageId = getSharedPreferences("PagePreference", Context.MODE_PRIVATE).getString("page_id", "none");
        if(pageId == "none") {
            Toast.makeText(MainActivity.this, "Error acquiring page_id", Toast.LENGTH_SHORT).show();
        }
        else {
            Bundle parameters = new Bundle();
            parameters.putString("fields", "access_token");
            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/" + pageId,
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            Log.v(TAG, response.toString());
                            try {
                                String pageAccessToken = response.getJSONObject().getString("access_token").toString();
                                SharedPreferences.Editor editor = getSharedPreferences("PagePreference", Context.MODE_PRIVATE).edit();
                                editor.putString("access_token", pageAccessToken);
                                editor.commit();
                            } catch (JSONException e) {
                                Toast.makeText(MainActivity.this, "Error acquiring page access token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            request.executeAsync();
        }
    }

}
