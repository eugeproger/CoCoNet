package com.eugeproger.coconet.tabs.group;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.eugeproger.coconet.R;
import com.eugeproger.coconet.support.Constant;
import com.eugeproger.coconet.support.DatabaseRealtimeFolderName;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groups = new ArrayList<>();
    private DatabaseReference groupReference;
    private FirebaseDatabase firebaseDatabase;

    public GroupsFragment() {
        // Required empty public constructor
    }

    private void initializeElements() {
        listView = groupFragmentView.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groups);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);


        firebaseDatabase = FirebaseDatabase.getInstance(Constant.REALTIME_DATABASE_LINK);
        groupReference = firebaseDatabase.getReference().child(DatabaseRealtimeFolderName.GROUPS);

        initializeElements();

        retrieveAndDisplayGroups();

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String currentGroupName = adapterView.getItemAtPosition(position).toString();

            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra(Constant.GROUP_NAME, currentGroupName);
            startActivity(groupChatIntent);
        });

        return groupFragmentView;
    }

    private void retrieveAndDisplayGroups() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();

                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                groups.clear();
                groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
