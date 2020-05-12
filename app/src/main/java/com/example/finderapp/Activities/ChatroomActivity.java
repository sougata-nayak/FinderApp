package com.example.finderapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.finderapp.Models.ChatMessage;
import com.example.finderapp.Adapters.ChatMessageRecyclerAdapter;
import com.example.finderapp.Models.Chatroom;
import com.example.finderapp.R;
import com.example.finderapp.Models.User;
import com.example.finderapp.Models.UserClient;
import com.example.finderapp.Fragments.UserListFragment;
import com.example.finderapp.Models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatroomActivity extends AppCompatActivity  {

    private static final String TAG = "ChatroomActivity";

    private EditText textMessage;
    private Chatroom thisChatroom;

    private RecyclerView chatMessageRecyclerView;
    private ListenerRegistration chatMessageEventListener, userListEventListener, userLocationEventListener;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;

    private ArrayList<ChatMessage> messagesList = new ArrayList<>(); //List of all the messages and its data
    private Set<String> messageIdsList = new HashSet<>(); //List of the message ids in string form
    private ArrayList<User> userList = new ArrayList<>(); //List of all users in chatroom
    private ArrayList<UserLocation> userLocations = new ArrayList<>();

    private UserListFragment userListFragment;

    private FirebaseFirestore database;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        //Initialize the views
        textMessage = findViewById(R.id.input_message);
        chatMessageRecyclerView = (RecyclerView) findViewById(R.id.chatmessage_recycler_view);

        database = FirebaseFirestore.getInstance(); //Initialize database


        findViewById(R.id.checkmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewMessage();
                //The send message button when pressed the above method is called to send the message
            }
        });


        getIncomingIntent(); //Receives the intent and does required processing

        initChatroomRecyclerView(); //Initializes the chatroom recycler view

        getChatroomUsers(); //Gets all the current users of the chatroom
    }



    private void getIncomingIntent() {

        //Gets the intent and receives extras

        if(getIntent().hasExtra("intent_chatroom")){

            thisChatroom = getIntent().getParcelableExtra("intent_chatroom"); //Now thisChatroom gets all info of the present chatroom

            setChatroomName();
            joinChatroom();
        }
    }

    private void setChatroomName() {

        //This method sets the name of the chatroom in the actionbar through thisChatroom title

        getSupportActionBar().setTitle(thisChatroom.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void joinChatroom() {


        DocumentReference joinChatroomRef = database.collection("Chatrooms")
                .document(thisChatroom.getChatroom_id())
                .collection("User_list")
                .document(FirebaseAuth.getInstance().getUid());

        User user = ((UserClient)getApplicationContext()).getUser();

        if(user != null){
            joinChatroomRef.set(user);
        }

    }

    private void getChatroomUsers() {

        CollectionReference userRef = database.collection("Chatrooms")
                .document(thisChatroom.getChatroom_id())
                .collection("User_list");

        userListEventListener = userRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){

                    // Clear the list and add all the users again
                    userList.clear();
                    userList = new ArrayList<>();


                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                        getUserLocation(user);
                    }

                    Log.d(TAG, "onEvent: user list size: " + userList.size());
                }
            }
        });
    }

    private void getUserLocation(User user) {
        CollectionReference locationRef = database.collection("UserLocations");

        userLocationEventListener = locationRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){

                    // Clear the list and add all the users again
                    userLocations.clear();
                    userLocations = new ArrayList<>();


                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserLocation user = doc.toObject(UserLocation.class);
                        userLocations.add(user);

                    }

                    Log.d(TAG, "onEvent: user list size: " + userList.size());
                }
            }
        });


        /*locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null){

                        userLocations.add(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });*/
    }


    private void initChatroomRecyclerView() {

        linearLayoutManager = new LinearLayoutManager(this);
        chatMessageRecyclerView.setLayoutManager(linearLayoutManager);

        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(messagesList, new ArrayList<User>(), getApplicationContext());
        chatMessageRecyclerView.setAdapter(mChatMessageRecyclerAdapter);


        chatMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    chatMessageRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(messagesList.size() > 0){
                                chatMessageRecyclerView.smoothScrollToPosition(
                                        chatMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });
    }

    private void insertNewMessage() {

        String message = textMessage.getText().toString().trim();

        if(!message.equals("")){

            DocumentReference newMessageDoc = database.collection("Chatrooms")
                    .document(thisChatroom.getChatroom_id())
                    .collection("Chat_messages")
                    .document();

            ChatMessage newChatMessage = new ChatMessage();
            newChatMessage.setMessage(message);
            newChatMessage.setMessage_id(newMessageDoc.getId());

            User user = ((UserClient)(getApplicationContext())).getUser();

            if(user != null){

                Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
                newChatMessage.setUser(user);
            }

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        clearMessage();
                    }
                    else{
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearMessage() {
        textMessage.setText("");
    }

    private void getChatMessages() {

        CollectionReference messagesRef = database
                .collection("Chatrooms")
                .document(thisChatroom.getChatroom_id())
                .collection("Chat_messages");


        chatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);

                                if(!messageIdsList.contains(message.getMessage_id())){

                                    messageIdsList.add(message.getMessage_id());
                                    messagesList.add(message);
                                    chatMessageRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                                }

                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void inflateUserListFragment(){
        hideSoftKeyboard();

        UserListFragment fragment = UserListFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("intent_user_list", userList);
        bundle.putParcelableArrayList("intent_user_location_list", userLocations);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void leaveChatroom(){

        DocumentReference joinChatroomRef = database
                .collection("Chatrooms")
                .document(thisChatroom.getChatroom_id())
                .collection("User_list")
                .document(FirebaseAuth.getInstance().getUid());

        joinChatroomRef.delete();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatMessageEventListener != null){
            chatMessageEventListener.remove();
        }
        if(userListEventListener != null){
            userListEventListener.remove();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                UserListFragment fragment =
                        (UserListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                if(fragment != null){
                    if(fragment.isVisible()){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }
            case R.id.action_chatroom_user_list:{
                inflateUserListFragment();
                return true;
            }
            case R.id.action_chatroom_leave:{
                leaveChatroom();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }
}