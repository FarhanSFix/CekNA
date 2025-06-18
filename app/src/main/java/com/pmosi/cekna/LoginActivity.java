package com.pmosi.cekna;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText edtIdentifier, edtPassword;
    Button btnLogin;

    TextView btnRegister, btnReset;
    FirebaseAuth auth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        edtIdentifier = findViewById(R.id.edtIdentifier);
        EditText edtPassword = findViewById(R.id.edtPassword);
        ImageView ivTogglePassword = findViewById(R.id.togglePasswordVisibility);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnReset = findViewById(R.id.btnResetPassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off); // mata terbuka
            } else {
                // Hide password
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off); // mata tertutup
            }

            // Move cursor to end
            edtPassword.setSelection(edtPassword.length());
        });

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");


        // Tombol Register dan Reset Password
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnReset.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));

        // Tombol Login
        btnLogin.setOnClickListener(v -> {
            String identifier = edtIdentifier.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            if (identifier.contains("@")) {
                // Jika input mengandung @, anggap sebagai email
                loginWithEmail(identifier, password);
            } else {
                // Kalau tidak, cari email dari username
                findEmailFromUsername(identifier, password);
            }
        });
    }

    private void findEmailFromUsername(String username, String password) {
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String email = userSnap.child("email").getValue(String.class);
                                if (email != null) {
                                    loginWithEmail(email, password);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (user.isEmailVerified()) {
                                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Email belum diverifikasi", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            });
                        }
                    } else {
                        String errorMsg = "Login gagal";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}