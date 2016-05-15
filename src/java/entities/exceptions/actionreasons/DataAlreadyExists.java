package entities.exceptions.actionreasons;


public class DataAlreadyExists extends ActionExceptionReason {

    public DataAlreadyExists() {
        super(ReasonType.DATAALREADYEXISTS.toString());
    }
    
    @Override
    public String toString() {
        return "Data already exists";
    }
    
}
