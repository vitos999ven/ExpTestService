package hibernate.DAO.Impl;

import hibernate.DAO.SelectionsDAO;
import hibernate.logic.Selection;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


public class SelectionsDAOImpl implements SelectionsDAO {

    private static class PreparerHolder {
        
        public static DAOFunctions.Preparer<Selection> preparerOnRemove = null;
        
    }
    
    @Override
    public boolean addSelection(Selection selection) {
        if(selection == null || getSelection(selection.getName()) != null) {
            return false;
        }
        
        return DAOFunctions.add(selection);
    }

    @Override
    public boolean updateSelection(Selection selection) {
        if(selection == null || getSelection(selection.getName()) == null) {
            return false;
        }
        
        return DAOFunctions.update(selection);
    }

    @Override
    public Selection getSelection(String name) {
        if (name == null) {
            return null;
        }
        
        return DAOFunctions.get(Selection.class,
                Arrays.asList(
                         Restrictions.eq("name", name)
                    ));
    }

    @Override
    public boolean removeSelection(String name) {
        Selection selection = getSelection(name);
        if (selection == null) {
            return false;
        }
        
        return DAOFunctions.remove(selection, PreparerHolder.preparerOnRemove);
    }

    @Override
    public Integer getSelectionSize(String name) {
        if (name == null) {
            return null;
        }
        
        return DAOFunctions.queryElement(
                Integer.class,
                "SELECT selSize "
              + "FROM selections s "
              + "WHERE s.name = :name ", 
                new TreeMap<String, Object>(){{
                    put("name", name);
                }}, 
                false);
    }
    
    @Override
    public Integer getSelectionHash(String name) {
        if (name == null) {
            return null;
        }
        
        return DAOFunctions.queryElement(
                Integer.class,
                "SELECT hashKey "
              + "FROM selections s "
              + "WHERE s.name = :name ", 
                new TreeMap<String, Object>(){{
                    put("name", name);
                }}, 
                false);
    }

    @Override
    public List<Selection> getAllSelections() {
        return DAOFunctions.criteria(Selection.class, null);
    }

    @Override
    public List<String> getAllSelectionsNames() {
        return DAOFunctions.queryList(
                String.class,
                "SELECT name "
              + "FROM selections ", 
                null, 
                false);
         
    }

    @Override
    public int getAllSelectionsCount() {
        Session session = null;
        
        Integer count = DAOFunctions.queryElement(
                Integer.class,
                "SELECT COUNT(*) FROM selections ", 
                null, 
                false);
        
        return (count == null) ? 0 : count;
    }

    @Override
    public void removeAllSelections() {
        DAOFunctions.removeAll(getAllSelections(), PreparerHolder.preparerOnRemove);
    }

}
