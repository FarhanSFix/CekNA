package com.pmosi.cekna;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    ListView listNilai;
    ArrayList<HashMap<String, String>> nilaiList = new ArrayList<>();
    DatabaseReference dbRef;
    Context context;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        context = requireContext();

        listNilai = view.findViewById(R.id.listNilai);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance("https://ceknilai-83711-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("nilai");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("HistoryFragment", "User belum login");
            return view;
        }

        String userId = currentUser.getUid();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nilaiList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        String dataUserId = data.child("user_id").getValue(String.class);
                        if (dataUserId != null && dataUserId.equals(userId)) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("id", data.getKey());
                            map.put("matkul", data.child("matkul").getValue(String.class));
                            map.put("semester", data.child("semester").getValue(String.class));

                            Double nilaiAkhir = data.child("nilai_akhir").getValue(Double.class);
                            map.put("hasil", nilaiAkhir != null ? String.format("%.2f", nilaiAkhir) : "0.0");

                            map.put("huruf", data.child("huruf").getValue(String.class));

                            nilaiList.add(map);
                        }
                    } catch (Exception e) {
                        Log.e("HistoryFragment", "Error parsing data: " + e.getMessage());
                    }
                }

                NilaiAdapter adapter = new NilaiAdapter(context, nilaiList);
                listNilai.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HistoryFragment", "Database error: " + error.getMessage());
            }
        });

        listNilai.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getActivity(), DetailNilaiActivity.class);
            intent.putExtra("id", nilaiList.get(position).get("id"));
            startActivity(intent);
        });

        return view;
    }

    class NilaiAdapter extends BaseAdapter {
        Context context;
        ArrayList<HashMap<String, String>> list;

        public NilaiAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_nilai, parent, false);

            TextView tvHuruf = view.findViewById(R.id.tvHuruf);
            TextView tvMatkul = view.findViewById(R.id.tvMatkul);
            TextView tvSemester = view.findViewById(R.id.tvSemester);
            TextView tvAngka = view.findViewById(R.id.tvAngka);

            HashMap<String, String> item = list.get(position);

            tvHuruf.setText(item.get("huruf"));
            tvMatkul.setText(item.get("matkul"));
            tvSemester.setText("Semester " + item.get("semester"));
            tvAngka.setText(item.get("hasil"));

            return view;
        }
    }
}
