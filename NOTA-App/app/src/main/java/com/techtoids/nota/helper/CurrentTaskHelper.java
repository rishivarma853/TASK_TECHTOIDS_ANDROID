package com.techtoids.nota.helper;

import com.techtoids.nota.model.BaseTask;
public class CurrentTaskHelper {
    public static CurrentTaskHelper instance = new CurrentTaskHelper();
    public BaseTask taskData;
    private BaseTask originalData;
    private CurrentTaskHelper() {
    }

    public BaseTask getOriginalData() {
        return originalData;
    }

    public void setTaskData(BaseTask taskData) {
        this.taskData = taskData;
        this.originalData = taskData;
    }

    public boolean hasChanges(){
        return !taskData.equals(originalData);
    }
}
