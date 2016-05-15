package entities.actionresults;


public class SucceededActionResult<T> extends ActionResult {

    private final T data;
    
    public SucceededActionResult() {
        super(true);
        data = null;
    }
    
    public SucceededActionResult(T data) {
        super(true);
        this.data = data;
    }
    
    public T getData() {
        return data;
    }
    
}
