package taskmanager.tasks;

import entities.alternatives.SelectionParams;
import hibernate.logic.Quantile;
import java.util.Objects;


public class PowerModulatorTask implements ProcessTask {
    
    private final Quantile quantile;
    private final SelectionParams params;
    private final int powerIterCount;
    private final TaskTypeEnum taskType = TaskTypeEnum.POWERMODULATION;
    
    public PowerModulatorTask(
            Quantile quantile, 
            SelectionParams params,
            int powerIterCount) {
        this.quantile = quantile;
        this.params = params;
        this.powerIterCount = powerIterCount;
    }
    
    @Override
    public int compareTo(ProcessTask other) {
        if (other == null) {
            return 1;
        }
        
        if (!(other instanceof PowerModulatorTask)) {
            return this.taskType.compareTo(other.getTaskType());
        }
        
        PowerModulatorTask otherTask = (PowerModulatorTask) other;
        
        int compareResult = (this.quantile == null) ? -1 : this.quantile.compareTo(otherTask.quantile);
        
        if (compareResult != 0) {
            return compareResult;
        }
        
        compareResult = (this.params == null) ? -1 : this.params.compareTo(otherTask.params);
        
        if (compareResult != 0) {
            return compareResult;
        }
        
        return this.powerIterCount - otherTask.powerIterCount;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PowerModulatorTask)) return false;
        
        PowerModulatorTask other = (PowerModulatorTask) obj;
        
        return (Objects.equals(quantile, other.quantile)
                && Objects.equals(params, other.params)
                && (powerIterCount == other.powerIterCount));
    }
    
    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }
}
