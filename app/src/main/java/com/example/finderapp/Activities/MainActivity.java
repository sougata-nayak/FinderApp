package com.example.finderapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finderapp.Models.Chatroom;
import com.example.finderapp.Adapters.ChatroomRecyclerAdapter;
import com.example.finderapp.R;
import com.example.finderapp.Models.User;
import com.example.finderapp.Models.UserClient;
import com.example.finderapp.Models.UserLocation;
import com.example.finderapp.services.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.example.finderapp.util.Constants.ERROR_DIALOG_REQUEST;
import static com.example.finderapp.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.finderapp.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatroomRecyclerView;
    private ProgressBar progressBar;

    private FirebaseFirestore database;
    private ListenerRegistration chatroomEventListener;

    private ArrayList<Chatroom> chatroomsArrayList = new ArrayList<>();
    private Set<String> chatroomIdsList = new HashSet<>();

    private ChatroomRecyclerAdapter chatroomRecyclerAdapter;
    private RecyclerView recyclerView;

    private static final String TAG = "MainActivity";

    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation userLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.chatrooms_recycler_view);

        database = FirebaseFirestore.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initSupportActionBar();
        initChatroomRecyclerView();

        findViewById(R.id.fab_create_chatroom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateChatroomDialog();
            }
        });
    }

    private void initChatroomRecyclerView() {
        chatroomRecyclerAdapter = new ChatroomRecyclerAdapter(chatroomsArrayList);
        chatroomRecyclerView.setAdapter(chatroomRecyclerAdapter);
        chatroomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void getUserDetails(){

        if(userLocation == null){
            userLocation = new UserLocation();

            DocumentReference userRef = database
                    .collection("Users")
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        Log.d(TAG, "onComplete: Received user details");

                        User user = task.getResult().toObject(User.class);

                        ((UserClient)(getApplicationContext())).setUser(user);
                        //This is done to set the user in the user client and use in anywhere

                        userLocation.setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else {
            getLastKnownLocation();
        }

    }


    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            //this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }


    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    private void getLastKnownLocation(){
        Log.d(TAG, "getLastKnownLocation: getting location");

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){

                    Location location = task.getResult();

                    if(location != null){

                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                        userLocation.setGeoPoint(geoPoint);
                        userLocation.setTimestamp(null);
                        saveUserLocation();
                        startLocationService();
                    }
                }
            }
        });
    }


    private void saveUserLocation(){
        if(userLocation != null){

            DocumentReference locationRef = database.collection("UserLocations")
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: " + userLocation.getGeoPoint().getLatitude() + "  " +
                                userLocation.getGeoPoint().getLongitude());

                    }
                }
            });
        }
    }



    private void getChatrooms(){

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        database.setFirestoreSettings(settings);

        CollectionReference chatroomsCollection = database.collection("Chatrooms");

        chatroomEventListener = chatroomsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called.");

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){

                        Chatroom chatroom = doc.toObject(Chatroom.class);

                        if(!chatroomIdsList.contains(chatroom.getChatroom_id())){
                            chatroomIdsList.add(chatroom.getChatroom_id());
                            chatroomsArrayList.add(chatroom);
                        }
                    }
                    Log.d(TAG, "onEvent: number of chatrooms: " + chatroomsArrayList.size());
                    chatroomRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void initSupportActionBar() {
        setTitle("Chatrooms");
    }


    private void CreateChatroomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new chatroom");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!input.getText().toString().equals("")){

                    BuildNewChatroom(input.getText().toString());
                }
                else{
                    input.setError("Please enter a name");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();
    }


    private void BuildNewChatroom(String chatroomName) {

        showDialog();
        final Chatroom chatroom = new Chatroom();
        chatroom.setTitle(chatroomName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        database.setFirestoreSettings(settings);

        DocumentReference newChatroomRef = database.collection("Chatrooms").document();

        chatroom.setChatroom_id(newChatroomRef.getId());

        newChatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                hideDialog();

                if(task.isSuccessful()){
                    navChatroomActivity(chatroom);
                }
                else{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void navChatroomActivity(Chatroom chatroom) {
        Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
        intent.putExtra("intent_chatroom", chatroom);
        startActivity(intent);
    }


    private void hideDialog() {
        progressBar.setVisibility(View.GONE);
    }


    private void showDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_sign_out:
                signOut();
                return true;
            case R.id.action_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatroomEventListener != null){
            chatroomEventListener.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getChatrooms();
                getUserDetails();
            }
            else{
                getLocationPermission();
            }
        }
    }


    /*** The below code if for asking user permission for their location ***/

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        //This is responsible for checking that gps is enabled for this app or not
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {

            buildAlertMessageNoGps(); //If gps not enables this method is called
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getChatrooms();
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){

        //This method is responsible for checking is google maps services is enabled and if not it helps th user enable it
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getChatrooms();
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
}
