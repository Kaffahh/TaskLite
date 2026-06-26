package com.kaffah.tasklite.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.data.CategoryRepository;
import com.kaffah.tasklite.data.TaskRepository;
import com.kaffah.tasklite.model.Category;
import com.kaffah.tasklite.model.Task;
import com.kaffah.tasklite.util.DateTimeUtils;
import com.kaffah.tasklite.util.ValidationUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskFormActivity extends Activity {
    public static final String EXTRA_TASK_ID = "task_id";

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Category> categories = new ArrayList<>();

    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private EditText etTitle;
    private EditText etDescription;
    private Spinner spCategory;
    private Spinner spPriority;
    private CheckBox cbReminder;
    private Button btnDate;
    private Button btnTime;
    private Button btnSave;
    private long taskId;
    private Long dueAt;
    private Task editingTask;
    private boolean formChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        taskRepository = new TaskRepository(this);
        categoryRepository = new CategoryRepository(this);
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, 0);

        bindViews();
        setupStaticAdapters();
        setupActions();
        restoreState(savedInstanceState);
        loadInitialData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        if (formChanged) {
            new AlertDialog.Builder(this)
                    .setTitle("Buang perubahan?")
                    .setMessage("Perubahan yang belum disimpan akan hilang.")
                    .setNegativeButton("Tetap di halaman", null)
                    .setPositiveButton("Buang perubahan", (dialog, which) -> finish())
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("dueAt", dueAt == null ? -1L : dueAt);
        outState.putBoolean("formChanged", formChanged);
    }

    private void bindViews() {
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        spPriority = findViewById(R.id.spPriority);
        cbReminder = findViewById(R.id.cbReminder);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSave = findViewById(R.id.btnSave);

        tvFormTitle.setText(taskId > 0 ? R.string.edit_task : R.string.add_task);
    }

    private void setupStaticAdapters() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tanpa prioritas", "Rendah", "Sedang", "Tinggi"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);
    }

    private void setupActions() {
        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        findViewById(R.id.btnClearDeadline).setOnClickListener(v -> {
            dueAt = null;
            cbReminder.setChecked(false);
            updateDeadlineButtons();
            markChanged();
        });
        cbReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && dueAt == null) {
                cbReminder.setChecked(false);
                Toast.makeText(this, "Pilih deadline terlebih dahulu.", Toast.LENGTH_SHORT).show();
            } else {
                markChanged();
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        long savedDueAt = savedInstanceState.getLong("dueAt", -1L);
        dueAt = savedDueAt == -1L ? null : savedDueAt;
        formChanged = savedInstanceState.getBoolean("formChanged", false);
        updateDeadlineButtons();
    }

    private void loadInitialData() {
        databaseExecutor.execute(() -> {
            List<Category> loadedCategories = categoryRepository.getAllCategories();
            Task loadedTask = taskId > 0 ? taskRepository.getTask(taskId) : null;
            mainHandler.post(() -> {
                categories.clear();
                categories.addAll(loadedCategories);
                setupCategoryAdapter();
                if (taskId > 0) {
                    if (loadedTask == null) {
                        Toast.makeText(this, "Tugas tidak ditemukan atau telah dihapus.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    editingTask = loadedTask;
                    populateTask(loadedTask);
                }
                updateDeadlineButtons();
            });
        });
    }

    private void setupCategoryAdapter() {
        List<String> labels = new ArrayList<>();
        labels.add("Tanpa kategori");
        for (Category category : categories) {
            labels.add(category.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void populateTask(Task task) {
        etTitle.setText(task.title);
        etDescription.setText(task.description);
        spPriority.setSelection(task.priority);
        cbReminder.setChecked(task.reminderEnabled);
        dueAt = task.dueAt;
        if (task.categoryId != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == task.categoryId) {
                    spCategory.setSelection(i + 1);
                    break;
                }
            }
        }
        formChanged = false;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (dueAt != null) {
            calendar.setTimeInMillis(dueAt);
        }
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.setTimeInMillis(dueAt == null ? System.currentTimeMillis() : dueAt);
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (dueAt == null) {
                        selected.set(Calendar.HOUR_OF_DAY, 23);
                        selected.set(Calendar.MINUTE, 59);
                    }
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    dueAt = selected.getTimeInMillis();
                    updateDeadlineButtons();
                    markChanged();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (dueAt != null) {
            calendar.setTimeInMillis(dueAt);
        }
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.setTimeInMillis(dueAt == null ? System.currentTimeMillis() : dueAt);
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    dueAt = selected.getTimeInMillis();
                    updateDeadlineButtons();
                    markChanged();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true)
                .show();
    }

    private void updateDeadlineButtons() {
        if (dueAt == null) {
            btnDate.setText("Pilih tanggal");
            btnTime.setText("Pilih waktu");
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dueAt);
        btnDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
        btnTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime()));
    }

    private void saveTask() {
        String titleError = ValidationUtils.validateTitle(etTitle.getText().toString());
        if (titleError != null) {
            etTitle.setError(titleError);
            return;
        }
        String descriptionError = ValidationUtils.validateDescription(etDescription.getText().toString());
        if (descriptionError != null) {
            etDescription.setError(descriptionError);
            return;
        }

        Task task = editingTask == null ? new Task() : editingTask;
        task.title = etTitle.getText().toString().trim();
        task.description = etDescription.getText().toString().trim();
        task.priority = spPriority.getSelectedItemPosition();
        task.categoryId = selectedCategoryId();
        task.dueAt = dueAt;
        task.reminderEnabled = cbReminder.isChecked() && dueAt != null;
        task.reminderAt = task.reminderEnabled ? dueAt : null;

        btnSave.setEnabled(false);
        databaseExecutor.execute(() -> {
            try {
                if (task.id > 0) {
                    taskRepository.update(task);
                } else {
                    taskRepository.insert(task);
                }
                mainHandler.post(() -> {
                    formChanged = false;
                    finish();
                });
            } catch (RuntimeException exception) {
                mainHandler.post(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Tugas gagal disimpan. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Long selectedCategoryId() {
        int position = spCategory.getSelectedItemPosition();
        if (position <= 0 || position > categories.size()) {
            return null;
        }
        return categories.get(position - 1).id;
    }

    private void markChanged() {
        formChanged = true;
    }
}
