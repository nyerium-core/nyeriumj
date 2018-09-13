package org.nyeriumj.params;

import java.math.BigInteger;
import org.nyeriumj.core.Block;
import org.nyeriumj.core.CoinDefinition;
import org.nyeriumj.core.NetworkParameters;

public class UnitTestParams extends AbstractBitcoinNetParams {
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 4;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 6;
    public static final int UNITNET_MAJORITY_WINDOW = 8;
    private static UnitTestParams instance;

    public UnitTestParams() {
        this.id = "com.google.nyeriumj.unittest";
        this.packetMagic = 185665799;
        this.addressHeader = 112;
        this.p2shHeader = 19;
        this.acceptableAddressCodes = new int[]{this.addressHeader, this.p2shHeader};
        this.maxTarget = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        this.genesisBlock.setTime(System.currentTimeMillis() / 1000);
        this.genesisBlock.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
        this.genesisBlock.solve();
        this.port = CoinDefinition.TestPort;
        this.interval = 10;
        this.dumpedPrivateKeyHeader = 267;
        this.targetTimespan = 200000000;
        this.spendableCoinbaseDepth = 5;
        this.subsidyDecreaseBlockCount = 100;
        this.dnsSeeds = null;
        this.addrSeeds = null;
        this.bip32HeaderPub = 70617039;
        this.bip32HeaderPriv = 70615956;
        this.majorityEnforceBlockUpgrade = 3;
        this.majorityRejectBlockOutdated = 4;
        this.majorityWindow = 7;
    }

    public static synchronized UnitTestParams get() {
        UnitTestParams unitTestParams;
        synchronized (UnitTestParams.class) {
            if (instance == null) {
                instance = new UnitTestParams();
            }
            unitTestParams = instance;
        }
        return unitTestParams;
    }

    public String getPaymentProtocolId() {
        return NetworkParameters.PAYMENT_PROTOCOL_ID_UNIT_TESTS;
    }
}
