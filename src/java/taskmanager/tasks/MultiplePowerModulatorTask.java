package taskmanager.tasks;

import entities.alternatives.SelectionParams;
import hibernate.logic.Quantile;
import java.util.List;
import java.util.Objects;


public class MultiplePowerModulatorTask implements ProcessTask {
    
    private final int selSize;
    private final String testType;
    private final List<Quantile> quantilesList;
    private final List<SelectionParams> paramsList;
    private final int powerIterCount;
    private final TaskTypeEnum taskType = TaskTypeEnum.MULTIPLEPOWERMODULATION;
    
    public MultiplePowerModulatorTask(
            int selSize,
            String testType,
            List<Quantile> quantilesList, 
            List<SelectionParams> paramsList,
            int powerIterCount) {
        this.selSize = selSize;
        this.testType = testType;
        this.quantilesList = quantilesList;
        this.paramsList = paramsList;
        this.powerIterCount = powerIterCount;
    }
    
    @Override
    public int compareTo(ProcessTask other) {
        if (other == null) {
            return 1;
        }
        
        if (!(other instanceof MultiplePowerModulatorTask)) {
            return this.taskType.compareTo(other.getTaskType());
        }
        
        MultiplePowerModulatorTask otherTask = (MultiplePowerModulatorTask) other;
        
        int compareResult = this.selSize - otherTask.selSize;
        
        if (compareResult != 0) {
            return compareResult;
        }
        
        compareResult = (this.testType == null) ? -1 : this.testType.compareTo(otherTask.testType);
        
        if (compareResult != 0) {
            return compareResult;
        }
        
        return this.powerIterCount - otherTask.powerIterCount;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MultiplePowerModulatorTask)) return false;
        
        MultiplePowerModulatorTask other = (MultiplePowerModulatorTask) obj;
        
        return ((selSize == other.selSize)
                && Objects.equals(testType, other.testType)
                && (powerIterCount == other.powerIterCount));
    }
    
    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }
}