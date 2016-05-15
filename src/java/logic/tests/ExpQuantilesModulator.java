package logic.tests;

import entities.exceptions.UnknownTestTypeException;
import entities.exceptions.WrongSelectionSizeException;
import hibernate.logic.Quantile;
import java.util.ArrayList;
import java.util.List;
import logic.selections.SelectionsModulator;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.SignificanceLevel.IndexesPair;
import hibernate.logic.TestType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import logic.MathFunctionsHandler;
import taskmanager.ProcessObserver;


public final class ExpQuantilesModulator {
    
    private ExpQuantilesModulator() {}
    
    public static QuantilePair modulateQuantiles(
            final TestType type,
            final SignificanceLevel signLevel,
            final int iterCount, 
            final int selSize) 
            throws WrongSelectionSizeException, UnknownTestTypeException {
        return modulateQuantiles(type, signLevel, iterCount, selSize, null);
    }
    
    public static QuantilePair modulateQuantiles(
            final TestType type,
            final SignificanceLevel signLevel,
            final int iterCount, 
            final int selSize, 
            ProcessObserver<Quantile> observer) 
            throws WrongSelectionSizeException, UnknownTestTypeException {
        List<BigDecimal> resultsList = new ArrayList<>(iterCount);
        
        List<BigDecimal> expSelection = null;
        for (int i = 0; i < iterCount; ++i) {
            expSelection = SelectionsModulator.createExpSelection(selSize, true, expSelection);
            BigDecimal value = ExpTestsExecutor.getTestValue(expSelection, type);
            resultsList.add(value);
            ProcessObserver.incrementIfExists(observer);
        }
        
        resultsList.sort(null);
        
        IndexesPair indexesPair = signLevel.getIndexesPair(iterCount);
        BigDecimal first = MathFunctionsHandler.round(resultsList.get(indexesPair.first)); 
        BigDecimal second = MathFunctionsHandler.round(resultsList.get(indexesPair.second));
        
        return new QuantilePair(first, second);
    }
    
    public static Map<TestType, Map<SignificanceLevel, QuantilePair> > modulateQuantiles(
            final Map<TestType, Set<SignificanceLevel> > typesForQuantiles,
            final int iterCount, 
            final int selSize, 
            ProcessObserver<Map<String, Map<Integer, QuantilePair> > > observer) 
            throws WrongSelectionSizeException, UnknownTestTypeException {
        Map<TestType, List<BigDecimal> > resultsLists = new TreeMap<>();
        for (Map.Entry<TestType, Set<SignificanceLevel> > typeEntry : typesForQuantiles.entrySet()) {
            List<BigDecimal> resultsList = new ArrayList<>(iterCount);
            resultsLists.put(typeEntry.getKey(), resultsList);
        }
        
        List<BigDecimal> expSelection = null;
        for (int i = 0; i < iterCount; ++i) {
            expSelection = SelectionsModulator.createExpSelection(selSize, true, expSelection);
            
            for (Map.Entry<TestType, List<BigDecimal> > typeEntry : resultsLists.entrySet()) {
                BigDecimal value = ExpTestsExecutor.getTestValue(expSelection, typeEntry.getKey());
                typeEntry.getValue().add(value);
            }
            
            ProcessObserver.incrementIfExists(observer);
        }
        
        IndexesPair indexesPair = null;
        Map<TestType, Map<SignificanceLevel, QuantilePair> > results = new TreeMap<>();
        for (Map.Entry<TestType, Set<SignificanceLevel> > typeEntry : typesForQuantiles.entrySet()) {
            List<BigDecimal> resultsList = resultsLists.get(typeEntry.getKey());
            resultsList.sort(null);
            
            Map<SignificanceLevel, QuantilePair> levelsForQuantiles = new TreeMap<>();
            results.put(typeEntry.getKey(), levelsForQuantiles);
            
            for (SignificanceLevel level : typeEntry.getValue()) {
                indexesPair = level.getIndexesPair(iterCount, indexesPair);
                BigDecimal first = MathFunctionsHandler.round(resultsList.get(indexesPair.first)); 
                BigDecimal second = MathFunctionsHandler.round(resultsList.get(indexesPair.second));
                
                levelsForQuantiles.put(level, new QuantilePair(first, second));
            }
            
        }
        
        return results;
    }
    
}
