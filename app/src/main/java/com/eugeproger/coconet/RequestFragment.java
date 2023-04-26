package com.eugeproger.coconet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RequestFragment extends Fragment {

    private View requestsFragment;
    private RecyclerView mRequestList;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragment = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestList = requestsFragment.findViewById(R.id.chat_request_RecyclerView_frag_request);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  requestsFragment;
    }
}