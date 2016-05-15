package services;

import entities.ChartArray;
import entities.IterationsCountsSet;
import entities.QuantilesMap;
import java.util.SortedSet;
import entities.SelectionData;
import entities.SelectionInfo;
import entities.TestResultsMap;
import entities.WeibullAlternativeResult;
import entities.exceptions.ActionException;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import logic.tests.QuantilePair;


public interface ApplicationService {
    
    public List<TestType> getTestTypes() throws ActionException;
    public void addTestType(String type, String typeName, boolean forSorted) throws ActionException;
    public void removeTestType(String type) throws ActionException;
    
    public IterationsCountsSet getIterationsCounts(String type) throws ActionException;
    public void addIterationsCount(String type, int count) throws ActionException;
    public void removeIterationsCount(String type, int count) throws ActionException;
    
    public List<SignificanceLevel> getSignificanceLevels() throws ActionException;
    public void addSignificanceLevel(int level) throws ActionException;
    public void removeSignificanceLevel(int level) throws ActionException;
    
    public Quantile modulateQuantile(
            String type,  
            int iterCount,
            int signLevel, 
            int selSize) throws ActionException;
    public Quantile getOrModulateQuantile(
            String type,  
            int iterCount,
            int signLevel, 
            int selSize) throws ActionException;
    public List<Quantile> getQuantiles(String type, int iterCount, int signLevel) throws ActionException;
    public List<Quantile> getQuantiles(
            String type, 
            int iterCount, 
            int signLevel,
            int minSelSize,
            int maxSelSize) throws ActionException;
    
    public Quantile modulateDefaultQuantile(
            String type,  
            int signLevel, 
            int selSize) throws ActionException;
    public Quantile getOrModulateDefaultQuantile(
            String type,  
            int signLevel, 
            int selSize) throws ActionException;
    
    public Map<String, Map<Integer, QuantilePair> > getOrModulateMultipleQuantiles(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException;
    
    public QuantilesMap getQuantiles(
            String type, 
            int iterCount, 
            int minSelSize, 
            int quantilesCount) throws ActionException;
    
    public Double getOrModulatePower(
            String testType,  
            int iterCount,
            int signLevel, 
            int selSize,
            String alternativeType,
            HttpServletRequest request,
            boolean ifNotExists) throws ActionException;
    
    public Map<Integer, ChartArray> getOrModulateWeibullPower(
            String testType,  
            int iterCount, 
            int selSize,
            boolean ifNotExists) throws ActionException;
    public Map<String, Map <Integer, ChartArray> > getOrModulateWeibullPower(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException;
    
    public Map<String, Map<Integer, Double> > getOrModulateTrueAcceptancePower(
            String[] testTypes,
            int iterCount,
            int selSize,
            boolean ifNotExists) throws ActionException;
    
    public SortedSet<SelectionInfo> getSelectionsInfo() throws ActionException;
    public SelectionData getSelection(String name) throws ActionException;
    public SelectionInfo addSelection(String name, String values) throws ActionException;
    public SelectionInfo modulateSelection(
            String name, 
            int size, 
            String selType, 
            boolean sorted,
            HttpServletRequest request) throws ActionException;
    public SelectionInfo appendToSelection(String name, String values) throws ActionException;
    public void removeSelection(String name) throws ActionException;
    public SelectionInfo sortSelection(String name) throws ActionException;
    public TestResultsMap checkSelectionByTests(
            final String selectionName, 
            final SortedSet<String> testTypes, 
            final int signLevel) throws ActionException;
    public WeibullAlternativeResult createResultsArrayAgainstWeibull(
            final int iterCount,
            final int signLevel,
            final int selSize,
            final String resultsJson
            ) throws ActionException;
    
}
