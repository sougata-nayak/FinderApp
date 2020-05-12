package com.example.finderapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finderapp.R;
import com.example.finderapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, confirmPassword;
    private ProgressBar progressBar;

    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText) findViewById(R.id.input_email);
        password = (EditText) findViewById(R.id.input_password);
        confirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        database = FirebaseFirestore.getInstance();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isEmpty(email.getText().toString())
                        && !isEmpty(password.getText().toString())
                        && !isEmpty(confirmPassword.getText().toString())){

                    //check if passwords match
                    if(password.getText().toString().equals(confirmPassword.getText().toString())){

                        //Initiate registration task
                        registerNewEmail(email.getText().toString(), password.getText().toString());
                    }else{
                        Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerNewEmail(final String email, String password) {
        showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    User user = new User();
                    user.setEmail(email);
                    user.setUsername(email.substring(0, email.indexOf("@")));
                    user.setUser_id(FirebaseAuth.getInstance().getUid());

                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    database.setFirestoreSettings(settings);

                    DocumentReference newUserRef = database.collection("Users")
                            .document(FirebaseAuth.getInstance().getUid());

                    newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            hideDialog();

                            if(task.isSuccessful()){
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                            else{
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Couldn't register user", Snackbar.LENGTH_SHORT).show();
                    hideDialog();
                }
            }
        });
    }


    private void hideDialog() {
        progressBar.setVisibility(View.GONE);
    }

    private void showDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }
}
