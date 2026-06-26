package com.kaffah.tasklite.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.data.TaskRepository;
import com.kaffah.tasklite.util.BackupManager;
import com.kaffah.tasklite.util.PreferenceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends Activity {
    private static final int REQUEST_CREATE_BACKUP = 1201;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private PreferenceManager preferenceManager;
    private TaskRepository taskRepository;
    private BackupManager backupManager;
    private Spinner spTheme;
    private CheckBox cbShowCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferenceManager = new PreferenceManager(this);
        taskRepository = new TaskRepository(this);
        backupManager = new BackupManager(this);

        bindViews();
        setupThemeSpinner();
        setupActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_BACKUP && resultCode == RESULT_OK && data != null) {
            writeBackup(data.getData());
        }
    }

    private void bindViews() {
        spTheme = findViewById(R.id.spTheme);
        cbShowCompleted = findViewById(R.id.cbShowCompleted);
        cbShowCompleted.setChecked(preferenceManager.showCompletedInAll());
    }

    private void setupThemeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Ikuti sistem", "Terang", "Gelap"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTheme.setAdapter(adapter);

        String mode = preferenceManager.getThemeMode();
        if ("light".equals(mode)) {
            spTheme.setSelection(1);
        } else if ("dark".equals(mode)) {
            spTheme.setSelection(2);
        } else {
            spTheme.setSelection(0);
        }
    }

    private void setupActions() {
        cbShowCompleted.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferenceManager.setShowCompletedInAll(isChecked));

        spTheme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 1) {
                    preferenceManager.setThemeMode("light");
                } else if (position == 2) {
                    preferenceManager.setThemeMode("dark");
                } else {
                    preferenceManager.setThemeMode("system");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnDeleteCompleted).setOnClickListener(v -> confirmDeleteCompleted());
        findViewById(R.id.btnExportBackup).setOnClickListener(v -> openCreateBackupDocument());
        findViewById(R.id.btnRestoreBackup).setOnClickListener(v ->
                Toast.makeText(this, "Restore JSON disiapkan untuk fase berikutnya.", Toast.LENGTH_SHORT).show());
    }

    private void confirmDeleteCompleted() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus semua tugas selesai?")
                .setMessage("Tindakan ini tidak dapat dibatalkan.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> databaseExecutor.execute(() -> {
                    int deleted = taskRepository.deleteCompleted();
                    mainHandler.post(() -> Toast.makeText(
                            this,
                            deleted + " tugas selesai dihapus.",
                            Toast.LENGTH_SHORT).show());
                }))
                .show();
    }

    private void openCreateBackupDocument() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, backupManager.defaultBackupFileName());
        startActivityForResult(intent, REQUEST_CREATE_BACKUP);
    }

    private void writeBackup(Uri uri) {
        if (uri == null) {
            return;
        }
        databaseExecutor.execute(() -> {
            try {
                String json = backupManager.createBackupJson();
                try (OutputStream stream = getContentResolver().openOutputStream(uri, "wt")) {
                    if (stream == null) {
                        throw new IOException("Output stream unavailable");
                    }
                    stream.write(json.getBytes(StandardCharsets.UTF_8));
                }
                mainHandler.post(() -> Toast.makeText(this, "Backup data berhasil dibuat.", Toast.LENGTH_SHORT).show());
            } catch (Exception exception) {
                mainHandler.post(() -> Toast.makeText(this, "Backup data gagal dibuat.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
