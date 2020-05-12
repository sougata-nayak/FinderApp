package com.example.finderapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finderapp.Models.ChatMessage;
import com.example.finderapp.Models.User;
import com.example.finderapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatMessageRecyclerAdapter extends RecyclerView.Adapter<ChatMessageRecyclerAdapter.ViewHolder>{

    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private Context context;

    public ChatMessageRecyclerAdapter(ArrayList<ChatMessage> messages, ArrayList<User> users, Context context) {
        this.messages = messages;
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatMessageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_message_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageRecyclerAdapter.ViewHolder holder, int position) {

        if(FirebaseAuth.getInstance().getUid().equals(messages.get(position).getUser().getUser_id())){
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(context, R.color.green1));
        }
        else{
            ((ViewHolder) holder).username.setTextColor(ContextCompat.getColor(context, R.color.blue2));
        }

        ((ViewHolder)holder).username.setText(messages.get(position).getUser().getUsername());
        ((ViewHolder)holder).message.setText(messages.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView message, username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.chat_message_message);
            username = itemView.findViewById(R.id.chat_message_username);
        }
    }
}
