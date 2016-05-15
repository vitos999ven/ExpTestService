package entities;

import java.math.BigDecimal;


public class TestResult {

    public final boolean result;
    public final BigDecimal test_value;
    public final BigDecimal first_value;
    public final BigDecimal second_value;
    
    public TestResult(
            boolean result, 
            BigDecimal test_value,
            BigDecimal first_value,
            BigDecimal second_value) {
        this.result = result;
        this.test_value = test_value;
        this.first_value = first_value;
        this.second_value = second_value;
    }
    
}
