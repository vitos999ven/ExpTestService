package hibernate.logic;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="significance_levels")
public class SignificanceLevel implements Comparable<SignificanceLevel> {
    
    private static final int maxLevel = 500;
    
    @Id
    @Column(name="level", unique = true, nullable = false)
    private Integer level;
    
    
    public SignificanceLevel() {}
    
    public SignificanceLevel(int level){
        this.level = (level > maxLevel) ? maxLevel : ((level < 0) ? 0 : level);
    }
    
    
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
    
    
    public static int getMaxLevel() {
        return maxLevel;
    }
    
    public double getDoubleValue() {
        return ((double)level) / (2 * maxLevel);
    }
    
    public IndexesPair getIndexesPair(int iterCount) {
        return new IndexesPair(iterCount, getDoubleValue());
    }
    
    public IndexesPair getIndexesPair(int iterCount, IndexesPair old) {
        if (old == null) {
            return new IndexesPair(iterCount, getDoubleValue());
        }
        return old.updateIndexes(iterCount, getDoubleValue());
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj == null || !(obj instanceof SignificanceLevel)) {
            return false;
        }
        
        SignificanceLevel other = (SignificanceLevel) obj;
        
        return Objects.equals(level, other.level);
    }

    @Override
    public int hashCode() {
        return level;
    }
    
    @Override
    public int compareTo(SignificanceLevel o) {
        if (o == null) return 1;
        
        return (level == null) ? -1 : level.compareTo(o.level);
    }
    
    public static final class IndexesPair {
        
        public int first;
        public int second;
        
        IndexesPair(int iterCount, double value) {
            updateIndexes(iterCount, value);
        }
        
        public IndexesPair updateIndexes(int iterCount, double value) {
            int index = (int) (value * iterCount);
            first =  index - 1;
            second = iterCount - index - 1;
            return this;
        }
        
    }

}
