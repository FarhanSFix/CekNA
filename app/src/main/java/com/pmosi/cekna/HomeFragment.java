package com.pmosi.cekna;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    EditText edtMatkul, edtSemester, bobotPresensi, bobotTugas, bobotUts, bobotUas;
    EditText nilaiPresensi, nilaiTugas, nilaiUts, nilaiUas;
    TextView tvHasil;
    CardView cardHasil;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi Firebase Database dan Auth
        dbRef = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("nilai");
        mAuth = FirebaseAuth.getInstance();

        // View Initialization
        edtMatkul = view.findViewById(R.id.edtMatkul);
        edtSemester = view.findViewById(R.id.edtSemester);
        bobotPresensi = view.findViewById(R.id.bobotPresensi);
        bobotTugas = view.findViewById(R.id.bobotTugas);
        bobotUts = view.findViewById(R.id.bobotUts);
        bobotUas = view.findViewById(R.id.bobotUas);
        nilaiPresensi = view.findViewById(R.id.nilaiPresensi);
        nilaiTugas = view.findViewById(R.id.nilaiTugas);
        nilaiUts = view.findViewById(R.id.nilaiUts);
        nilaiUas = view.findViewById(R.id.nilaiUas);
        tvHasil = view.findViewById(R.id.tvHasil);
        cardHasil = view.findViewById(R.id.cardHasil);

        Button btnHitung = view.findViewById(R.id.btnHitungSimpan);
        btnHitung.setOnClickListener(v -> hitungDanSimpan());

        return view;
    }

    private void hitungDanSimpan() {
        String matkul = edtMatkul.getText().toString().trim();
        String semester = edtSemester.getText().toString().trim();

        if (matkul.isEmpty() || semester.isEmpty() ||
                bobotPresensi.getText().toString().isEmpty() ||
                bobotTugas.getText().toString().isEmpty() ||
                bobotUts.getText().toString().isEmpty() ||
                bobotUas.getText().toString().isEmpty() ||
                nilaiPresensi.getText().toString().isEmpty() ||
                nilaiTugas.getText().toString().isEmpty() ||
                nilaiUts.getText().toString().isEmpty() ||
                nilaiUas.getText().toString().isEmpty()) {

            tvHasil.setText("Harap isi semua field terlebih dahulu!");
            cardHasil.setVisibility(View.VISIBLE);
            return;
        }

        try {
            double bPresensi = Double.parseDouble(bobotPresensi.getText().toString());
            double bTugas = Double.parseDouble(bobotTugas.getText().toString());
            double bUts = Double.parseDouble(bobotUts.getText().toString());
            double bUas = Double.parseDouble(bobotUas.getText().toString());

            double nPresensi = Double.parseDouble(nilaiPresensi.getText().toString());
            double nTugas = Double.parseDouble(nilaiTugas.getText().toString());
            double nUts = Double.parseDouble(nilaiUts.getText().toString());
            double nUas = Double.parseDouble(nilaiUas.getText().toString());

            double total = (bPresensi * nPresensi + bTugas * nTugas + bUts * nUts + bUas * nUas) / 100;
            String huruf = konversiHuruf(total);
            double bobot = konversiBobot(huruf);

            String hasil = "Nilai Akhir: " + total + "\nNilai Huruf: " + huruf + "\nBobot: " + bobot;
            tvHasil.setText(hasil);
            cardHasil.setVisibility(View.VISIBLE);

            // Cek apakah user sedang login
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                tvHasil.setText("Gagal menyimpan: user belum login.");
                cardHasil.setVisibility(View.VISIBLE);
                return;
            }

            String userId = currentUser.getUid(); // Ambil UID user login
            String id = dbRef.push().getKey();    // ID data baru

            Map<String, Object> nilaiMap = new HashMap<>();
            nilaiMap.put("matkul", matkul);
            nilaiMap.put("semester", semester);
            nilaiMap.put("bobot_presensi", bPresensi);
            nilaiMap.put("bobot_tugas", bTugas);
            nilaiMap.put("bobot_uts", bUts);
            nilaiMap.put("bobot_uas", bUas);
            nilaiMap.put("nilai_presensi", nPresensi);
            nilaiMap.put("nilai_tugas", nTugas);
            nilaiMap.put("nilai_uts", nUts);
            nilaiMap.put("nilai_uas", nUas);
            nilaiMap.put("nilai_akhir", total);
            nilaiMap.put("huruf", huruf);
            nilaiMap.put("bobot", bobot);
            nilaiMap.put("user_id", userId); // Simpan UID user

            dbRef.child(id).setValue(nilaiMap);

        } catch (NumberFormatException e) {
            tvHasil.setText("Format angka tidak valid. Pastikan semua angka diisi dengan benar.");
            cardHasil.setVisibility(View.VISIBLE);
        }
    }

    private String konversiHuruf(double nilai) {
        if (nilai >= 85) return "A";
        else if (nilai >= 80) return "A-";
        else if (nilai >= 75) return "B+";
        else if (nilai >= 70) return "B";
        else if (nilai >= 65) return "B-";
        else if (nilai >= 60) return "C+";
        else if (nilai >= 55) return "C";
        else if (nilai >= 50) return "C-";
        else if (nilai >= 45) return "D";
        else return "E";
    }

    private double konversiBobot(String huruf) {
        switch (huruf) {
            case "A": return 4.0;
            case "A-": return 3.75;
            case "B+": return 3.5;
            case "B": return 3.0;
            case "B-": return 2.75;
            case "C+": return 2.5;
            case "C": return 2.0;
            case "C-": return 1.75;
            case "D": return 1.0;
            default: return 0.0;
        }
    }
}
