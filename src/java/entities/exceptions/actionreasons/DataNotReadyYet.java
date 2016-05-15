package entities.exceptions.actionreasons;


public class DataNotReadyYet extends ActionExceptionReason {

    private double status;
    
    public DataNotReadyYet(double status) {
        super(ReasonType.DATANOTREADYYET.toString());
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Data not ready yet. Status: " + status;
    }
}
