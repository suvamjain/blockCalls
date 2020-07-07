package com.suvamjain.blockcalls.adapter;

import android.content.Context;
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
    private Context context;

    public BlockedContactListAdapter(Context context, List<Contact> contacts) {
        this.contacts = contacts;
        this.context = context;
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
        holder.itemCount.setText(context.getResources().getString(R.string.call_count, contact.getCalls()));
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

        private TextView itemName, itemNumber, itemCount;

        public ContactViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemNumber = itemView.findViewById(R.id.item_number);
            itemCount = itemView.findViewById(R.id.item_count);
        }
    }
}
