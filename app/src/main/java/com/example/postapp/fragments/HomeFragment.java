package com.example.postapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;
import com.example.postapp.PostDetails;
import com.example.postapp.R;
import com.example.postapp.adptor.Adaptor;
import com.example.postapp.dataModel.Post;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements Adaptor.OnPostListener {
    View view;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Adaptor adaptor;
    List<Post> arrayList = new ArrayList<>();
    private AVLoadingIndicatorView loadingIndicatorView;
    DataQueryBuilder queryBuilder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.home_fragment, container, false);
        loadingIndicatorView = view.findViewById(R.id.avi);
        getData();

        return view;
    }

    private void setUpRecycleView() {
        recyclerView = view.findViewById(R.id.recycleView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adaptor = new Adaptor(getActivity(), arrayList, this);
        recyclerView.setAdapter(adaptor);

    }

    private void getData() {
        setUpRecycleView();

        queryBuilder = DataQueryBuilder.create();
        queryBuilder.setPageSize(100);
        queryBuilder.setSortBy("created DESC");
        Backendless.Data.of(Post.class).find(queryBuilder, new AsyncCallback<List<Post>>() {
            @Override
            public void handleResponse(List<Post> response) {
                loadingIndicatorView.smoothToHide();

                arrayList = response;
                adaptor.setData(arrayList);
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                Toast.makeText(getActivity(), fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        EventHandler<Post> rt = Backendless.Data.of(Post.class).rt();
        rt.addCreateListener(new AsyncCallback<Post>() {
            @Override
            public void handleResponse(Post response) {
                arrayList.add(response);
                adaptor.notifyDataSetChanged();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getActivity(), fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onPostClick(int position) {

        Intent intent = new Intent(getActivity().getApplicationContext(), PostDetails.class);
        Post post = arrayList.get(position);
        intent.putExtra("name", post.getNameUser());
        intent.putExtra("profile", post.getProfilePic());
        intent.putExtra("desc", post.getDescription());
        intent.putExtra("pic", post.getPhotoUrl());
        intent.putExtra("date", post.getDateUpload());
        startActivity(intent);
    }
}
