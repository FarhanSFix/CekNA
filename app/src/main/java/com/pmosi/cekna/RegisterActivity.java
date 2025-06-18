package com.pmosi.cekna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername, edtEmail, edtPassword;
    Button btnRegister;
    FirebaseAuth auth;
    TextView loginText;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        EditText edtPassword = findViewById(R.id.edtPassword);
        ImageView toggle = findViewById(R.id.togglePasswordVisibility);
        btnRegister = findViewById(R.id.btnRegister);
        loginText = findViewById(R.id.loginText);

        toggle.setOnClickListener(v -> {
            if (edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggle.setImageResource(R.drawable.ic_visibility_off); // ikon mata terbuka
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggle.setImageResource(R.drawable.ic_visibility_off); // ikon mata tertutup
            }
            edtPassword.setSelection(edtPassword.length()); // cursor tetap di akhir
        });

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");


        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                if (emailTask.isSuccessful()) {
                                    String uid = user.getUid();
                                    usersRef.child(uid).setValue(new User(username, email));

                                    Toast.makeText(this, "Registrasi berhasil. Cek email untuk verifikasi.", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        loginText.setOnClickListener(v ->  startActivity(new Intent(this, LoginActivity.class)));
    }

    public static class User {
        public String username, email;

        public User() {}
        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}