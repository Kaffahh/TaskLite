package com.kaffah.tasklite.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.data.CategoryRepository;
import com.kaffah.tasklite.model.Category;
import com.kaffah.tasklite.ui.adapter.CategoryAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryActivity extends Activity {
    private static final int[] COLOR_VALUES = {
            0xFF2563EB,
            0xFF16A34A,
            0xFFDC2626,
            0xFFD97706,
            0xFF7C3AED,
            0xFF64748B
    };
    private static final String[] COLOR_LABELS = {"Biru", "Hijau", "Merah", "Oranye", "Ungu", "Abu-abu"};

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CategoryRepository categoryRepository;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryRepository = new CategoryRepository(this);
        categoryAdapter = new CategoryAdapter(LayoutInflater.from(this), new CategoryAdapter.CategoryActionListener() {
            @Override
            public void onCategoryClicked(Category category) {
                showCategoryDialog(category);
            }

            @Override
            public void onMenuClicked(Category category) {
                showCategoryMenu(category);
            }
        });

        ListView listCategories = findViewById(R.id.listCategories);
        listCategories.setAdapter(categoryAdapter);
        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showCategoryDialog(null));
        loadCategories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadCategories() {
        databaseExecutor.execute(() -> {
            List<Category> categories = categoryRepository.getAllCategories();
            mainHandler.post(() -> categoryAdapter.submitList(categories));
        });
    }

    private void showCategoryMenu(Category category) {
        new AlertDialog.Builder(this)
                .setTitle(category.name)
                .setItems(new String[]{"Edit", "Hapus"}, (dialog, which) -> {
                    if (which == 0) {
                        showCategoryDialog(category);
                    } else {
                        confirmDelete(category);
                    }
                })
                .show();
    }

    private void showCategoryDialog(Category category) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Nama kategori");
        nameInput.setSingleLine(true);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        if (category != null) {
            nameInput.setText(category.name);
            nameInput.setSelection(category.name.length());
        }
        content.addView(nameInput);

        Spinner colorSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, COLOR_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);
        if (category != null) {
            colorSpinner.setSelection(colorIndexOf(category.color));
        }
        content.addView(colorSpinner);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(category == null ? "Tambah kategori" : "Edit kategori")
                .setView(content)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError("Nama kategori wajib diisi.");
                return;
            }

            long exceptId = category == null ? 0 : category.id;
            databaseExecutor.execute(() -> {
                boolean used = categoryRepository.isNameUsed(name, exceptId);
                if (used) {
                    mainHandler.post(() -> nameInput.setError("Nama kategori sudah digunakan."));
                    return;
                }

                int color = COLOR_VALUES[colorSpinner.getSelectedItemPosition()];
                if (category == null) {
                    categoryRepository.insert(name, color);
                } else {
                    category.name = name;
                    category.color = color;
                    categoryRepository.update(category);
                }
                mainHandler.post(() -> {
                    dialog.dismiss();
                    loadCategories();
                });
            });
        });
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus kategori?")
                .setMessage("Kategori akan dihapus, tetapi tugas di dalamnya tetap tersimpan tanpa kategori.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> databaseExecutor.execute(() -> {
                    categoryRepository.delete(category.id);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Kategori dihapus.", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    });
                }))
                .show();
    }

    private int colorIndexOf(int color) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i] == color) {
                return i;
            }
        }
        return 0;
    }
}
