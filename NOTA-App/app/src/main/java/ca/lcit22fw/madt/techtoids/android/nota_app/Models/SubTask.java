package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

public class SubTask {
    private Meta _meta;
    private String _description;
    private String _dueDate;
    private String _taskStatus;
    private String _parentID;

    public SubTask(String id, String title, String description, String dueDate, String taskStatus, String parentID) {
        this._meta = new Meta(id, title);
        this._description = description;
        this._dueDate = dueDate;
        this._taskStatus = taskStatus;
        this._parentID = parentID;
    }

    public Meta getMeta() {
        return this._meta;
    }

    public void setMeta(Meta meta) {
        this._meta = meta;
    }

    public String getDescription() {
        return this._description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public String getDueDate() {
        return this._dueDate;
    }

    public void setDueDate(String dueDate) {
        this._dueDate = dueDate;
    }

    public String getTaskStatus() {
        return this._taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this._taskStatus = taskStatus;
    }

    public String getParentID() {
        return this._parentID;
    }

    public void setParentID(String parentID) {
        this._parentID = parentID;
    }
}
