package entities;

import java.util.SortedMap;
import java.util.TreeMap;
import logic.tests.QuantilePair;


public class QuantilesMap {
    
    public final String test_type;
    public final int iter_count;
    public final int min_sel_size;
    public final int quantiles_count;
    public final SortedMap<Integer, SortedMap<Integer, QuantilePair> > quantiles;
    
    public QuantilesMap(
            String test_type, 
            int iter_count, 
            int min_sel_size,
            int quantiles_count) {
        this.test_type = test_type;
        this.iter_count = iter_count;
        this.min_sel_size = min_sel_size;
        this.quantiles_count = quantiles_count;
        quantiles = new TreeMap<>();
    }
    
    
    
}
