package com.eugeproger.coconet.tabs.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.eugeproger.coconet.RequestFragment;
import com.eugeproger.coconet.tabs.chat.ChatsFragment;
import com.eugeproger.coconet.tabs.contact.ContactsFragment;
import com.eugeproger.coconet.tabs.group.GroupsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 1:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 2:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 3:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Contacts";
            case 1:
                return "Requests";
            case 2:
                return "Chats";
            case 3:
                return "Groups";
            default:
                return null;
        }
    }

}
