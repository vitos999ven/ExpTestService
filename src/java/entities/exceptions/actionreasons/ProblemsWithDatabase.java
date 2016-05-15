package entities.exceptions.actionreasons;


public class ProblemsWithDatabase extends ActionExceptionReason {

    public ProblemsWithDatabase() {
        super(ReasonType.DBPROBLEMS.toString());
    }
    
    @Override
    public String toString() {
        return "Problems with database";
    }
    
}
