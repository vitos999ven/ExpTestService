package services;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LocksMap<K> {
    private final Map<K, WeakReference<Lock> > cacheMap;
    
    public LocksMap() {
        cacheMap = new WeakHashMap<>();
    }
    
    public Lock get(K key) {
        synchronized(cacheMap) {
            WeakReference<Lock> ref = cacheMap.get(key);
            
            if (ref == null) {
                Lock lock = new ReentrantLock();
                cacheMap.put(key, new WeakReference(lock));
                return lock;
            }
            
            Lock lock = ref.get();
            
            if (lock == null) {
                lock = new ReentrantLock();
                cacheMap.put(key, new WeakReference(lock));
            }
            
            return lock;
        }
    }
    
    public int getLocksCount() {
        synchronized(cacheMap) {
            return cacheMap.size();
        }
    }
}
