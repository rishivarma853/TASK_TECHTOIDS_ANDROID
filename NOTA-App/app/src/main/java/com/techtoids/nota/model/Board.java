package com.techtoids.nota.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Board {
    private final String boardId = UUID.randomUUID().toString();
    private String title;
    private String userId;
    private Date updatedAt;
    private LatLng location;

    public Board() {
    }

    public String getBoardId() {
        return this.boardId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LatLng getLocation() {
        return this.location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
    @Override
    public String toString() {
        return "Board{" +
                "taskListId='" + boardId + '\'' +
                ", title='" + title + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
