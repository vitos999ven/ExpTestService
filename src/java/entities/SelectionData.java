package entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class SelectionData implements Comparable<SelectionData> {
    
    public final String name;
    public List<BigDecimal> values;
    public volatile int hash;
    
    private SelectionData() {
        this.name = "";
        this.values = null;
        this.hash = 0;
    }
    
    public SelectionData(String name, List<BigDecimal> values) {
        this.name = name;
        this.values = values;
        this.hash = 0;
    }
    
    public SelectionData(String name, List<BigDecimal> values, int hash) {
        this.name = name;
        this.values = values;
        this.hash = hash;
    }

    @Override
    public int compareTo(SelectionData other) {
        if (other == null) return 1;
        return this.name.compareTo(other.name);
    }
    
    @Override
    public SelectionData clone() {
        return new SelectionData(name, (values == null) ? null : new ArrayList<>(values), hash);
    }
}
