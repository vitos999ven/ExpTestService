package logic.tests;

import controllers.RequestParams;
import entities.exceptions.UnknownTestTypeException;
import entities.exceptions.WrongSelectionSizeException;
import logic.MathFunctionsHandler;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import entities.TestResult;
import entities.exceptions.ActionException;
import entities.exceptions.actionreasons.DataNotReadyYet;
import entities.exceptions.actionreasons.InvalidParameter;
import entities.exceptions.actionreasons.InternalError;
import entities.exceptions.actionreasons.NoDependentData;
import entities.exceptions.actionreasons.WrongDBData;
import hibernate.logic.IterationsCount;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.nevec.rjm.BigDecimalMath;
import taskmanager.ProcessObserver;
import taskmanager.TaskManager;


public class ExpTestsExecutor {
    
    private static final BigDecimal MORAN_CONST = new BigDecimal("0.5772");
    
    private static final int MIN_SELECTION_SIZE = 10;
    
    private static final Logger logger = Logger.getLogger(ExpTestsExecutor.class);
    
    public static TestResult makeTestForSelection(
            List<BigDecimal> selection, 
            final TestType type, 
            final SignificanceLevel signLevel) 
            throws ActionException {
        if (selection == null) {
                throw new ActionException(new WrongDBData(RequestParams.SELECTION_VALUES));
        }
        
        Factory factory = Factory.getInstance();
        
        int defaultIterCount = type.getDefaultIterCount();
        IterationsCount count = factory.getIterationsCountsDAO().getIterationsCount(type, defaultIterCount);
        if (count == null) {
            throw new ActionException(new NoDependentData(RequestParams.ITERATIONS_COUNT));
        }
        
        Quantile quantile = factory.getQuantilesDAO().getQuantile(type, signLevel, count, selection.size());
        
        if (quantile == null) {
            ProcessObserver<Quantile> observer = TaskManager.getInstance().doQuantilesModulation(
                    type, 
                    signLevel,
                    defaultIterCount, 
                    selection.size());
            
            if(observer == null) {
                String message = "No observer for default quantile modulation";
                logger.error(message);
                throw new ActionException(new InternalError(message));
            }
            
            try {
                quantile = observer.get();
                
                if (quantile == null) {
                    String message = "No quantile for default quantile modulation";
                    logger.error(message);
                    throw new ActionException(new InternalError(message));
                }
            } catch (TimeoutException ex) {
                logger.error(ex);
                throw new ActionException(new DataNotReadyYet(observer.status()));
            } catch (ExecutionException ex) {
                logger.error("ExecutionException while making test for selection", ex.getCause());
                if (ex.getCause() instanceof WrongSelectionSizeException){
                    throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
                } else if (ex.getCause() instanceof UnknownTestTypeException){
                    throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
                }
                throw new ActionException(new InternalError(
                        (ex.getCause() != null) ? ex.getCause().getLocalizedMessage() : ex.getLocalizedMessage()));
            } catch (InterruptedException ex) {
                logger.error(ex);
                throw new ActionException(new InternalError(ex.getLocalizedMessage()));
            }
        }
        
        BigDecimal resultValue;
        try {
            if (type.getForSorted()) {
                selection = new ArrayList<>(selection);
                selection.sort(null);
            }
            
            resultValue = getTestValue(selection, type);
        } catch (WrongSelectionSizeException ex) {
            throw new ActionException(new InvalidParameter(RequestParams.SELECTION_SIZE));
        } catch (UnknownTestTypeException ex) {
            throw new ActionException(new InvalidParameter(RequestParams.TEST_TYPE));
        }
        
        boolean result = checkValue(resultValue, quantile);
        
        return new TestResult(
                result, 
                MathFunctionsHandler.round(resultValue), 
                MathFunctionsHandler.round(quantile.getFirstValue()), 
                MathFunctionsHandler.round(quantile.getSecondValue())
            );
    }
    
    public static boolean checkValue(BigDecimal value, Quantile quantile){
        if (value == null || quantile == null) {
            return false;
        }
        
        return (value.compareTo(quantile.getFirstValue()) >= 0 && value.compareTo(quantile.getSecondValue()) <= 0);
    }
    
