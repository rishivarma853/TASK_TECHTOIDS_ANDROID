package ca.lcit22fw.madt.techtoids.android.nota_app.Models;

import java.util.ArrayList;

public class Board {
    private Meta _meta;
    private ArrayList<Task> _taskList;

    public Board(Meta meta, ArrayList<Task> taskList) {
        this._meta = meta;
        this._taskList = taskList;
    }

//    public Board()

    public Meta getMeta() {
        return this._meta;
    }

    public void setMeta(Meta meta) {
        this._meta = meta;
    }

    public ArrayList<Task> getTaskList() {
        return _taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this._taskList = taskList;
    }
}
