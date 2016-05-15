package hibernate.DAO;

import hibernate.logic.TestType;
import java.util.List;


public interface TestTypesDAO {
    
    public boolean addTestType(TestType type);
    public boolean containsTestType(String typeName);
    public boolean updateTestType(TestType type); 
    public TestType getTestType(String typeName); 
    public boolean removeTestType(TestType type);
    public List<TestType> getAllTestTypes();
    public void removeAllTestTypes();
    
}
