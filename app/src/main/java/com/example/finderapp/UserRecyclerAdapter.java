package com.example.finderapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private ArrayList<User> usersList = new ArrayList<>();
    private UserListRecyclerClickListener mClickListener;

    public UserRecyclerAdapter(ArrayList<User> usersList, UserListRecyclerClickListener clickListener) {
        this.usersList = usersList;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_list_item, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ((ViewHolder)holder).email.setText(usersList.get(position).getEmail());
        ((ViewHolder)holder).username.setText(usersList.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView email, username;
        UserListRecyclerClickListener mClickListener;

        public ViewHolder(@NonNull View itemView, UserListRecyclerClickListener clickListener) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.mEmail);

            mClickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onUserClicked(getAdapterPosition());
        }
    }

    public interface UserListRecyclerClickListener{

        void onUserClicked(int position);
    }
}
