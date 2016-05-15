package taskmanager.tasks;


public class MultipleQuantileModulatorTask implements ProcessTask {

    private final int iterCount;
    private final int selSize;
    private final TaskTypeEnum taskType = TaskTypeEnum.MULTIPLEQUANTILESMODULATION;
    
    public MultipleQuantileModulatorTask(
            int iterCount, 
            int selSize) {
        this.iterCount = iterCount;
        this.selSize = selSize;
    }
    
    @Override
    public int compareTo(ProcessTask other) {
        if (other == null) {
            return 1;
        }
        
        if (!(other instanceof MultipleQuantileModulatorTask)) {
            return this.taskType.compareTo(other.getTaskType());
        }
        
        MultipleQuantileModulatorTask otherTask = (MultipleQuantileModulatorTask)other;
        
        if (this.iterCount != otherTask.iterCount) {
            return (this.iterCount - otherTask.iterCount);
        }
        
        return this.selSize - otherTask.selSize;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MultipleQuantileModulatorTask)) return false;
        
        MultipleQuantileModulatorTask other = (MultipleQuantileModulatorTask) obj;
        
        return ((iterCount == other.iterCount)
                && (selSize == other.selSize) );
    }

    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }
}
