package com.suvamjain.blockcalls.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suvamjain.blockcalls.R;
import com.suvamjain.blockcalls.model.Contact;
import com.suvamjain.blockcalls.util.ContactDiffUtil;

import java.util.List;

public class BlockedContactListAdapter extends RecyclerView.Adapter<BlockedContactListAdapter.ContactViewHolder> {

    private List<Contact> contacts;

    public BlockedContactListAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, null);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = getItem(position);

        holder.itemName.setText(contact.getName());
        holder.itemNumber.setText(contact.getNumber());
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public Contact getItem(int position) {
        return contacts.get(position);
    }

    public void addContacts(List<Contact> newContact) {
        ContactDiffUtil contactDiffUtil = new ContactDiffUtil(contacts, newContact);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(contactDiffUtil);
        contacts.clear();
        contacts.addAll(newContact);
        diffResult.dispatchUpdatesTo(this);
    }

    protected class ContactViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName, itemNumber;

        public ContactViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemNumber = itemView.findViewById(R.id.item_number);
        }
    }
}
