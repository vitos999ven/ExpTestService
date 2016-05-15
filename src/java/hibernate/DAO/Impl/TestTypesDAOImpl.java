package hibernate.DAO.Impl;

import hibernate.DAO.TestTypesDAO;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class TestTypesDAOImpl implements TestTypesDAO {

    private static class PreparerHolder {
        public static DAOFunctions.Preparer<TestType> preparerOnRemove = (TestType element, Session session) -> {
            Factory factory = Factory.getInstance();
            factory.getQuantilesDAO().removeQuantiles(element, session);
            factory.getIterationsCountsDAO().removeIterationsCounts(element, session);
        };
    }
    
    @Override
    public boolean addTestType(TestType type) {
        return DAOFunctions.add(type);
    }

    @Override
    public boolean containsTestType(String typeName) {
        if (typeName == null) {
            return false;
        }
        
        return (getTestType(typeName) != null);
    }
    
    @Override
    public TestType getTestType(String type) {
        return DAOFunctions.get(TestType.class,
                Arrays.asList(
                         Restrictions.eq("type", type)
                    ));
    }
    
    @Override
    public boolean updateTestType(TestType type) {
        return DAOFunctions.update(type);
    }
    
    @Override
    public boolean removeTestType(TestType type) {
        return DAOFunctions.remove(type, PreparerHolder.preparerOnRemove);
    }

    @Override
    public List<TestType> getAllTestTypes() {
        return DAOFunctions.criteria(TestType.class, null);
    }

    @Override
    public void removeAllTestTypes() {
        DAOFunctions.removeAll(getAllTestTypes(), PreparerHolder.preparerOnRemove);
    }

}
