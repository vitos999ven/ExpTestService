package hibernate.logic;

import hibernate.util.Factory;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.Session;


@Entity
@Table(name="iterations_counts"
        , uniqueConstraints = @UniqueConstraint(
                columnNames = {"testType", "count"}))
public class IterationsCount  implements Comparable<IterationsCount> {

    public static final int DEFAULT = 10000;
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="id", unique = true, nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="testType", nullable = false)
    private TestType testType;
    
    @Column(name="count", nullable = false)
    private Integer count;

    public IterationsCount() {}
    
    public IterationsCount(Long id) {
        this.id = id;
    }
    
    public IterationsCount(TestType testType, Integer count) {
        this.testType = testType;
        this.count = count;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
    
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof IterationsCount)) {
            return false;
        }
        
        IterationsCount other = (IterationsCount) obj;
        
        return (Objects.equals(id, other.id));
    }
    
    @Override
    public int compareTo(IterationsCount o) {
        if (o == null) return 1;
        
        return (id == null) ? -1 : id.compareTo(o.id);
    }

}
