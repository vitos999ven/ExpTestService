package logic.tests;

import entities.alternatives.SelectionParams;
import entities.alternatives.SelectionsEnum;
import entities.alternatives.WeibullParams;
import entities.exceptions.UnknownTestTypeException;
import entities.exceptions.WrongSelectionSizeException;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import logic.MathFunctionsHandler;
import logic.selections.SelectionsModulator;
import taskmanager.ProcessObserver;


public final class PowerModulator {
    
    public static final List<SelectionParams> weibulParamsList = new LinkedList<>();
    
    static {
        BigDecimal step = new BigDecimal("0.05");
        BigDecimal curr = new BigDecimal("0.2");
        while(curr.compareTo(MathFunctionsHandler.ONE) < 0) {
            weibulParamsList.add(new WeibullParams(curr));
            curr = curr.add(step);
        }
        curr = curr.add(step);
        while(curr.compareTo(MathFunctionsHandler.FOUR) <= 0) {
            weibulParamsList.add(new WeibullParams(curr));
            curr = curr.add(step);
        }
    }
    
    private PowerModulator() {}
    
    public static Double modulatePower(
            final Quantile quantile,
            final SelectionParams params,
            final int powerIterCount
            ) throws WrongSelectionSizeException, UnknownTestTypeException {
        return modulatePower(quantile, params, powerIterCount, null);
    }
    
    public static Double modulatePower(
            final Quantile quantile,
            final SelectionParams params,
            final int powerIterCount,
            final ProcessObserver<Power> observer
            ) throws WrongSelectionSizeException, UnknownTestTypeException {
        
        SelectionsEnum alternative = params.getSelectionType();
        boolean typeForSorted = quantile.getTestType().getForSorted();
        
        AlternativeCreator creator = null;
        
        switch(alternative) {
            case EXP:
                creator = (old) -> SelectionsModulator.createExpSelection(quantile.getSelectionSize(), typeForSorted, old);
                        
                break;
            case WEIBULL:
                WeibullParams wParams = (WeibullParams) params;
                creator = (old) -> SelectionsModulator.createWeibullSelection(
                        quantile.getSelectionSize(),
                        typeForSorted, 
                        wParams.getWeibullParam(),
                        old);
                break; 
            default:
                throw new InvalidParameterException("alternative");
        }
        
        int failedResultsCount = 0;
        
        List<BigDecimal> selection = null;
        for (int i = 0; i < powerIterCount; ++i) {
            selection = creator.createNew(selection);
            BigDecimal value = ExpTestsExecutor.getTestValue(selection, quantile.getTestType());
            
            if (!ExpTestsExecutor.checkValue(value, quantile)) {
                ++failedResultsCount;
            }
            
            ProcessObserver.incrementIfExists(observer);
        }
        
        double power = (double)failedResultsCount / powerIterCount;
                
        if (alternative == SelectionsEnum.EXP) {
            power = 1.0 - power;
        }
        return power;
    }
    
    public static Map<Quantile, Map<SelectionParams, Double> > modulatePower(
            final int selSize,
            final List<Quantile> quantiles,
            final List<SelectionParams> paramsList,
            final int powerIterCount,
            final ProcessObserver<Map<Quantile, Map<SelectionParams, Double> > > observer
            ) throws WrongSelectionSizeException, UnknownTestTypeException {
        if (quantiles == null) {
            throw new InvalidParameterException("quantiles");
        }
        
        if (paramsList == null) {
            throw new InvalidParameterException("paramsList");
        }
        
        Map<Quantile, Map<SelectionParams, Double> > result = new TreeMap<>();
        
        final GenericValue<Boolean> forSorted = new GenericValue<>(Boolean.FALSE);
        
        Map<Quantile, GenericValue<Integer> > failedResultsCounts = new TreeMap<>();
        
        quantiles.stream().forEach((quantile) -> {
            Integer curSelSize = quantile.getSelectionSize();
            if (selSize != curSelSize) {
                throw new InvalidParameterException("quantiles");
            }
            
            if (!forSorted.getValue() && quantile.getTestType().getForSorted()) {
                forSorted.setValue(Boolean.TRUE);
            }
            
            failedResultsCounts.put(quantile, new GenericValue<>(0));
        });
        
        final GenericValue<Boolean> lastIsStrong = new GenericValue<>(Boolean.TRUE);
        
        List<BigDecimal> selection = null;
        
        for (SelectionParams params : paramsList) {
            SelectionsEnum alternative = params.getSelectionType();
            
            AlternativeCreator creator = null;

            switch (alternative) {
                case EXP:
                    creator = (old) -> SelectionsModulator.createExpSelection(
                            selSize,
                            forSorted.getValue(),
                            old);
                    lastIsStrong.setValue(Boolean.FALSE);
                    break;
                case WEIBULL:
                    WeibullParams wParams = (WeibullParams) params;
                    creator = (old) -> SelectionsModulator.createWeibullSelection(
                            selSize,
                            forSorted.getValue(),
                            wParams.getWeibullParam(),
                            old);
                    if (wParams.getWeibullParam().compareTo(MathFunctionsHandler.HALF) <= 0) {
                        lastIsStrong.setValue(Boolean.FALSE);
                    }
                    break;
                default:
                    throw new InvalidParameterException("alternative");
            }
            
            if (!lastIsStrong.getValue()) {
                for (int i = 0; i < powerIterCount; ++i) {
                    selection = creator.createNew(selection);
                
                    for (Quantile quantile : quantiles) {
                        GenericValue<Integer> count = failedResultsCounts.get(quantile);

                        BigDecimal value = ExpTestsExecutor.getTestValue(selection, quantile.getTestType());

                        if (!ExpTestsExecutor.checkValue(value, quantile)) {
                            count.setValue(count.getValue() + 1);
                        }
                    }

                    ProcessObserver.incrementIfExists(observer);
                }
            } else {
                for (Quantile quantile : quantiles) {
                    GenericValue<Integer> count = failedResultsCounts.get(quantile);
                    count.setValue(powerIterCount);
                }
                
                ProcessObserver.incrementIfExists(observer, powerIterCount);
            }
            
            
            lastIsStrong.setValue(Boolean.TRUE);
            quantiles.stream().forEach((quantile) -> {
                GenericValue<Integer> failedCount = failedResultsCounts.get(quantile);
                Map<SelectionParams, Double> quantilePowers = result.get(quantile);
                if (quantilePowers == null) {
                    quantilePowers = new TreeMap<>();
                    result.put(quantile, quantilePowers);
                }
                
                int failed = failedCount.getValue();
                Double power = (double)failed / powerIterCount;
                
                if (alternative == SelectionsEnum.EXP) {
                    power = 1.0 - power; 
                }
                
                if (lastIsStrong.getValue() && failed < powerIterCount) {
                    lastIsStrong.setValue(Boolean.FALSE);
                }
                
                failedCount.setValue(0);
                
                quantilePowers.put(params, power);
            });
        }
        
        return result;
    }
     
    private interface AlternativeCreator {
        public List<BigDecimal> createNew(List<BigDecimal> old);
    }
    
    private static class GenericValue<T> {
        private T value;
        
        public GenericValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
