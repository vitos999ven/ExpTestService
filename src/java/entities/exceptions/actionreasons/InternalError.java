package entities.exceptions.actionreasons;


public class InternalError extends ActionExceptionReason {
    
    public final String message;

    public InternalError(String message) {
        super(ReasonType.INTERNALERROR.toString());
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return (new StringBuilder().append("Internal error: ").append(message).toString());
    }
}
