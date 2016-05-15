package hibernate.DAO;

import hibernate.logic.SignificanceLevel;
import java.util.List;


public interface SignificanceLevelsDAO {
    
    public boolean addSignificanceLevel(SignificanceLevel level);
    public boolean containsSignificanceLevel(SignificanceLevel level);
    public boolean removeSignificanceLevel(SignificanceLevel level);
    public List<SignificanceLevel> getAllSignificanceLevels();
    public void removeAllSignificanceLevels();
    
}
