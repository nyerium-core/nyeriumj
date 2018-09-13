package org.nyeriumj.params;

import org.nyeriumj.core.CoinDefinition;

public class TestNet2Params extends AbstractBitcoinNetParams {
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 51;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 75;
    public static final int TESTNET_MAJORITY_WINDOW = 100;


    public TestNet2Params() {
        this.id = "org.nyeriumj.test";
        this.packetMagic = 4206867930L;
        this.port = CoinDefinition.TestPort;
        this.addressHeader = 112;
        this.p2shHeader = 19;
        this.acceptableAddressCodes = new int[]{this.addressHeader, this.p2shHeader};
        this.interval = 1440;
        this.targetTimespan = 86400;
        this.maxTarget = CoinDefinition.proofOfWorkLimit;
        this.dumpedPrivateKeyHeader = 267;
        this.genesisBlock.setTime(1296688602);
        this.genesisBlock.setDifficultyTarget(487063544);
        this.genesisBlock.setNonce(384568319);
        this.spendableCoinbaseDepth = CoinDefinition.spendableCoinbaseDepth;
        this.subsidyDecreaseBlockCount = CoinDefinition.subsidyDecreaseBlockCount;
        String genesisHash = this.genesisBlock.getHashAsString();
        this.dnsSeeds = null;
        this.addrSeeds = null;
        this.bip32HeaderPub = 70617039;
        this.bip32HeaderPriv = 70615956;
        majorityEnforceBlockUpgrade = TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TESTNET_MAJORITY_WINDOW;
    }

    private static TestNet2Params instance;
    public static synchronized TestNet2Params get() {
        if (instance == null) {
            instance = new TestNet2Params();
        }
        return instance;
    }


    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
