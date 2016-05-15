package hibernate.logic;

import entities.alternatives.SelectionParams;
import entities.alternatives.SelectionsEnum;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name="powers"
        , uniqueConstraints = @UniqueConstraint(
                columnNames = {"quantile", "alternative", "paramsString"}))
public class Power implements Comparable<Power> {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="id", unique = true, nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="quantile", nullable = false)
    private Quantile quantile;
    
    @Column(name="alternative", nullable = false) 
    @Enumerated(EnumType.STRING)
    private SelectionsEnum alternative;
    
    @Column(name="paramsString", nullable = false) 
    private String paramsString;
    
    @Column(name="power", nullable = false) 
    private Double power;
    
    @Transient
    private SelectionParams alternativeParams = null;

    public Power() {}
    
    public Power(Long id) {
        this.id = id;
    }
    
    public Power(Quantile quantile, SelectionParams alternativeParams, Double power) {
        this.quantile = quantile;
        this.power = power;
        setAlternativeParams(alternativeParams);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quantile getQuantile() {
        return quantile;
    }
    
    public void setQuantile(Quantile quantile) {
        this.quantile = quantile;
    }
    
    public SelectionsEnum getAlternative() {
        return alternative;
    }

    public void setAlternative(SelectionsEnum alternative) {
        this.alternative = alternative;
    }

    public String getParamsString() {
        return paramsString;
    }

    public void setParamsString(String paramsString) {
        this.paramsString = paramsString;
    }
    
    public SelectionParams getAlternativeParams() {
        if (alternativeParams != null 
                && alternativeParams.getSelectionType().equals(alternative)) {
            return alternativeParams;
        }
        if (alternative == null) return null;
        alternativeParams = alternative.parseSelectionParams(paramsString);
        return alternativeParams;
    }

    public void setAlternativeParams(SelectionParams alternativeParams) {
        this.alternativeParams = alternativeParams;
        this.alternative = alternativeParams.getSelectionType();
        this.paramsString = alternativeParams.toJson();
    }
    
    public Double getPower() {
        return power;
    }
    
    public void setPower(Double power) {
        this.power = power;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof Power)) return false;
        
        Power other = (Power) obj;
        
        return (Objects.equals(id, other.id));
    }

    @Override
    public int compareTo(Power other) {
        if (other == null) return 1;
        return (this.id == null) ? -1 : this.id.compareTo(other.id);
    }
    
}
