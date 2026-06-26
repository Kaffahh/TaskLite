package com.kaffah.tasklite.ui.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kaffah.tasklite.R;
import com.kaffah.tasklite.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    public interface CategoryActionListener {
        void onCategoryClicked(Category category);

        void onMenuClicked(Category category);
    }

    private final LayoutInflater inflater;
    private final CategoryActionListener listener;
    private final List<Category> categories = new ArrayList<>();

    public CategoryAdapter(LayoutInflater inflater, CategoryActionListener listener) {
        this.inflater = inflater;
        this.listener = listener;
    }

    public void submitList(List<Category> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categories.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bind(getItem(position), listener);
        return convertView;
    }

    private static class ViewHolder {
        final TextView viewColor;
        final TextView tvCategoryName;
        final Button btnCategoryMenu;

        ViewHolder(View view) {
            viewColor = view.findViewById(R.id.viewColor);
            tvCategoryName = view.findViewById(R.id.tvCategoryName);
            btnCategoryMenu = view.findViewById(R.id.btnCategoryMenu);
        }

        void bind(Category category, CategoryActionListener listener) {
            tvCategoryName.setText(category.name);
            GradientDrawable colorDot = new GradientDrawable();
            colorDot.setShape(GradientDrawable.OVAL);
            colorDot.setColor(category.color);
            viewColor.setBackground(colorDot);
            tvCategoryName.setOnClickListener(v -> listener.onCategoryClicked(category));
            btnCategoryMenu.setOnClickListener(v -> listener.onMenuClicked(category));
        }
    }
}
