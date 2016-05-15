package entities.exceptions.actionreasons;


public class NoDependentData extends ActionExceptionReason {

    public final String name;

    public NoDependentData(String name) {
        super(ReasonType.NODEPENDENTDATA.toString());
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return (new StringBuilder().append("No dependent data \"").append(name).append("\"").toString());
    }
}