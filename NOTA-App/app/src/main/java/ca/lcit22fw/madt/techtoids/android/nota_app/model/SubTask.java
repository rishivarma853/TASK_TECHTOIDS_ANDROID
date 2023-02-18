package ca.lcit22fw.madt.techtoids.android.nota_app.model;

public class SubTask extends BaseTask {
    private String parentTaskId;

    public SubTask() {
    }

    public SubTask(int order, String title, String description) {
        super(order, title, description);
    }

    public SubTask(int order, String title, String description, String parentTaskId) {
        super(order, title, description);
        this.parentTaskId = parentTaskId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
        onUpdate();
    }


}
