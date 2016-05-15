package entities.exceptions.actionreasons;

public enum ReasonType {

    DATAALREADYEXISTS("already_exists"),
    DATANOTREADYYET("data_not_ready_yet"),
    DBPROBLEMS("db_problems"),
    INTERNALERROR("internal_error"),
    INVALIDPARAMETER("invalid_param"),
    NODATA("no_data"),
    NODEPENDENTDATA("no_dependent_data"),
    WRONGDBDATA("wrong_db_data");

    private final String type;

    ReasonType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type;
    }
}
