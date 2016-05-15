package hibernate.util;

import hibernate.DAO.Impl.IterationsCountsDAOImpl;
import hibernate.DAO.Impl.PowersDAOImpl;
import hibernate.DAO.Impl.QuantilesDAOImpl;
import hibernate.DAO.Impl.SelectionsDAOImpl;
import hibernate.DAO.Impl.SignificanceLevelsDAOImpl;
import hibernate.DAO.Impl.TestTypesDAOImpl;
import hibernate.DAO.IterationsCountsDAO;
import hibernate.DAO.PowersDAO;
import hibernate.DAO.QuantilesDAO;
import hibernate.DAO.SelectionsDAO;
import hibernate.DAO.SignificanceLevelsDAO;
import hibernate.DAO.TestTypesDAO;


public class Factory {

    private static class FactoryHolder{
        private final static Factory instance = new Factory();
    }
    
    private static class SelectionsDAOHolder{
        private final static SelectionsDAO selectionsDAO = new SelectionsDAOImpl();
    }
    
    private static class QuantilesDAOHolder{
        private final static QuantilesDAO quantilesDAO = new QuantilesDAOImpl();
    }
    
    private static class TestTypesDAOHolder{
        private final static TestTypesDAO testTypesDAO = new TestTypesDAOImpl();
    }
    
    private static class SignificanceLevelsDAOHolder{
        private final static SignificanceLevelsDAO significanceLevelsDAO = new SignificanceLevelsDAOImpl();
    }
    
    private static class IterationsCountsDAOHolder{
        private final static IterationsCountsDAO iterationsCountsDAO = new IterationsCountsDAOImpl();
    }
    
    private static class PowersDAOHolder{
        private final static PowersDAO powersDAO = new PowersDAOImpl();
    }
    
    public static Factory getInstance(){
        return FactoryHolder.instance;
    }
    
    
    public SelectionsDAO getSelectionsDAO(){
        return SelectionsDAOHolder.selectionsDAO;
    }
     
    public QuantilesDAO getQuantilesDAO(){
        return QuantilesDAOHolder.quantilesDAO;
    }
    
    public TestTypesDAO getTestTypesDAO(){
        return TestTypesDAOHolder.testTypesDAO;
    }
    
    public SignificanceLevelsDAO getSignificanceLevelsDAO(){
        return SignificanceLevelsDAOHolder.significanceLevelsDAO;
    }
    
    public IterationsCountsDAO getIterationsCountsDAO(){
        return IterationsCountsDAOHolder.iterationsCountsDAO;
    }
    
    public PowersDAO getPowersDAO(){
        return PowersDAOHolder.powersDAO;
    }
}
