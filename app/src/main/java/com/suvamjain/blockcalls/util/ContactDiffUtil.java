package com.suvamjain.blockcalls.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.suvamjain.blockcalls.model.Contact;

import java.util.List;

public class ContactDiffUtil extends DiffUtil.Callback {

    List<Contact> oldContactList;
    List<Contact> newContactList;

    public ContactDiffUtil(List<Contact> oldContactList, List<Contact> newContactList) {
        this.oldContactList = oldContactList;
        this.newContactList = newContactList;
    }

    @Override
    public int getOldListSize() {
        return oldContactList.size();
    }

    @Override
    public int getNewListSize() {
        return newContactList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldContactList.get(oldItemPosition).getId() == newContactList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldContactList.get(oldItemPosition).equals(newContactList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
