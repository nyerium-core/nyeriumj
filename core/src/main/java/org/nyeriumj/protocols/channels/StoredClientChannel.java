
package org.nyeriumj.protocols.channels;

import org.nyeriumj.core.Coin;
import org.nyeriumj.core.ECKey;
import org.nyeriumj.core.Sha256Hash;
import org.nyeriumj.core.Transaction;

import java.util.Locale;

/**
 * Represents the state of a channel once it has been opened in such a way that it can be stored and used to resume a
 * channel which was interrupted (eg on connection failure) or keep track of refund transactions which need broadcast
 * when they expire.
 */
class StoredClientChannel {
    int majorVersion;
    Sha256Hash id;
    Transaction contract, refund;
    // The expiry time of the contract in protocol v2.
    long expiryTime;
    // The transaction that closed the channel (generated by the server)
    Transaction close;
    ECKey myKey;
    ECKey serverKey;
    Coin valueToMe, refundFees;

    // In-memory flag to indicate intent to resume this channel (or that the channel is already in use)
    boolean active = false;

    StoredClientChannel(int majorVersion, Sha256Hash id, Transaction contract, Transaction refund, ECKey myKey, ECKey serverKey, Coin valueToMe,
                        Coin refundFees, long expiryTime, boolean active) {
        this.majorVersion = majorVersion;
        this.id = id;
        this.contract = contract;
        this.refund = refund;
        this.myKey = myKey;
        this.serverKey = serverKey;
        this.valueToMe = valueToMe;
        this.refundFees = refundFees;
        this.expiryTime = expiryTime;
        this.active = active;
    }

    long expiryTimeSeconds() {
        switch (majorVersion) {
            case 1:
                return refund.getLockTime() + 60 * 5;
            case 2:
                return expiryTime + 60 * 5;
            default:
                throw new IllegalStateException("Invalid version");
        }
    }

    @Override
    public String toString() {
        final String newline = String.format(Locale.US, "%n");
        final String closeStr = close == null ? "still open" : close.toString().replaceAll(newline, newline + "   ");
        return String.format(Locale.US, "Stored client channel for server ID %s (%s)%n" +
                        "    Version:     %d%n" +
                        "    Key:         %s%n" +
                        "    Server key:  %s%n" +
                        "    Value left:  %s%n" +
                        "    Refund fees: %s%n" +
                        "    Expiry     : %s%n" +
                        "    Contract:  %s" +
                        "Refund:    %s" +
                        "Close:     %s",
                id, active ? "active" : "inactive", majorVersion, myKey, serverKey, valueToMe, refundFees, expiryTime,
                contract.toString().replaceAll(newline, newline + "    "),
                refund.toString().replaceAll(newline, newline + "    "),
                closeStr);
    }
}
