package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

import java.util.ArrayList;

public class Task {
    private Meta _meta;
    private String _description;
    private ArrayList<SubTask> _subTaskList;

    public Task(String id, String title, String description) {
        this._meta = new Meta(id, title);

    }

}
