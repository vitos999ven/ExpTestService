package hibernate.DAO.Impl;

import entities.alternatives.SelectionParams;
import entities.alternatives.SelectionsEnum;
import hibernate.DAO.PowersDAO;
import hibernate.logic.Power;
import hibernate.logic.Quantile;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class PowersDAOImpl implements PowersDAO {

    private static class PreparerHolder {
        public static DAOFunctions.Preparer<Power> preparerOnRemove = null;
    }
    
    @Override
    public boolean addPower(Power power) {
        return DAOFunctions.add(power);
    }

    @Override
    public boolean updatePower(Power power) {
        return DAOFunctions.update(power);
    }

    @Override
    public Power getPower(Long id) {
        if (id == null) {
            return null;
        }
        
        return DAOFunctions.get(Power.class,
                Arrays.asList(
                         Restrictions.eq("id", id)
                    ));
    }

    @Override
    public Power getPower(Quantile quantile, SelectionParams params) {
        if (quantile == null || params == null) {
            return null;
        }
        
        SelectionsEnum alternative = params.getSelectionType();
        if(alternative == null) {
            return null;
        } 
        
        String paramsString = params.toJson();
        if (paramsString == null) {
            return null;
        }
        
        return DAOFunctions.get(Power.class,
                Arrays.asList(
                         Restrictions.eq("quantile", quantile),
                         Restrictions.eq("alternative", alternative),
                         Restrictions.eq("paramsString", paramsString)
                    ));
    }

    @Override
    public boolean removePower(Long id) {
        Power power = getPower(id);
        if (power == null) {
            return false;
        }
       
        return DAOFunctions.remove(power, PreparerHolder.preparerOnRemove);
    }

    @Override
    public boolean removePower(Quantile quantile, SelectionParams params) {
        Power power = getPower(quantile, params);
        if (power == null) {
            return false;
        }
       
        return DAOFunctions.remove(power, PreparerHolder.preparerOnRemove);
    }

    @Override
    public List<Power> getPowers(Quantile quantile) {
        if (quantile == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Power.class,
                Arrays.asList(
                         Restrictions.eq("quantile", quantile)
                    ));
    }

    @Override
    public List<Power> getPowers(Quantile quantile, SelectionsEnum alternative) {
        if (quantile == null || alternative == null) {
            return null;
        }
        
        return DAOFunctions.criteria(Power.class,
                Arrays.asList(
                         Restrictions.eq("quantile", quantile),
                         Restrictions.eq("alternative", alternative)   
                    ));
    }

    @Override
    public List<Power> getAllPowers() {
        return DAOFunctions.criteria(Power.class, null);
    }

    @Override
    public void removePowers(Quantile quantile) {
        DAOFunctions.removeAll(getPowers(quantile), PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removePowers(Quantile quantile, Session session) {
        DAOFunctions.removeAll(getPowers(quantile), session, false, PreparerHolder.preparerOnRemove);
    }

    @Override
    public void removeAllPowers() {
        DAOFunctions.removeAll(getAllPowers(), PreparerHolder.preparerOnRemove);
    }

}
