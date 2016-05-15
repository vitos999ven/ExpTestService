package hibernate.DAO.Impl;

import hibernate.DAO.QuantilesDAO;
import hibernate.logic.IterationsCount;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import hibernate.util.Factory;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class QuantilesDAOImpl implements QuantilesDAO {

    private static class PreparerHolder {
        
        public static DAOFunctions.Preparer<Quantile> preparerOnRemove = (Quantile element, Session session) -> {
            Factory.getInstance().getPowersDAO().removePowers(element, session);
        };
        
    }
    
    @Override
    public boolean addQuantile(Quantile quantile) {
        if(quantile == null || getQuantile(
                quantile.getTestType(), 
                quantile.getSignificanceLevel(), 
                quantile.getIterationsCount(),
                quantile.getSelectionSize()) != null) {
            return false;
        }
        
        return DAOFunctions.add(quantile);
    }

    @Override
    public boolean updateQuantile(Quantile quantile) {
        if(quantile == null || getQuantile(quantile.getId()) == null) {
            return false;
        }
        
        return DAOFunctions.update(quantile);
    }

    @Override
    public Quantile getQuantile(Long id) {
        if (id == null) {
            return null;
        }
        
        return DAOFunctions.get(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("id", id)
                    ));
    }
    
    @Override
    public Quantile getQuantile(
            TestType type, 
            SignificanceLevel signLevel, 
            IterationsCount iterCount, 
            Integer selSize) {
        if (type == null || signLevel == null || selSize == null || iterCount == null) {
            return null;
        }
        
        return DAOFunctions.get(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("testType", type),
                         Restrictions.eq("signLevel", signLevel),
                         Restrictions.eq("iterCount", iterCount),
                         Restrictions.eq("selSize", selSize)
                    ));
    }

    @Override
    public boolean removeQuantile(Long id) {
        Quantile quantile = getQuantile(id);
        if (quantile == null) {
            return false;
        }
        
        return DAOFunctions.remove(quantile, PreparerHolder.preparerOnRemove);
    }
    
    @Override
    public boolean removeQuantile(
            TestType type, 
            SignificanceLevel signLevel, 
            IterationsCount iterCount, 
            Integer selSize) {
        Quantile quantile = getQuantile(type, signLevel, iterCount, selSize);
        if (quantile == null) {
            return false;
        }
        
        return DAOFunctions.remove(quantile, PreparerHolder.preparerOnRemove);
    }

    @Override
    public List<Quantile> getQuantiles(
            TestType type, 
            SignificanceLevel signLevel, 
            IterationsCount iterCount) {
        if (type == null || signLevel == null || iterCount == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("testType", type),
                         Restrictions.eq("signLevel", signLevel),
                         Restrictions.eq("iterCount", iterCount)
                    ));
    }

    @Override
    public List<Quantile> getQuantiles(
            TestType type, 
            SignificanceLevel signLevel, 
            IterationsCount iterCount, 
            int minSelSize, 
            int maxSelSize) {
        if (type == null || signLevel == null || iterCount == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("testType", type),
                         Restrictions.eq("signLevel", signLevel),
                         Restrictions.eq("iterCount", iterCount),
                         Restrictions.between("selSize", minSelSize, maxSelSize)
                    ));
    }

    @Override
    public List<Quantile> getQuantiles(TestType type) {
        if (type == null) {
            return null;
        }
        
        System.out.println(type.getType());
        return DAOFunctions.criteria(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("testType", type)
                    ));
    }

    @Override
    public List<Quantile> getQuantiles(SignificanceLevel signLevel) {
        if (signLevel == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("signLevel", signLevel)
                    ));
    }

    @Override
    public List<Quantile> getQuantiles(IterationsCount iterCount) {
        if (iterCount == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Quantile.class,
                Arrays.asList(
                         Restrictions.eq("iterCount", iterCount)
                    ));
    }

    @Override
    public List<Quantile> getAllQuantiles() {
        return DAOFunctions.criteria(Quantile.class, null);
    }

    @Override
    public void removeQuantiles(TestType type) {
        DAOFunctions.removeAll(getQuantiles(type), PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeQuantiles(SignificanceLevel signLevel) {
        DAOFunctions.removeAll(getQuantiles(signLevel), PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeQuantiles(IterationsCount iterCount) {
        DAOFunctions.removeAll(getQuantiles(iterCount), PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeQuantiles(TestType type, Session session) {
        List<Quantile> list = getQuantiles(type);
        DAOFunctions.removeAll(list, session, false, PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeQuantiles(SignificanceLevel signLevel, Session session) {
        DAOFunctions.removeAll(getQuantiles(signLevel), session, false, PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeQuantiles(IterationsCount iterCount, Session session) {
        DAOFunctions.removeAll(getQuantiles(iterCount), session, false, PreparerHolder.preparerOnRemove);
    }
    
    @Override
    public void removeAllQuantiles() {
        DAOFunctions.removeAll(getAllQuantiles(), PreparerHolder.preparerOnRemove);
    }

}
