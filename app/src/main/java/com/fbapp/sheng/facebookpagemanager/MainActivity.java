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
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.fbapp.sheng.facebookpagemanager.model.PagePreference;
import com.fbapp.sheng.facebookpagemanager.model.PostsItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements PostsFragment.OnListFragmentInteractionListener,
    PostMetricsFragment.OnFragmentInteractionListener {
    public static final String TAG = "MainActivity";

    private NavigationView drawerList;
    private ArrayAdapter<String> drawerAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
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
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
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

    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        LoginManager.getInstance().logInWithPublishPermissions(MainActivity.this, Arrays.asList("publish_pages"));
        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = PostsFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        NavigationView drawer = (NavigationView) findViewById(R.id.navigation_drawer);
        drawer.setNavigationItemSelectedListener(mOnNavigationViewItemSelectedListener);
        setDefaultPage();
    }

    @Override
    public void onListFragmentInteraction(PostsItem postsItem) {
        Log.v(TAG, postsItem.id.toString());
        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = PostMetricsFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Bundle postArgs = new Bundle();
        postArgs.putString("post_id", postsItem.id.toString());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.setArguments(postArgs);
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    private void setDefaultPage(){
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
                                String pageId = object.getJSONObject("accounts").getJSONArray("data").getJSONObject(0).getString("id");
                                String name = object.getJSONObject("accounts").getJSONArray("data").getJSONObject(0).getString("name");
                                new PagePreference(MainActivity.this).setPageId(pageId);
                                new PagePreference(MainActivity.this).setName(name);
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
