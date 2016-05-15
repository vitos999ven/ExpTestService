package hibernate.DAO.Impl;

import hibernate.DAO.Impl.DAOFunctions.Preparer;
import hibernate.DAO.IterationsCountsDAO;
import hibernate.logic.IterationsCount;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class IterationsCountsDAOImpl implements IterationsCountsDAO {

    private static class PreparerHolder {
        public static Preparer<IterationsCount> preparerOnRemove = (IterationsCount element, Session session) -> {
            Factory.getInstance().getQuantilesDAO().removeQuantiles(element, session);
        };
    }
    
    @Override
    public boolean addIterationsCount(IterationsCount iterCount) {
        if(iterCount == null || getIterationsCount(iterCount.getId()) != null) {
            return false;
        }
        
        return DAOFunctions.add(iterCount);
    }

    @Override
    public boolean updateIterationsCount(IterationsCount iterCount) {
        if(iterCount == null || getIterationsCount(iterCount.getId()) == null) {
            return false;
        }
        
        return DAOFunctions.update(iterCount);
    }

    @Override
    public IterationsCount getIterationsCount(Long id) {
        if (id == null) {
            return null;
        }
        
        return DAOFunctions.get(IterationsCount.class,
                Arrays.asList(
                         Restrictions.eq("id", id)
                    ));
    }
    
    @Override
    public IterationsCount getIterationsCount(TestType testType, int iterCount) {
        if (testType == null) {
            return null;
        }
        
        return DAOFunctions.get(IterationsCount.class,
                Arrays.asList(
                         Restrictions.eq("testType", testType),
                         Restrictions.eq("count", iterCount)
                    ));
    }

    @Override
    public boolean removeIterationsCount(Long id) {
        IterationsCount iterCount = getIterationsCount(id);
        if (iterCount == null) {
            return false;
        }
       
        return DAOFunctions.remove(iterCount, PreparerHolder.preparerOnRemove);
    }

    @Override
    public boolean removeIterationsCount(TestType testType, int count) {
        IterationsCount iterCount = getIterationsCount(testType, count);
        if (iterCount == null) {
            return false;
        }
       
        return DAOFunctions.remove(iterCount, PreparerHolder.preparerOnRemove);
    }
    
    @Override
    public List<IterationsCount> getIterationsCounts(TestType testType) {
        if (testType == null) {
            return null;
        }
        
        return DAOFunctions.criteria(IterationsCount.class,
                Arrays.asList(
                         Restrictions.eq("testType", testType)
                    ));
    }

    @Override
    public List<IterationsCount> getAllIterationsCounts() {
        return DAOFunctions.criteria(IterationsCount.class, null);
    }

    @Override
    public void removeIterationsCounts(TestType testType) {
        DAOFunctions.removeAll(getIterationsCounts(testType), PreparerHolder.preparerOnRemove);
    }
    
    @Override
    public void removeIterationsCounts(TestType testType, Session session) {
        DAOFunctions.removeAll(getIterationsCounts(testType), session, false, PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeAllIterationsCounts() {
        DAOFunctions.removeAll(getAllIterationsCounts(), PreparerHolder.preparerOnRemove);
    }
    
}
