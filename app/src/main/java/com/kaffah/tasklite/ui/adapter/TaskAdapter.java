package com.kaffah.tasklite.ui.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.model.Task;
import com.kaffah.tasklite.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends BaseAdapter {
    public interface TaskActionListener {
        void onTaskClicked(Task task);

        void onCompletionChanged(Task task, boolean completed);

        void onMenuClicked(Task task);
    }

    private final LayoutInflater inflater;
    private final TaskActionListener listener;
    private final List<Task> tasks = new ArrayList<>();

    public TaskAdapter(Context context, TaskActionListener listener) {
        inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void submitList(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);
        holder.bind(task, listener);
        return convertView;
    }

    private static class ViewHolder {
        final CheckBox cbCompleted;
        final TextView tvTitle;
        final TextView tvMeta;
        final TextView tvDeadline;
        final Button btnTaskMenu;

        ViewHolder(View view) {
            cbCompleted = view.findViewById(R.id.cbCompleted);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvMeta = view.findViewById(R.id.tvMeta);
            tvDeadline = view.findViewById(R.id.tvDeadline);
            btnTaskMenu = view.findViewById(R.id.btnTaskMenu);
        }

        void bind(Task task, TaskActionListener listener) {
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(task.completed);
            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) ->
                    listener.onCompletionChanged(task, isChecked));

            tvTitle.setText(task.title);
            tvTitle.setPaintFlags(task.completed
                    ? tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tvTitle.setAlpha(task.completed ? 0.55f : 1f);

            String category = task.categoryName == null ? "Tanpa kategori" : task.categoryName;
            String priority = priorityLabel(task.priority);
            String reminder = task.reminderEnabled ? " - Pengingat aktif" : "";
            tvMeta.setText(category + " - " + priority + reminder);

            String deadline = DateTimeUtils.formatDeadline(task.dueAt);
            if (DateTimeUtils.isOverdue(task.dueAt, task.completed)) {
                deadline = "Terlambat - " + deadline;
                tvDeadline.setTextColor(0xFFDC2626);
            } else {
                tvDeadline.setTextColor(0xFF64748B);
            }
            tvDeadline.setText(deadline);

            View.OnClickListener openListener = v -> listener.onTaskClicked(task);
            tvTitle.setOnClickListener(openListener);
            tvMeta.setOnClickListener(openListener);
            tvDeadline.setOnClickListener(openListener);
            btnTaskMenu.setOnClickListener(v -> listener.onMenuClicked(task));
            btnTaskMenu.setOnLongClickListener(v -> {
                listener.onMenuClicked(task);
                return true;
            });
        }

        private String priorityLabel(int priority) {
            if (priority == 3) {
                return "Prioritas Tinggi";
            }
            if (priority == 2) {
                return "Prioritas Sedang";
            }
            if (priority == 1) {
                return "Prioritas Rendah";
            }
            return "Tanpa prioritas";
        }
    }
}
