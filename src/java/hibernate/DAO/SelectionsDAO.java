package hibernate.DAO;

import hibernate.logic.Selection;
import java.util.List;


public interface SelectionsDAO {
    
    public boolean addSelection(Selection selection);
    public boolean updateSelection(Selection selection);
    public Selection getSelection(String name);
    public boolean removeSelection(String name);
    public Integer getSelectionSize(String name);
    public Integer getSelectionHash(String name);
    public List<Selection> getAllSelections();
    public List<String> getAllSelectionsNames();
    public int getAllSelectionsCount();
    public void removeAllSelections();
   
}
