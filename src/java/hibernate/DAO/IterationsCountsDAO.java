package hibernate.DAO;

import hibernate.logic.IterationsCount;
import hibernate.logic.TestType;
import java.util.List;
import org.hibernate.Session;


public interface IterationsCountsDAO {
    
    public boolean addIterationsCount(IterationsCount iterCount);
    public boolean updateIterationsCount(IterationsCount iterCount);
    public IterationsCount getIterationsCount(Long id);
    public IterationsCount getIterationsCount(TestType testType, int iterCount);
    public boolean removeIterationsCount(Long id);
    public boolean removeIterationsCount(TestType testType, int iterCount);
    public List<IterationsCount> getIterationsCounts(TestType testType);
    public List<IterationsCount> getAllIterationsCounts();
    public void removeIterationsCounts(TestType testType);
    public void removeIterationsCounts(TestType testType, Session session);
    public void removeAllIterationsCounts();
    
}
