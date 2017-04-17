package com.fbapp.sheng.facebookpagemanager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fbapp.sheng.facebookpagemanager.PostsFragment.OnListFragmentInteractionListener;
import com.fbapp.sheng.facebookpagemanager.model.PageItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PageItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyPageItemsRecyclerViewAdapter extends RecyclerView.Adapter<MyPageItemsRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PageItem item);
    }

    private List<PageItem> mValues;
    private OnItemClickListener mListener;

    public MyPageItemsRecyclerViewAdapter(List<PageItem> items, OnItemClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.page_drawer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(mValues.size() != 0) {
            holder.mItem = mValues.get(position);
            holder.mNameView.setText(mValues.get(position).getName());
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onItemClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public PageItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.page_drawer_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
