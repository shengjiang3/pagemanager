package com.fbapp.sheng.facebookpagemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.fbapp.sheng.facebookpagemanager.model.PostsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PostsFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "PostsFragment";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<PostsItem> postList;
    private RecyclerView recyclerView;
    private MyPostsRecyclerViewAdapter postAdapter;
    private TabLayout tab;
    private FloatingActionButton fab;
    private GraphResponse lastResponse;
    private String edge;
    private Button nextPostButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostsFragment() {
    }

    public static PostsFragment newInstance(int columnCount) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_list, container, false);
        postList = new ArrayList<PostsItem>(0);
        // Set the adapter
        Context context = view.getContext();
        if(getArguments() != null) {
            edge = getArguments().getString("edge");
        }
        else {
            edge = "promotable_posts";
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        postAdapter = new MyPostsRecyclerViewAdapter(postList, mListener);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(postAdapter);
        recyclerView.setOnScrollListener(mPostsListScrollListener);

        tab = (TabLayout) view.findViewById(R.id.tab_layout);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab1) {
                switch(tab1.getPosition()) {
                    case 0:
                        loadPagePosts(true);
                        break;
                    case 1:
                        loadPagePosts(false);
                        break;
                }
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab1) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab1) {
            }
        });

        nextPostButton = (Button) view.findViewById(R.id.button_next_page);
        nextPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view1) {
                getNextPagePosts();
            }
        });

        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Class fragmentClass = PublishPostsFragment.class;
                Fragment fragment = null;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lastResponse = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getActivity().getSharedPreferences("PagePreference", Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(mPreferenceChangedListener);
        loadPagePosts(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences pref = getActivity().getSharedPreferences("PagePreference", Context.MODE_PRIVATE);
        pref.unregisterOnSharedPreferenceChangeListener(mPreferenceChangedListener);
        postList.clear();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(PostsItem item);
    }

    private void loadPagePosts(final boolean is_published) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PagePreference", Context.MODE_PRIVATE);
        String pageAccessToken = sharedPreferences.getString("access_token", "none");
        String pageId = sharedPreferences.getString("page_id", "none");
        if(pageAccessToken != "none" && pageId != "none") {
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,message,from,to");
            parameters.putString("access_token", pageAccessToken);
            parameters.putBoolean("is_published", is_published);
            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/" + pageId + "/" + edge,
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            Log.v(TAG, response.toString());
                            try {
                                JSONObject object = response.getJSONObject();
                                postList.clear();
                                if(object != null) {
                                    JSONArray arr = object.getJSONArray("data");
                                    for (int i = 0; i < arr.length(); ++i) {
                                        String id = arr.getJSONObject(i).getString("id");
                                        String message = arr.getJSONObject(i).getString("message");
                                        String from = arr.getJSONObject(i).getJSONObject("from").getString("name");
                                        PostsItem temp = new PostsItem(id, message, from, is_published);
                                        postList.add(temp);
                                    }
                                    lastResponse = response;
                                }
                                postAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(postAdapter);
                            }
                            catch (JSONException jsone) {
                                Toast.makeText(getActivity(), "Error retrieving page data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            request.executeAsync();
        }
    }

    private void getNextPagePosts() {
        GraphRequest request = lastResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        if(request != null) {
            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.v(TAG, response.toString());
                    try {
                        JSONArray arr = response.getJSONObject().getJSONArray("data");
                        for(int i = 0; i < arr.length(); ++i) {
                            String id = arr.getJSONObject(i).getString("id");
                            String message = arr.getJSONObject(i).getString("message");
                            String from = arr.getJSONObject(i).getJSONObject("from").getString("name");
                            PostsItem temp = new PostsItem(id, message, from, tab.getSelectedTabPosition() == 0);
                            postList.add(temp);
                        }
                        postAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(postAdapter);
                    }
                    catch (JSONException jsone) {
                        Toast.makeText(getActivity(), "Error retrieving page data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            request.executeAsync();
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String pageId = sharedPreferences.getString("page_id", "none");
            String pageAccessToken = sharedPreferences.getString("access_token", "none");
            if(pageId != "none" && pageAccessToken != "none") {
                loadPagePosts(tab.getSelectedTabPosition() == 0);
            }
        }
    };

    private RecyclerView.OnScrollListener mPostsListScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(!recyclerView.canScrollVertically(-1) && dy < 0) {
                //loadPagePosts(tab.getSelectedTabPosition()==0);
            }
            else if(!recyclerView.canScrollVertically(1) && dy > 0) {
                //getNextPagePosts();
            }
        }
    };
}
