package ca.lcit22fw.madt.techtoids.android.nota_app.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class BaseTask {
    private final String taskId = UUID.randomUUID().toString();
    private String taskListId;
    private int order;

    private String title;
    private String description;

    private Date updatedAt;

    private Date dueDate;

    private ArrayList<String> attachmentList = new ArrayList<>();

    public BaseTask() {
    }

    public BaseTask(int order, String title, String description) {
        this.order = order;
        this.title = title;
        this.description = description;
    }

    public String getTaskListId() {
        return taskListId;
    }

    public void setTaskListId(String taskListId) {
        this.taskListId = taskListId;
    }

    public String getTaskId() {
        return taskId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseTask)) return false;
        BaseTask baseTask = (BaseTask) o;
        return getTaskId().equals(baseTask.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }

    @Override
    public String toString() {
        return "BaseTask{" +
                "taskListId='" + taskListId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", order=" + order +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
