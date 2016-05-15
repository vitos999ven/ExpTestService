package entities.exceptions.actionreasons;


public class WrongDBData extends ActionExceptionReason {
    
    private String name;
    
    public WrongDBData(String name) {
        super(ReasonType.WRONGDBDATA.toString());
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "Wrong data " + name;
    }
}
