package hibernate.logic;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="selections")
public class Selection implements Comparable<Selection> {
    
    @Id
    @Column(name="name", unique = true, nullable = false)
    private String name;
    
    @Column(name="selSize", nullable = false)
    private Integer selSize;
    
    @Column(name="selValues", nullable = false, columnDefinition="TEXT")
    private String selValues;
    
    @Column(name="hashKey", nullable = false)
    private Integer hashKey;
    
    
    public Selection() {}
    
    public Selection(String name, int size, String values) {
        this.name = name;
        this.selSize = size;
        this.selValues = values;
        this.hashKey = 0;
    }
    
    public Selection(String name, int size, String values, int hash) {
        this.name = name;
        this.selSize = size;
        this.selValues = values;
        this.hashKey = hash;
    }
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public int getSize() {
        return selSize;
    }

    public void setSize(int size) {
        this.selSize = size;
    }

    
    public String getValues() {
        return selValues;
    }

    public void setValues(String values) {
        this.selValues = values;
    }

    
    public int getHash() {
        return hashKey;
    }

    public void setHash(int hash) {
        this.hashKey = hash;
    }
    
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof Selection)) {
            return false;
        }
        
        Selection other = (Selection) obj;
        
        return (Objects.equals(name, other.name));
    }

    @Override
    public int compareTo(Selection o) {
        if (o == null) return 1;
        
        return (name== null) ? -1 : name.compareTo(o.name);
    }
   
}
