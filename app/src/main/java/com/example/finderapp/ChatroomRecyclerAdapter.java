package com.example.finderapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatroomRecyclerAdapter extends RecyclerView.Adapter<ChatroomRecyclerAdapter.ViewHolder> {

    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Context context;


    public ChatroomRecyclerAdapter(ArrayList<Chatroom> mChatrooms) {
        this.mChatrooms = mChatrooms;
    }

    public ChatroomRecyclerAdapter(ArrayList<Chatroom> mChatrooms, Context context) {
        this.mChatrooms = mChatrooms;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chatroom_item_list, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatroomRecyclerAdapter.ViewHolder holder, final int position) {

        ((ViewHolder)holder).chatroomTitle.setText(mChatrooms.get(position).getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context.getApplicationContext(), mChatrooms.get(position).toString() , Toast.LENGTH_SHORT).show();

                Chatroom chatroom = new Chatroom();
                chatroom.setTitle(mChatrooms.get(position).getTitle());
                chatroom.setChatroom_id(mChatrooms.get(position).getChatroom_id());

                Intent intent = new Intent(context.getApplicationContext(), ChatroomActivity.class);
                intent.putExtra("intent_chatroom", chatroom);
                context.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mChatrooms.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView chatroomTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatroomTitle = itemView.findViewById(R.id.chatroom_title);
        }
    }


}
