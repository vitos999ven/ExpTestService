package entities;

import java.util.SortedSet;


public class IterationsCountsSet implements Comparable<IterationsCountsSet> {

    public final String test_type;
    public final int default_iter_count;
    public final SortedSet<Integer> iter_counts;
    
    public IterationsCountsSet(String test_type, int default_iter_count, SortedSet<Integer> iter_counts) {
        this.test_type = test_type;
        this.default_iter_count = default_iter_count;
        this.iter_counts = iter_counts;
    }
    
    @Override
    public int compareTo(IterationsCountsSet other) {
        if (other == null) return 1;
        return this.test_type.compareTo(other.test_type);
    }

}
