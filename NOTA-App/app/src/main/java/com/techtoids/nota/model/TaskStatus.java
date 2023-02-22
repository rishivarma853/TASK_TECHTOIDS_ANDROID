package com.techtoids.nota.model;

import androidx.annotation.NonNull;

public enum TaskStatus {
    TODO("Todo"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed");

    public final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return value.toUpperCase();
    }
}
