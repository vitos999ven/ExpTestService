package entities;

import entities.actionresults.ActionResult;
import java.util.Map;
import java.util.TreeMap;


public class TestResultsMap {
    
    public final SelectionInfo selection;
    public final Map<String, ActionResult> test_results;
    
    public TestResultsMap(SelectionInfo selection) {
        this.selection = selection;
        this.test_results = new TreeMap<>();
    }
}
