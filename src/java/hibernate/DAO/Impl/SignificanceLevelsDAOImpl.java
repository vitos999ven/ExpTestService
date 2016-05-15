package hibernate.DAO.Impl;

import hibernate.DAO.SignificanceLevelsDAO;
import hibernate.logic.SignificanceLevel;
import hibernate.util.Factory;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class SignificanceLevelsDAOImpl implements SignificanceLevelsDAO {

    private static class PreparerHolder {
        
        public static DAOFunctions.Preparer<SignificanceLevel> preparerOnRemove = (SignificanceLevel element, Session session) -> {
            Factory.getInstance().getQuantilesDAO().removeQuantiles(element, session);
        };
    }
    
    @Override
    public boolean addSignificanceLevel(SignificanceLevel level) {
        return DAOFunctions.add(level);
    }

    @Override
    public boolean containsSignificanceLevel(SignificanceLevel level) {
        if (level == null) {
            return false;
        }
        
        level = DAOFunctions.get(SignificanceLevel.class,
                Arrays.asList(
                         Restrictions.eq("level", level.getLevel())
                    ));
        
        return (level != null);
    }

    @Override
    public boolean removeSignificanceLevel(SignificanceLevel level) {
        return DAOFunctions.remove(level, PreparerHolder.preparerOnRemove);
    }

    @Override
    public List<SignificanceLevel> getAllSignificanceLevels() {
        return DAOFunctions.criteria(SignificanceLevel.class, null);
    }

    @Override
    public void removeAllSignificanceLevels() {
        List<SignificanceLevel> levels = getAllSignificanceLevels();
        
        DAOFunctions.removeAll(levels, PreparerHolder.preparerOnRemove);
    }

}
