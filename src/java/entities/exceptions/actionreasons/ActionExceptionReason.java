package entities.exceptions.actionreasons;

public class ActionExceptionReason {

    private final String type;

    protected ActionExceptionReason(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
}
