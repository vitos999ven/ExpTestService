package entities.exceptions.actionreasons;


public class NoData extends ActionExceptionReason {

    public NoData() {
        super(ReasonType.NODATA.toString());
    }
    
    @Override
    public String toString() {
        return "No data";
    }
    
}
