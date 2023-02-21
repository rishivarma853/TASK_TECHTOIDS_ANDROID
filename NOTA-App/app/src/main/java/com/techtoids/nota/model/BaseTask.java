package com.techtoids.nota.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaseTask implements Cloneable {
    private String taskId = UUID.randomUUID().toString();
    private String boardId;
    private String userId;
    private int order;
    private String title;
    private String description;
    private Date updatedAt;
    private Date dueDate;
    private ArrayList<String> attachmentList = new ArrayList<>();
    private TaskStatus taskStatus = TaskStatus.TODO;
    List<BaseTask> childTasks = new ArrayList<>();
    private double latitude;
    private double longitude;

    public List<BaseTask> getChildTasks() {
        return childTasks;
    }

    public void setChildTasks(List<BaseTask> childTasks) {
        this.childTasks = childTasks;
    }

    public BaseTask() {
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        onUpdate();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        onUpdate();
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ArrayList<String> getAttachmentList() {
        return this.attachmentList;
    }

    public void setAttachmentList(ArrayList<String> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public void onUpdate() {
        this.updatedAt = Helper.getUTCDate();
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseTask)) return false;
        BaseTask taskData = (BaseTask) o;
        return getTaskId().equals(taskData.getTaskId())
                && getTitle().equals(taskData.getTitle())
                && getDescription().equals(taskData.getDescription())
                && getDueDate().equals(taskData.getDueDate())
                && getAttachmentList().equals(taskData.getAttachmentList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }

    @Override
    public String toString() {
        return "BaseTask{" +
                "taskId='" + taskId + '\'' +
                ", boardId='" + boardId + '\'' +
                ", userId='" + userId + '\'' +
                ", order=" + order +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                ", dueDate=" + dueDate +
                ", attachmentList=" + attachmentList +
                ", taskStatus=" + taskStatus +
                ", childTasks=" + childTasks +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public boolean contains(String text) {
        String search = text.trim().toLowerCase();
        return getTitle().toLowerCase().contains(search) ||
                getDescription().toLowerCase().contains(search) ||
                childTasks.stream().filter(task -> task.contains(text)).collect(Collectors.toList()).size() > 0;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();

        BaseTask task = (BaseTask) obj;

        task.setAttachmentList((ArrayList<String>) task.getAttachmentList().clone());
        task.setChildTasks(task.getChildTasks().stream().map(t -> {
            try {
                return (BaseTask) t.clone();
            } catch (CloneNotSupportedException e) {
                return t;
            }
        }).collect(Collectors.toList()));
        return task;
    }
}