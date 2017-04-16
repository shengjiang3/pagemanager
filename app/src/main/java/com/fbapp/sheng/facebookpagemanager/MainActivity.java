package com.fbapp.sheng.facebookpagemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fbapp.sheng.facebookpagemanager.model.PagePreference;
import com.fbapp.sheng.facebookpagemanager.model.PostsItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements PostsFragment.OnListFragmentInteractionListener {
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
            switch(item.getItemId()) {
                case R.id.log_out_selection:
                    LoginManager.getInstance().logOut();
                    break;
            }
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
        //callbackManager = CallbackManager.Factory.create();
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
