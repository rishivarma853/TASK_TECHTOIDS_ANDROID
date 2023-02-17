package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Board {
    private final String taskListId = UUID.randomUUID().toString();
    private String title;
    private List<ParentTask> tasks = new ArrayList<>();

    private String userId;
    private Date updatedAt;

    private LatLng location;

    public Board() {
    }

    public Board(String title, List<ParentTask> tasks, String userId) {
        this.title = title;
        this.tasks = tasks;
        this.userId = userId;
    }

    public String getTaskListId() {
        return taskListId;
    }

    public String getTitle() {
        return title;
    }

    public List<ParentTask> getTasks() {
        return tasks;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    

    public void addParentTask(ParentTask task){
        insertParentTask(tasks.size(), task);
    }

    public void insertParentTask(int position, ParentTask task){
        task.setTaskListId(getTaskListId());
        tasks.remove(task);
        int index = Math.min(position, tasks.size());
        tasks.add(index, task);
        task.onUpdate();
        updatedAt = Helper.getUTCDate();
        refreshOrder();
    }

    private void refreshOrder(){
        for (int i = 0; i < tasks.size(); i++) {
            ParentTask task = tasks.get(i);
            task.setOrder(i);
        }
    }

    @Override
    public String toString() {
        return "Board{" +
                "taskListId='" + taskListId + '\'' +
                ", title='" + title + '\'' +
                ", tasks=" + tasks +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
