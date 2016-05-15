package entities.exceptions;

import entities.exceptions.actionreasons.ActionExceptionReason;


public class ActionException extends AppException {
    
    private final ActionExceptionReason reason;
     
    public ActionException(ActionExceptionReason reason) {
        this.reason = reason;
    }
    
    
    public ActionExceptionReason getReason() {
        return reason;
    }
}
