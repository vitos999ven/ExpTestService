package entities;

import java.util.SortedSet;
import java.util.TreeSet;


public enum TestTypeEnum {
    
    UNKNOWN("Unknown test", 0),
    GINI("Gini test", 1),
    GREENWOOD("Greenwood test", 2),
    FROCINI("Frocini test", 3),
    KIMBERMICHEL("Kimber-Michel test", 4),
    MORAN("Moran test", 5),
    RAO("Rao test", 6),
    SHAPIROWILK("Shapiro-Wilk test", 7),
    SHERMAN("Sherman test", 8);
    
    private final String name;
    private final int value;
    
    TestTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public static SortedSet<TestTypeEnum> createSet(String types) {
        SortedSet<TestTypeEnum> set = new TreeSet<>();
        set.add(GINI);
        set.add(GREENWOOD);
        set.add(FROCINI);
        set.add(KIMBERMICHEL);
        set.add(MORAN);
        set.add(RAO);
        set.add(SHAPIROWILK);
        set.add(SHERMAN);
        return set;
    }
    
    public static boolean checkType(String type) {
        TestTypeEnum[] types = TestTypeEnum.values();
        
        for (int i = 0; i < types.length; ++i) {
            if (types[i].getName().equals(type)) {
                return true;
            }
        }
        
        return false;
    }
}
