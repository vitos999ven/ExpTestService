package services;

import entities.SelectionData;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;


public class SelectionsMap {
    
    private final Map<String, WeakReference<SelectionData> > selectionsMap;
    
    public SelectionsMap() {
        selectionsMap = new WeakHashMap<>();
    }
    
    public SelectionData get(String name) {
        synchronized(this) {
            WeakReference<SelectionData> ref = selectionsMap.get(name);

            if (ref == null) {
                return null;
            }

            return ref.get();
        }
    }
    
    public SelectionData remove(String name) {
        synchronized(this) {
            WeakReference<SelectionData> ref = selectionsMap.remove(name);

            if (ref == null) {
                return null;
            }

            return ref.get();
        }
    }
    
    public SelectionData put(String name, SelectionData selectionData) {
        synchronized(this) {
            WeakReference<SelectionData> ref = selectionsMap.put(name, new WeakReference<>(selectionData));

            if (ref == null) {
                return null;
            }

            return ref.get();
        }
    }
    
}