    public static BigDecimal getTestValue(List<BigDecimal> selection, TestType type) 
            throws WrongSelectionSizeException, UnknownTestTypeException {
        BigDecimal result = MathFunctionsHandler.ZERO;
        
        switch (type.getType()) {
            case "gini":
                result = getGiniTestValue(selection);
                break;
            case "greenwood":
                result = getGreenwoodTestValue(selection);
                break;
            case "frocini":
                result = getFrociniTestValue(selection);
                break;
            case "kimbermichel":
                result = getKimberMichelTestValue(selection);
                break;
            case "moran":
                result = getMoranTestValue(selection);
                break;
            case "rao":
                result = getRaoTestValue(selection);
                break;
            case "shapirowilk":
                result = getShapiroWilkTestValue(selection);
                break;
            case "sherman":
                result = getShermanTestValue(selection);
                break;
            default:
                throw new UnknownTestTypeException();
        }
        
        return result;
    }
    
    private static BigDecimal getGiniTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        return MathFunctionsHandler.ZERO;
    }
    
    private static BigDecimal getGreenwoodTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        BigDecimal size = BigDecimal.valueOf(selection.size());
        BigDecimal sum = MathFunctionsHandler.getSum(selection);
        BigDecimal extra = MathFunctionsHandler.ZERO;
        
        for (BigDecimal value : selection) {
            extra = extra.add(value.multiply(value));
        }
        
        return MathFunctionsHandler.divide(size.multiply(extra), sum.multiply(sum));
    }
    
    private static BigDecimal getFrociniTestValue(List<BigDecimal> selection)
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        BigDecimal size = BigDecimal.valueOf(selection.size());
        BigDecimal average = MathFunctionsHandler.getAverage(selection);
        BigDecimal extra = MathFunctionsHandler.ZERO;
        
        BigDecimal i = MathFunctionsHandler.ZERO;
        BigDecimal half = MathFunctionsHandler.HALF;
        for (BigDecimal value : selection) {
            i = i.add(MathFunctionsHandler.ONE);
            extra = extra.add(
                    MathFunctionsHandler.ONE.subtract(
                            BigDecimalMath.exp(MathFunctionsHandler.divide(value.negate(), average))
                    ).subtract(
                            MathFunctionsHandler.divide(i.subtract(half), size)
                    ).abs()
            );
        }
        
        return MathFunctionsHandler.divide(extra, BigDecimalMath.sqrt(size));
    }
    
    private static BigDecimal getKimberMichelTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        
        return MathFunctionsHandler.ZERO;
    }
    
    
    private static BigDecimal getMoranTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        BigDecimal average = MathFunctionsHandler.getAverage(selection);
        BigDecimal sum = MathFunctionsHandler.ZERO;
        
        Iterator<BigDecimal> iterator = selection.iterator();
        
        while(iterator.hasNext())
        {
            BigDecimal divide = MathFunctionsHandler.divide(iterator.next(), average);
            BigDecimal log = MathFunctionsHandler.log(divide);
            sum = sum.add(log);
        }
        
        return MathFunctionsHandler.divide(sum, BigDecimal.valueOf(selection.size())).add(MORAN_CONST);
    }
    
    private static BigDecimal getRaoTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        return MathFunctionsHandler.ZERO;
    }
    
    private static BigDecimal getShapiroWilkTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        BigDecimal sum = MathFunctionsHandler.getSum(selection);
        BigDecimal size = BigDecimal.valueOf(selection.size());
        BigDecimal average = MathFunctionsHandler.divide(sum, size);
        BigDecimal extra = MathFunctionsHandler.ZERO;
        
        Iterator<BigDecimal> iterator = selection.iterator();
        
        while(iterator.hasNext())
        {
            extra = extra.add(iterator.next().subtract(average).pow(2));
        }
        
        return MathFunctionsHandler.divide(extra, sum.multiply(sum));
    }
    
    private static BigDecimal getShermanTestValue(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        checkSelection(selection);
        
        BigDecimal size = BigDecimal.valueOf(selection.size());
        BigDecimal average = MathFunctionsHandler.getAverage(selection);
        BigDecimal extra = MathFunctionsHandler.ZERO;
        
        Iterator<BigDecimal> iterator = selection.iterator();
        
        while(iterator.hasNext())
        {
            extra = extra.add(iterator.next().subtract(average).abs());
        }
        
        return MathFunctionsHandler.divide(extra, size.multiply(average).multiply(MathFunctionsHandler.TWO));
    }
    
    private static void checkSelection(List<BigDecimal> selection) 
            throws WrongSelectionSizeException {
        if (selection.size() < MIN_SELECTION_SIZE) {
            throw new WrongSelectionSizeException();
        }
    }
}
