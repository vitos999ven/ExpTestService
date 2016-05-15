package taskmanager.tasks;

import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import java.util.Objects;


public class QuantilesModulatorTask implements ProcessTask {

    private final TestType testType;
    private final int iterCount;
    private final int selSize;
    private final SignificanceLevel signLevel;
    private final TaskTypeEnum taskType = TaskTypeEnum.QUANTILESMODULATION;
    
    public QuantilesModulatorTask(
            TestType testType, 
            SignificanceLevel signLevel,
            int iterCount, 
            int selSize) {
        this.testType = testType;
        this.signLevel = signLevel;
        this.iterCount = iterCount;
        this.selSize = selSize;
    }
    
    @Override
    public int compareTo(ProcessTask other) {
        if (other == null) {
            return 1;
        }
        
        if (!(other instanceof QuantilesModulatorTask)) {
            return this.taskType.compareTo(other.getTaskType());
        }
        
        QuantilesModulatorTask otherTask = (QuantilesModulatorTask) other;
        
        int testTypeCompare = this.testType.compareTo(otherTask.testType);
        
        if (testTypeCompare != 0) {
            return testTypeCompare;
        }
        
        int signLevelsCompare = this.signLevel.compareTo(otherTask.signLevel);
        
        if (signLevelsCompare != 0) {
            return signLevelsCompare;
        }
        
        if (this.iterCount != otherTask.iterCount) {
            return (this.iterCount - otherTask.iterCount);
        }
        
        return this.selSize - otherTask.selSize;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof QuantilesModulatorTask)) return false;
        
        QuantilesModulatorTask other = (QuantilesModulatorTask) obj;
        
        return (Objects.equals(testType, other.testType)
                && Objects.equals(signLevel, other.signLevel)
                && (iterCount == other.iterCount)
                && (selSize == other.selSize) );
    }

    @Override
    public TaskTypeEnum getTaskType() {
        return taskType;
    }
    
}
