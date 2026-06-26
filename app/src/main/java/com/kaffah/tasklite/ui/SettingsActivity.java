package com.kaffah.tasklite.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.kaffah.tasklite.R;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        TextView title = findViewById(R.id.tvPlaceholderTitle);
        TextView message = findViewById(R.id.tvPlaceholderMessage);
        title.setText("Pengaturan");
        message.setText("Tema, backup, restore, hapus tugas selesai, dan izin pengingat disiapkan untuk fase implementasi berikutnya.");
    }
}
