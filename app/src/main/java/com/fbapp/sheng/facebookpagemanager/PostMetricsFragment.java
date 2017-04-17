package com.fbapp.sheng.facebookpagemanager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.fbapp.sheng.facebookpagemanager.model.PagePreference;
import com.fbapp.sheng.facebookpagemanager.model.PostsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostMetricsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostMetricsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostMetricsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private String postId;
    private EditText impressionText;
    private EditText reachText;
    private EditText likesText;
    private String TAG = "PostMetricsFragment";

    public PostMetricsFragment() {
        // Required empty public constructor
    }

    public static PostMetricsFragment newInstance(String param1, String param2) {
        PostMetricsFragment fragment = new PostMetricsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        postId = getArguments().getString("post_id");
        View view = inflater.inflate(R.layout.fragment_post_metrics, container, false);
        Spinner periodSpinner = (Spinner) view.findViewById(R.id.period_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.period_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);

        impressionText = (EditText) view.findViewById(R.id.impression_number);
        reachText = (EditText) view.findViewById(R.id.reach_number);
        likesText = (EditText) view.findViewById(R.id.likes_number);
        loadPostMetrics();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void loadPostMetrics() {
        //StringBuilder metricRequestString = new StringBuilder("?metric={page_impressions, page_impressions_unique}");
        Bundle args = new Bundle();
        args.putBoolean("summary", true);
        args.putString("access_token", new PagePreference(getActivity()).getPageAccessToken());
        GraphRequest likesRequest = new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/" + postId + "/likes",
                args,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.v(TAG, response.toString());
                            JSONObject arr = response.getJSONObject();
                            String count = arr.getJSONObject("summary").getString("total_count");
                            likesText.setText(count);
                        }
                        catch (JSONException jsone) {
                            jsone.printStackTrace();
                        }
                    }
                }
        );
        Bundle metricsArgs = new Bundle();
        metricsArgs.putString("metric", "page_impressions");
        //metricsArgs.putString("access_token", new PagePreference(getActivity()).getPageAccessToken());
        GraphRequest viewsRequest = new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/" + postId + "/insights",
                metricsArgs,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.v(TAG, response.toString());
                            JSONObject arr = response.getJSONObject().getJSONObject("");
                        }
                        catch (JSONException jsone) {
                            jsone.printStackTrace();
                        }
                    }
                }
        );
        likesRequest.executeAsync();
        viewsRequest.executeAsync();
    }
}