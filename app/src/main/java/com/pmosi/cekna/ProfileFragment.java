package com.pmosi.cekna;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private CircleImageView profileImage;
    private TextView tvName, tvEmail;
    private Uri imageUri;

    private String uid;
    private String base64Image = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            uid = user.getUid();
            tvEmail.setText(user.getEmail());

            userRef = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(uid);

            loadUserProfile();

            // Edit profile dialog
            profileImage.setOnClickListener(v -> showEditDialog());
            tvName.setOnClickListener(v -> showEditDialog());

            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });
        }

        return view;
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String base64 = snapshot.child("photo").getValue(String.class);

                tvName.setText(username != null ? username : "Pengguna");

                if (base64 != null && !base64.isEmpty()) {
                    byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                    Glide.with(requireContext())
                            .asBitmap()
                            .load(imageBytes)
                            .into(profileImage);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText edtUsername = dialogView.findViewById(R.id.edt_username);
        Button btnChangePhoto = dialogView.findViewById(R.id.btn_change_photo);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                edtUsername.setText(snapshot.getValue(String.class));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnChangePhoto.setOnClickListener(v -> showImagePickerOptions());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Profil")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newUsername = edtUsername.getText().toString().trim();
                    userRef.child("username").setValue(newUsername);

                    if (!base64Image.isEmpty()) {
                        userRef.child("photo").setValue(base64Image);
                    }

                    loadUserProfile();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showImagePickerOptions() {
        String[] options = {"Kamera", "Galeri"};
        new AlertDialog.Builder(getContext())
                .setTitle("Pilih Gambar")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null && requestCode == REQUEST_GALLERY) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), data.getData());
                base64Image = bitmapToBase64(bitmap);
                Glide.with(this).load(bitmap).into(profileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (data != null && requestCode == REQUEST_CAMERA) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            base64Image = bitmapToBase64(bitmap);
            Glide.with(this).load(bitmap).into(profileImage);
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
