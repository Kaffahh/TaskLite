package com.kaffah.tasklite.util;

public final class ValidationUtils {
    public static final int MAX_TITLE = 120;
    public static final int MAX_DESCRIPTION = 2000;

    private ValidationUtils() {
    }

    public static String validateTitle(String title) {
        String safeTitle = title == null ? "" : title.trim();
        if (safeTitle.isEmpty()) {
            return "Judul tugas wajib diisi.";
        }
        if (safeTitle.length() > MAX_TITLE) {
            return "Judul tugas maksimal 120 karakter.";
        }
        return null;
    }

    public static String validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION) {
            return "Deskripsi maksimal 2.000 karakter.";
        }
        return null;
    }
}
