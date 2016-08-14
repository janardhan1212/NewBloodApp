package com.bloodtolife.bloodapp.Main_screen;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bloodtolife.bloodapp.Helpers.SQLiteHandler;
import com.bloodtolife.bloodapp.R;
import com.bloodtolife.bloodapp.adapter.PostViewHolder;
import com.bloodtolife.bloodapp.pojos.Post;
import com.bloodtolife.bloodapp.utils.FirebaseUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {

    public static final String TAG = "PostsFragment";
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    private int mRecyclerViewPosition = 0;
    private OnPostSelectedListener mListener;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<PostViewHolder> mAdapter;
    private SQLiteHandler db;
    private HashMap<String, String> user_;

    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        db = new SQLiteHandler(getActivity());
        user_ = db.getUserDetails();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

      /*  if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
            // TODO: RecyclerView only restores position properly for some tabs.
        }
        Log.d(TAG, "Restoring recycler view position (all): " + mRecyclerViewPosition);*/
        Query allPostsQuery = FirebaseUtil.getPostsRef();
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // TODO: Refresh feed view.
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private FirebaseRecyclerAdapter<Post, PostViewHolder> getFirebaseRecyclerAdapter(Query query) {
        return new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class, R.layout.request_feed, PostViewHolder.class, query) {
            @Override
            public void populateViewHolder(final PostViewHolder postViewHolder,
                                           final Post post, final int position) {
                setupPost(postViewHolder, post, position, null);
            }

            @Override
            public void onViewRecycled(PostViewHolder holder) {
                super.onViewRecycled(holder);
//                FirebaseUtil.getLikesRef().child(holder.mPostKey).removeEventListener(holder.mLikeListener);
            }
        };
    }

    private void setupPost(final PostViewHolder postViewHolder, final Post post, final int position, final String inPostKey) {
        final Map<String, Object> postValues = post.toMap();
        String loc = postValues.get("city") + "," + postValues.get("state") + "," + postValues.get("hospital_address");
        postViewHolder.setText(postValues.get("name").toString(), loc, postValues.get("phone_number").toString(), postValues.get("blood_group").toString(), "hi");
        // postViewHolder.setTimestamp(DateUtils.getRelativeTimeSpanString((long) postValues.get("time_stamp")).toString());
        final String postKey;
        final String share_text = "I need " + postValues.get("blood_group") + "blood group urgently at " + loc + "," + "/n ~ " + postValues.get("name") + "," + postValues.get("phone_number").toString();
        if (mAdapter instanceof FirebaseRecyclerAdapter) {
            postKey = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
        } else {
            postKey = inPostKey;
        }


postViewHolder.setTimestamp(DateUtils.getRelativeTimeSpanString(
        Long.parseLong(postValues.get("time_stamp").toString())).toString());
        postViewHolder.setIcon(user_.get("profile_pic_url"), user_.get("uid"));
        postViewHolder.setPostClickListener(new PostViewHolder.PostClickListener() {
            @Override
            public void showComments() {
                Log.d(TAG, "Comment position: " + position);
                mListener.onPostComment(postKey);
            }

            @Override
            public void share_post() {
                Log.d(TAG, "Share position: " + position);
                mListener.onPostShare(share_text);
            }

            @Override
            public void call_post() {
                Log.d(TAG, "Call position: " + position);
               String val= "tel:+91"+postValues.get("phone_number");
                mListener.onPostCall(val);

            }

        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPostSelectedListener) {
            mListener = (OnPostSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPostSelectedListener");
        }
    }
    // Method to share either text or URL.

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnPostSelectedListener {
        void onPostComment(String postKey);

        void onPostShare(String postKey);

        void onPostCall(String postKey);
    }
}
