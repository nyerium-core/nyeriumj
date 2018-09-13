package org.nyeriumj.store;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;
import javax.annotation.Nullable;

/**
 * A Map with multiple key types that is DB per-thread-transaction-aware.
 * However, this class is not thread-safe.
 * @param <UniqueKeyType> is a key that must be unique per object
 * @param <MultiKeyType> is a key that can have multiple values
 */
class TransactionalMultiKeyHashMap<UniqueKeyType, MultiKeyType, ValueType> {
    TransactionalHashMap<UniqueKeyType, ValueType> mapValues;
    HashMap<MultiKeyType, Set<UniqueKeyType>> mapKeys;

    public TransactionalMultiKeyHashMap() {
        mapValues = new TransactionalHashMap<UniqueKeyType, ValueType>();
        mapKeys = new HashMap<MultiKeyType, Set<UniqueKeyType>>();
    }

    public void BeginTransaction() {
        mapValues.beginDatabaseBatchWrite();
    }

    public void CommitTransaction() {
        mapValues.commitDatabaseBatchWrite();
    }

    public void AbortTransaction() {
        mapValues.abortDatabaseBatchWrite();
    }

    @Nullable
    public ValueType get(UniqueKeyType key) {
        return mapValues.get(key);
    }

    public void put(UniqueKeyType uniqueKey, MultiKeyType multiKey, ValueType value) {
        mapValues.put(uniqueKey, value);
        Set<UniqueKeyType> set = mapKeys.get(multiKey);
        if (set == null) {
            set = new HashSet<UniqueKeyType>();
            set.add(uniqueKey);
            mapKeys.put(multiKey, set);
        }else{
            set.add(uniqueKey);
        }
    }

    @Nullable
    public ValueType removeByUniqueKey(UniqueKeyType key) {
        return mapValues.remove(key);
    }

    public void removeByMultiKey(MultiKeyType key) {
        Set<UniqueKeyType> set = mapKeys.remove(key);
        if (set != null)
            for (UniqueKeyType uniqueKey : set)
                removeByUniqueKey(uniqueKey);
    }
}

