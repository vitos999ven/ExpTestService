package entities;


public class SelectionInfo implements Comparable<SelectionInfo> {
    
    public String name;
    public Integer size;
    public Integer hash;
    
    public SelectionInfo() {
        this.name = "";
        this.size = 0;
        this.hash = 0;
    }
    
    public SelectionInfo(String name, Integer size, Integer hash) {
        this.name = name;
        this.size = size;
        this.hash = hash;
    }

    @Override
    public int compareTo(SelectionInfo other) {
        if (other == null) return 1;
        return this.name.compareTo(other.name);
    }
}
