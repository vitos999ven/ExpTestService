package entities.exceptions.actionreasons;


public class InvalidParameter extends ActionExceptionReason {
    
    private final String parameter;
    
    public InvalidParameter(String parameter) {
        super(ReasonType.INVALIDPARAMETER.toString());
        this.parameter = parameter;
    }
    
    @Override
    public String toString() {
        return "Invalid parameter " + parameter;
    }
}
