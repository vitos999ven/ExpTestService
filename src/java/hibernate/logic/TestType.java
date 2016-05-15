package hibernate.logic;

import entities.TestTypeEnum;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="test_types")
public class TestType implements Comparable<TestType>{
    
    @Id
    @Column(name="type", unique = true, nullable = false)
    private String type;
    
    @Column(name="name", unique = true, nullable = false)
    private String name;
    
    @Column(name="forSorted", nullable = false)
    private Boolean forSorted;
    
    @Column(name="defaultIterCount", nullable = false)
    private Integer defaultIterCount;
    
    public TestType() {}
    
    public TestType(TestTypeEnum type) {
        this.type = type.getName();
        this.name = "";
        this.forSorted = false;
        this.defaultIterCount = IterationsCount.DEFAULT;
    }
    
    public TestType(String type) {
        this.type = type;
        this.name = "";
        this.forSorted = false;
        this.defaultIterCount = IterationsCount.DEFAULT;
    }
    
    public TestType(String type, String name) {
        this.type = type;
        this.name = name;
        this.forSorted = false;
        this.defaultIterCount = IterationsCount.DEFAULT;
    }
    
    public TestType(String type, String name, boolean forSorted) {
        this.type = type;
        this.name = name;
        this.forSorted = forSorted;
        this.defaultIterCount = IterationsCount.DEFAULT;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isValidType() {
        return TestTypeEnum.checkType(type);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getForSorted() {
        return forSorted;
    }

    public void setForSorted(Boolean forSorted) {
        this.forSorted = forSorted;
    }

    public Integer getDefaultIterCount() {
        return defaultIterCount;
    }

    public void setDefaultIterCount(Integer defaultIterCount) {
        this.defaultIterCount = defaultIterCount;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof TestType) || type == null) {
            return false;
        }
        
        TestType other = (TestType) obj;
        
        return (type.equals(other.type));
    }

    @Override
    public int compareTo(TestType o) {
        if (o == null) return 1;
        if (type == null) return -1;
        
        return type.compareTo(o.type);
    }

}
