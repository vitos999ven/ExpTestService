package taskmanager.tasks;


public interface ProcessTask extends Comparable<ProcessTask> {
    public TaskTypeEnum getTaskType();
}
