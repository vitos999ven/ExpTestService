package services;

import hibernate.logic.Power;
import java.util.ListIterator;


public class ResultsArrayInfo {

    public final boolean succeded;
    public final double trueAcceptancePower;
    public final ListIterator<Power> alternativePowers;
    
    public ResultsArrayInfo(boolean succeded, double trueAcceptancePower, ListIterator<Power> alternativePowers) {
        this.succeded = succeded;
        this.trueAcceptancePower = trueAcceptancePower;
        this.alternativePowers = alternativePowers;
    }
            
}
