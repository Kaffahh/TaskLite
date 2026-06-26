package com.kaffah.tasklite.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.data.TaskRepository;
import com.kaffah.tasklite.model.Task;
import com.kaffah.tasklite.ui.adapter.TaskAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;
    private TextView tvSummary;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private LinearLayout emptyState;
    private EditText etSearch;
    private String activeFilter = TaskRepository.FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskRepository = new TaskRepository(this);
        bindViews();
        setupActions();
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskRepository != null) {
            loadTasks();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void bindViews() {
        tvSummary = findViewById(R.id.tvSummary);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        emptyState = findViewById(R.id.emptyState);
        etSearch = findViewById(R.id.etSearch);
        ListView listTasks = findViewById(R.id.listTasks);

        taskAdapter = new TaskAdapter(this, new TaskAdapter.TaskActionListener() {
            @Override
            public void onTaskClicked(Task task) {
                openTaskForm(task.id);
            }

            @Override
            public void onCompletionChanged(Task task, boolean completed) {
                updateCompletion(task, completed);
            }

            @Override
            public void onMenuClicked(Task task) {
                showTaskMenu(task);
            }
        });
        listTasks.setAdapter(taskAdapter);
    }

    private void setupActions() {
        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnCategories = findViewById(R.id.btnCategories);
        Button btnSettings = findViewById(R.id.btnSettings);
        RadioGroup rgFilters = findViewById(R.id.rgFilters);

        btnAddTask.setOnClickListener(v -> openTaskForm(0));
        btnCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        rgFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.filterToday) {
                activeFilter = TaskRepository.FILTER_TODAY;
            } else if (checkedId == R.id.filterOverdue) {
                activeFilter = TaskRepository.FILTER_OVERDUE;
            } else if (checkedId == R.id.filterCompleted) {
                activeFilter = TaskRepository.FILTER_COMPLETED;
            } else {
                activeFilter = TaskRepository.FILTER_ALL;
            }
            loadTasks();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(MainActivity.this::loadTasks, 280);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadTasks() {
        String query = etSearch == null ? "" : etSearch.getText().toString();
        databaseExecutor.execute(() -> {
            List<Task> tasks = taskRepository.getTasks(activeFilter, query);
            int[] summary = taskRepository.getSummary();
            mainHandler.post(() -> {
                taskAdapter.submitList(tasks);
                tvSummary.setText(summary[0] + " tugas aktif - " + summary[1] + " selesai");
                updateEmptyState(tasks.isEmpty(), query);
            });
        });
    }

    private void updateEmptyState(boolean isEmpty, String query) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (!isEmpty) {
            return;
        }

        if (query != null && !query.trim().isEmpty()) {
            tvEmptyTitle.setText("Tugas tidak ditemukan");
            tvEmptyMessage.setText("Coba gunakan kata pencarian yang berbeda.");
        } else if (TaskRepository.FILTER_TODAY.equals(activeFilter)) {
            tvEmptyTitle.setText("Tidak ada tugas hari ini");
            tvEmptyMessage.setText("Tidak ada tugas untuk hari ini.");
        } else if (TaskRepository.FILTER_OVERDUE.equals(activeFilter)) {
            tvEmptyTitle.setText("Tidak ada tugas terlambat");
            tvEmptyMessage.setText("Tidak ada tugas yang terlambat.");
        } else if (TaskRepository.FILTER_COMPLETED.equals(activeFilter)) {
            tvEmptyTitle.setText("Belum ada tugas yang selesai");
            tvEmptyMessage.setText("Tugas selesai akan tampil di sini.");
        } else {
            tvEmptyTitle.setText(R.string.empty_tasks_title);
            tvEmptyMessage.setText(R.string.empty_tasks_message);
        }
    }

    private void openTaskForm(long taskId) {
        Intent intent = new Intent(this, TaskFormActivity.class);
        if (taskId > 0) {
            intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, taskId);
        }
        startActivity(intent);
    }

    private void updateCompletion(Task task, boolean completed) {
        databaseExecutor.execute(() -> {
            taskRepository.setCompleted(task.id, completed);
            mainHandler.post(this::loadTasks);
        });
    }

    private void showTaskMenu(Task task) {
        String statusText = task.completed ? "Aktifkan kembali" : "Tandai selesai";
        new AlertDialog.Builder(this)
                .setTitle(task.title)
                .setItems(new String[]{"Edit", statusText, "Hapus"}, (dialog, which) -> {
                    if (which == 0) {
                        openTaskForm(task.id);
                    } else if (which == 1) {
                        updateCompletion(task, !task.completed);
                    } else {
                        confirmDelete(task);
                    }
                })
                .show();
    }

    private void confirmDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus tugas?")
                .setMessage("Tugas \"" + task.title + "\" akan dihapus.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> databaseExecutor.execute(() -> {
                    taskRepository.delete(task.id);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Tugas dihapus.", Toast.LENGTH_SHORT).show();
                        loadTasks();
                    });
                }))
                .show();
    }
}
