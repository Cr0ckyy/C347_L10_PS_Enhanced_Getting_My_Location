package com.myapplicationdev.c347_l10_psenhancedgettingmylocation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    Button btnRefresh;
    TextView tvRecords;
    ListView lvRecords;
    ArrayList<String> al;
    ArrayAdapter<String> aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        btnRefresh = findViewById(R.id.btnRefresh);
        tvRecords = findViewById(R.id.tvRecords);
        lvRecords = findViewById(R.id.lvRecords);

        btnRefresh.setOnClickListener(view -> getLatLng());
    }

    @SuppressLint("SetTextI18n")
    private void getLatLng() {

        al = new ArrayList<>();
        String folderLocation = getFilesDir().getAbsolutePath() + "/Folder";
        File targetFile = new File(folderLocation, "data.txt");


        if (targetFile.exists()) {
            try {

                FileReader reader = new FileReader(targetFile);
                BufferedReader br = new BufferedReader(reader);
                String line = br.readLine();

                while (line != null) {
                    al.add(line);
                    line = br.readLine();
                }

                br.close();
                reader.close();

                aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, al);
                lvRecords.setAdapter(aa);
                tvRecords.setText("Number of records: " + al.size());

            } catch (Exception e) {
                Toast.makeText(RecordsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}