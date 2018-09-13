package org.nyeriumj.store;

import com.google.common.base.Objects;
import org.nyeriumj.core.Sha256Hash;
import org.nyeriumj.core.UTXO;

/* compiled from: MemoryFullPrunedBlockStore */
class StoredTransactionOutPoint {
    Sha256Hash hash;
    long index;

    StoredTransactionOutPoint(Sha256Hash hash, long index) {
        this.hash = hash;
        this.index = index;
    }

    StoredTransactionOutPoint(UTXO out) {
        this.hash = out.getHash();
        this.index = out.getIndex();
    }

    Sha256Hash getHash() {
        return this.hash;
    }

    long getIndex() {
        return this.index;
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{Long.valueOf(getIndex()), getHash()});
    }

    public String toString() {
        return "Stored transaction out point: " + this.hash + ":" + this.index;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StoredTransactionOutPoint other = (StoredTransactionOutPoint) o;
        if (getIndex() == other.getIndex() && Objects.equal(getHash(), other.getHash())) {
            return true;
        }
        return false;
    }
}
