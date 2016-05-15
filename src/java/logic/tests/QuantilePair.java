package logic.tests;

import java.math.BigDecimal;


public class QuantilePair {
    
    public final BigDecimal first;
    public final BigDecimal second;
    
    public QuantilePair(BigDecimal first, BigDecimal second) {
        this.first = first;
        this.second = second;
    }

    
    public QuantilePair(int selectionSize, BigDecimal first, BigDecimal second) {
        this.first = first;
        this.second = second;
    }
    
}

