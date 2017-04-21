package com.fbapp.sheng.facebookpagemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

public class FBLoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private final String TAG = "FBLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fblogin);
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("read_insights, manage_pages");
        if(AccessToken.getCurrentAccessToken() != null) {
            startMainActivity();
        }
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, loginResult.toString());
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.v(TAG, response.toString());
                                /*try {
                                    JSONArray data = object.getJSONObject("accounts").getJSONArray("data");

                                    for (int i = 0; i < 1; ++i) {
                                        String pageId = data.getJSONObject(i).getString("id");
                                        String name = data.getJSONObject(i).getString("name");
                                        new PagePreference(FBLoginActivity.this).setPageId(pageId);
                                        new PagePreference(FBLoginActivity.this).setName(name);
                                    }
                                }
                                catch(JSONException jsone) {
                                    jsone.printStackTrace();
                                }*/
                                startMainActivity();
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "accounts");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "Facebook login cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook login error");
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(FBLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
