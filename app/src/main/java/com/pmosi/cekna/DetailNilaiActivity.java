package com.pmosi.cekna;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DetailNilaiActivity extends AppCompatActivity {

    EditText edtMatkul, edtSemester, edtBobotPresensi, edtBobotTugas, edtBobotUts, edtBobotUas;
    EditText edtNilaiPresensi, edtNilaiTugas, edtNilaiUts, edtNilaiUas;
    Button btnUpdate, btnHapus;
    DatabaseReference db;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_nilai);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        // Inisialisasi view
        edtMatkul = findViewById(R.id.edtMatkul);
        edtSemester = findViewById(R.id.edtSemester);
        edtBobotPresensi = findViewById(R.id.bobotPresensi);
        edtBobotTugas = findViewById(R.id.bobotTugas);
        edtBobotUts = findViewById(R.id.bobotUts);
        edtBobotUas = findViewById(R.id.bobotUas);
        edtNilaiPresensi = findViewById(R.id.nilaiPresensi);
        edtNilaiTugas = findViewById(R.id.nilaiTugas);
        edtNilaiUts = findViewById(R.id.nilaiUts);
        edtNilaiUas = findViewById(R.id.nilaiUas);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnHapus = findViewById(R.id.btnHapus);

        // Ambil ID dari intent
        id = getIntent().getStringExtra("id");
        db = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("nilai").child(id);

        // Ambil data dari Firebase dan isi ke field
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                edtMatkul.setText(snapshot.child("matkul").getValue(String.class));
                edtSemester.setText(snapshot.child("semester").getValue(String.class));
                edtBobotPresensi.setText(String.valueOf(snapshot.child("bobot_presensi").getValue(Double.class)));
                edtBobotTugas.setText(String.valueOf(snapshot.child("bobot_tugas").getValue(Double.class)));
                edtBobotUts.setText(String.valueOf(snapshot.child("bobot_uts").getValue(Double.class)));
                edtBobotUas.setText(String.valueOf(snapshot.child("bobot_uas").getValue(Double.class)));
                edtNilaiPresensi.setText(String.valueOf(snapshot.child("nilai_presensi").getValue(Double.class)));
                edtNilaiTugas.setText(String.valueOf(snapshot.child("nilai_tugas").getValue(Double.class)));
                edtNilaiUts.setText(String.valueOf(snapshot.child("nilai_uts").getValue(Double.class)));
                edtNilaiUas.setText(String.valueOf(snapshot.child("nilai_uas").getValue(Double.class)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailNilaiActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Update
        btnUpdate.setOnClickListener(v -> {
            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("matkul", edtMatkul.getText().toString());
            updateMap.put("semester", edtSemester.getText().toString());
            updateMap.put("bobot_presensi", Double.parseDouble(edtBobotPresensi.getText().toString()));
            updateMap.put("bobot_tugas", Double.parseDouble(edtBobotTugas.getText().toString()));
            updateMap.put("bobot_uts", Double.parseDouble(edtBobotUts.getText().toString()));
            updateMap.put("bobot_uas", Double.parseDouble(edtBobotUas.getText().toString()));
            updateMap.put("nilai_presensi", Double.parseDouble(edtNilaiPresensi.getText().toString()));
            updateMap.put("nilai_tugas", Double.parseDouble(edtNilaiTugas.getText().toString()));
            updateMap.put("nilai_uts", Double.parseDouble(edtNilaiUts.getText().toString()));
            updateMap.put("nilai_uas", Double.parseDouble(edtNilaiUas.getText().toString()));

            double hasil = hitungNilai(updateMap);
            updateMap.put("nilai_akhir", hasil);
            updateMap.put("huruf", konversiNilai(hasil));

            db.updateChildren(updateMap).addOnSuccessListener(unused -> {
                Toast.makeText(DetailNilaiActivity.this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(DetailNilaiActivity.this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show();
            });
        });

        // Tombol Hapus
        btnHapus.setOnClickListener(v -> {
            db.removeValue().addOnSuccessListener(unused -> {
                Toast.makeText(DetailNilaiActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(DetailNilaiActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // Fungsi hitung nilai akhir
    private double hitungNilai(HashMap<String, Object> map) {
        double bPresensi = Double.parseDouble(map.get("bobot_presensi").toString());
        double bTugas = Double.parseDouble(map.get("bobot_tugas").toString());
        double bUts = Double.parseDouble(map.get("bobot_uts").toString());
        double bUas = Double.parseDouble(map.get("bobot_uas").toString());

        double nPresensi = Double.parseDouble(map.get("nilai_presensi").toString());
        double nTugas = Double.parseDouble(map.get("nilai_tugas").toString());
        double nUts = Double.parseDouble(map.get("nilai_uts").toString());
        double nUas = Double.parseDouble(map.get("nilai_uas").toString());

        return (bPresensi * nPresensi + bTugas * nTugas + bUts * nUts + bUas * nUas) / 100;
    }

    // Fungsi konversi nilai ke huruf
    private String konversiNilai(double nilai) {
        if (nilai >= 85) return "A";
        else if (nilai >= 80) return "A-";
        else if (nilai >= 75) return "B+";
        else if (nilai >= 70) return "B";
        else if (nilai >= 65) return "B-";
        else if (nilai >= 60) return "C+";
        else if (nilai >= 55) return "C";
        else if (nilai >= 50) return "D";
        else return "E";
    }
}
