package logic;

import entities.exceptions.WrongSelectionSizeException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import org.nevec.rjm.BigDecimalMath;


public class MathFunctionsHandler {
    
    private static final int OPERATIONS_DIGITS = 20;
    private static final int ROUND_DIGITS = 5;
    private static final MathContext mathContext = new MathContext(ROUND_DIGITS, RoundingMode.HALF_UP);
    
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal TWO = BigDecimal.valueOf(2);
    public static final BigDecimal FOUR = BigDecimal.valueOf(4);
    public static final BigDecimal HALF = new BigDecimal("0.5");
    public static final BigDecimal MIN = BigDecimal.valueOf(Long.MIN_VALUE);

    
    public static BigDecimal divide(BigDecimal first, BigDecimal second) {
        return first.divide(second, OPERATIONS_DIGITS, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal log(BigDecimal value) {
        try {
            return BigDecimalMath.log(value);
        }catch(ArithmeticException ex) {
            if (value.compareTo(ZERO) == 0) {
                return MIN;
            }
            throw ex;
        }
    }
    
    public static BigDecimal getSum(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        if (selection.isEmpty()) {
            throw new WrongSelectionSizeException();
        }
        
        BigDecimal sum = ZERO;
        
        Iterator<BigDecimal> iterator = selection.iterator();
        
        while(iterator.hasNext()) {
            sum = sum.add(iterator.next());
        }
        
        return sum;
    }
    
    public static BigDecimal getAverage(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        if (selection.isEmpty()) {
            throw new WrongSelectionSizeException();
        }
        
        return divide(getSum(selection), BigDecimal.valueOf(selection.size()));
    }
    
    public static BigDecimal round(BigDecimal value) {
        return value.round(mathContext);
    }
}
