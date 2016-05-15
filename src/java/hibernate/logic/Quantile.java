package hibernate.logic;

import java.math.BigDecimal;
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


@Entity
@Table(name="quantiles"
        , uniqueConstraints = @UniqueConstraint(
                columnNames = {"testType", "selSize", "signLevel", "iterCount"}))
public class Quantile implements Comparable<Quantile> {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="id", unique = true, nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="testType", nullable = false)
    private TestType testType;
    
    @Column(name="selSize", nullable = false) 
    private Integer selSize;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="signLevel", nullable = false)
    private SignificanceLevel signLevel;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="iterCount", nullable = false)
    private IterationsCount iterCount;
    
    @Column(name="firstValue") 
    private BigDecimal firstValue;
    
    @Column(name="secondValue") 
    private BigDecimal secondValue;
    
    
    public Quantile() {};
    
    public Quantile(Long id) {
        this.id = id;
    };
    
    public Quantile(
            TestType testType,
            SignificanceLevel signLevel,
            IterationsCount iterCount,
            Integer selectionSize,
            BigDecimal first,
            BigDecimal second) {
        this.testType = testType;
        this.selSize = selectionSize;
        this.signLevel = signLevel;
        this.iterCount = iterCount;
        this.firstValue = first;
        this.secondValue = second;
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

    
    public Integer getSelectionSize() {
        return selSize;
    }

    public void setSelectionSize(Integer selSize) {
        this.selSize = selSize;
    }

    
    public SignificanceLevel getSignificanceLevel() {
        return signLevel;
    }

    public void setSignificanceLevel(SignificanceLevel signLevel) {
        this.signLevel = signLevel;
    }

    
    public IterationsCount getIterationsCount() {
        return iterCount;
    }

    public void setIterationsCount(IterationsCount iterCount) {
        this.iterCount = iterCount;
    }
    
    
    public BigDecimal getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(BigDecimal first) {
        this.firstValue = first;
    }

    
    public BigDecimal getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(BigDecimal second) {
        this.secondValue = second;
    }
    
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof Quantile)) return false;
        
        Quantile other = (Quantile) obj;
        
        return (Objects.equals(id, other.id));
    }

    @Override
    public int compareTo(Quantile o) {
        if (o == null) return 1;
        
        return (id == null) ? -1 : id.compareTo(o.id);
    }
    
}
