package entities.actionresults;

import entities.exceptions.actionreasons.ActionExceptionReason;


public class FailedActionResult extends ActionResult {
    
    private final ActionExceptionReason reason;
    
    public FailedActionResult(ActionExceptionReason reason) {
        super(false);
        this.reason = reason;
    }
    
    public ActionExceptionReason getReason() {
        return reason;
    }
}
