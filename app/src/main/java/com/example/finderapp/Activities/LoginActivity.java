package com.example.finderapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finderapp.Models.User;
import com.example.finderapp.R;
import com.example.finderapp.Models.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressBar progressBar;

    private FirebaseAuth.AuthStateListener authStateListener;

    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);

        SetupFirebaseAuth();

        findViewById(R.id.link_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Log.d(TAG, "onClick: attempting to authenticate.");
        if(email.getText().toString().equals("") || password.getText().toString().equals("")){

            Toast.makeText(LoginActivity.this, "You didn't fill out the forms", Toast.LENGTH_SHORT).show();

        }
        else{
            showDialog();

            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            hideDialog();
                            if(task.isCanceled()){
                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SetupFirebaseAuth() {

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "Authenticated with" + user.getEmail(), Toast.LENGTH_SHORT).show();

                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    database.setFirestoreSettings(settings);

                    DocumentReference userRef = database.collection("Users").document(user.getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: successfully set the user client.");
                                //This part will pass one java class to another using getApplication context

                                User user = task.getResult().toObject(User.class);
                                ((UserClient)(getApplicationContext())).setUser(user);
                                //This is done to set the user in the user client and use in anywhere
                            }
                        }
                    });

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else{
                    //User signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }


    private void hideDialog() {
        progressBar.setVisibility(View.GONE);
    }

    private void showDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if(authStateListener!=null){
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }
}
