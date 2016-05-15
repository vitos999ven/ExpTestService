package logic.selections;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import logic.MathFunctionsHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.nevec.rjm.BigDecimalMath;


public class SelectionsModulator {
    
    
    
    private static final Random random = new Random();
    
    private static final BigDecimal MAX_INT = BigDecimal.valueOf(Integer.MAX_VALUE);
    
    public static BigDecimal getRandValue() {
        int intValue;
        do {
            intValue = random.nextInt();
        } while (intValue == 0);
        BigDecimal bigValue = BigDecimal.valueOf(Math.abs(intValue));
        return /*BigDecimal.valueOf(random.nextDouble());*/MathFunctionsHandler.divide(bigValue, MAX_INT);
    }
     
    public static BigDecimal getExpValue() {
        return (MathFunctionsHandler.log(MathFunctionsHandler.ONE.subtract(getRandValue())).negate());
    }
    
    public static BigDecimal getWeibullValue(BigDecimal weibullParam)
            throws InvalidParameterException {
        if (weibullParam.compareTo(MathFunctionsHandler.ZERO) <= 0) {
            throw new InvalidParameterException("Wrong weibull parameter while generating new weibull value");
        }
        
        return BigDecimalMath.pow(
                getExpValue(), MathFunctionsHandler.divide(MathFunctionsHandler.ONE, weibullParam));
    }
    
    public static List<BigDecimal> createExpSelection(int count, boolean sorted) {
        return createExpSelection(count, sorted, null);
    }
    
    public static List<BigDecimal> createExpSelection(int count, boolean sorted, List<BigDecimal> selectionList) {
        if (selectionList == null) {
            selectionList = new ArrayList<>(count);
        }else{
            if (selectionList.size() == count) {
                for (int i = 0; i < count; ++i) {
                    selectionList.set(i, getExpValue());
                }
                
                if (sorted) {
                    selectionList.sort(null);
                }
                
                return selectionList;
            }
            selectionList.clear();
        }
        
        for (int i = 0; i < count; i++) {
            selectionList.add(getExpValue());
        }
        
        if (sorted) {
            selectionList.sort(null);
        }
        
        return selectionList;
    } 
    
    
    public static List<BigDecimal> createWeibullSelection(
            int count, 
            boolean sorted, 
            BigDecimal weibullParam)
            throws InvalidParameterException {
        return createWeibullSelection(count, sorted, weibullParam, null);
    }
    
    public static List<BigDecimal> createWeibullSelection(
            int count, 
            boolean sorted, 
            BigDecimal weibullParam, 
            List<BigDecimal> selectionList)
            throws InvalidParameterException {
        if (selectionList == null) {
            selectionList = new ArrayList<>(count);
        }else{
            if (selectionList.size() == count) {
                for (int i = 0; i < count; ++i) {
                    selectionList.set(i, getWeibullValue(weibullParam));
                }
                
                if (sorted) {
                    selectionList.sort(null);
                }
                
                return selectionList;
            }
            
            selectionList.clear();
        }
        
        for (int i = 0; i < count; i++) {
            selectionList.add(i, getWeibullValue(weibullParam));
        }
        
        if (sorted) {
            selectionList.sort(null);
        }
        
        return selectionList;
    }
}
