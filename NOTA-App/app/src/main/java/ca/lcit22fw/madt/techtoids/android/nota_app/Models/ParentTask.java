package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

import java.util.ArrayList;
import java.util.List;

//import javax.annotation.Nullable;

public class ParentTask extends BaseTask {
    private List<SubTask> childTasks = new ArrayList<>();

    public ParentTask() {
    }

    public ParentTask(int order, String title, String description) {
        super(order, title, description);
    }

    public List<SubTask> getChildTasks() {
        return childTasks;
    }

    public void addChildTask(SubTask task){
        insertChildTask(childTasks.size(), task);
    }

    public void insertChildTask(int position, SubTask task){
        task.setParentTaskId(getTaskId());
        childTasks.remove(task);
        int index = Math.min(position, childTasks.size());
        childTasks.add(index, task);
        task.onUpdate();
        onUpdate();
        refreshOrder();
    }

    private void refreshOrder(){
        for (int i = 0; i < childTasks.size(); i++) {
            SubTask task = childTasks.get(i);
            task.setOrder(i);
        }
    }
}
