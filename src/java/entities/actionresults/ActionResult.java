package entities.actionresults;


public abstract class ActionResult {
    
    public final boolean succeeded;
    
    ActionResult(boolean succeeded) {
        this.succeeded = succeeded;
    }
    
}
