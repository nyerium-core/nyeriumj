package org.nyeriumj.store;

import javax.annotation.Nullable;
import java.util.*;


/**
 * A HashMap<KeyType, ValueType> that is DB transaction-aware
 * This class is not thread-safe.
 */
class TransactionalHashMap<KeyType, ValueType> {
    ThreadLocal<HashMap<KeyType, ValueType>> tempMap;
    ThreadLocal<HashSet<KeyType>> tempSetRemoved;
    private ThreadLocal<Boolean> inTransaction;

    HashMap<KeyType, ValueType> map;

    public TransactionalHashMap() {
        tempMap = new ThreadLocal<HashMap<KeyType, ValueType>>();
        tempSetRemoved = new ThreadLocal<HashSet<KeyType>>();
        inTransaction = new ThreadLocal<Boolean>();
        map = new HashMap<KeyType, ValueType>();
    }

    public void beginDatabaseBatchWrite() {
        inTransaction.set(true);
    }

    public void commitDatabaseBatchWrite() {
        if (tempSetRemoved.get() != null)
            for(KeyType key : tempSetRemoved.get())
                map.remove(key);
        if (tempMap.get() != null)
            for (Map.Entry<KeyType, ValueType> entry : tempMap.get().entrySet())
                map.put(entry.getKey(), entry.getValue());
        abortDatabaseBatchWrite();
    }

    public void abortDatabaseBatchWrite() {
        inTransaction.set(false);
        tempSetRemoved.remove();
        tempMap.remove();
    }

    @Nullable
    public ValueType get(KeyType key) {
        if (Boolean.TRUE.equals(inTransaction.get())) {
            if (tempMap.get() != null) {
                ValueType value = tempMap.get().get(key);
                if (value != null)
                    return value;
            }
            if (tempSetRemoved.get() != null && tempSetRemoved.get().contains(key))
                return null;
        }
        return map.get(key);
    }

    public List<ValueType> values() {
        List<ValueType> valueTypes = new ArrayList<ValueType>();
        for (KeyType keyType : map.keySet()) {
            valueTypes.add(get(keyType));
        }
        return valueTypes;
    }

    public void put(KeyType key, ValueType value) {
        if (Boolean.TRUE.equals(inTransaction.get())) {
            if (tempSetRemoved.get() != null)
                tempSetRemoved.get().remove(key);
            if (tempMap.get() == null)
                tempMap.set(new HashMap<KeyType, ValueType>());
            tempMap.get().put(key, value);
        }else{
            map.put(key, value);
        }
    }

    @Nullable
    public ValueType remove(KeyType key) {
        if (Boolean.TRUE.equals(inTransaction.get())) {
            ValueType retVal = map.get(key);
            if (retVal != null) {
                if (tempSetRemoved.get() == null)
                    tempSetRemoved.set(new HashSet<KeyType>());
                tempSetRemoved.get().add(key);
            }
            if (tempMap.get() != null) {
                ValueType tempVal = tempMap.get().remove(key);
                if (tempVal != null)
                    return tempVal;
            }
            return retVal;
        }else{
            return map.remove(key);
        }
    }
}
