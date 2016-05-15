package hibernate.DAO;

import hibernate.logic.IterationsCount;
import hibernate.logic.Quantile;
import hibernate.logic.SignificanceLevel;
import hibernate.logic.TestType;
import java.util.List;
import org.hibernate.Session;


public interface QuantilesDAO {
    
    public boolean addQuantile(Quantile quantile);
    public boolean updateQuantile(Quantile quantile);
    
    public Quantile getQuantile(Long id);
    public Quantile getQuantile(
            TestType type, 
            SignificanceLevel signLevel,
            IterationsCount iterCount,
            Integer selSize);
    
    public boolean removeQuantile(Long id);
    public boolean removeQuantile(
            TestType type, 
            SignificanceLevel signLevel,
            IterationsCount iterCount,
            Integer selSize);
    
    public List<Quantile> getQuantiles(
            TestType type,  
            SignificanceLevel signLevel,
            IterationsCount iterCount);
    public List<Quantile> getQuantiles(
            TestType type,  
            SignificanceLevel signLevel,
            IterationsCount iterCount,
            int minSelSize,
            int maxSelSize);
    public List<Quantile> getQuantiles(TestType type);
    public List<Quantile> getQuantiles(SignificanceLevel signLevel);
    public List<Quantile> getQuantiles(IterationsCount iterCount);
    public List<Quantile> getAllQuantiles();
    
    public void removeQuantiles(TestType type);
    public void removeQuantiles(SignificanceLevel signLevel);
    public void removeQuantiles(IterationsCount iterCount);
    
    public void removeQuantiles(TestType type, Session session);
    public void removeQuantiles(SignificanceLevel signLevel, Session session);
    public void removeQuantiles(IterationsCount iterCount, Session session);
    
    public void removeAllQuantiles();
    
}
