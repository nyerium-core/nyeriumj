package org.nyeriumj.params;

import org.nyeriumj.core.*;

import com.google.common.base.Preconditions;

public class MainNetParams extends AbstractBitcoinNetParams {
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_WINDOW = 1000;

    public static final int MAINNET_MAJORITY_DIP0001_WINDOW = 8000;
    public static final int MAINNET_MAJORITY_DIP0001_THRESHOLD = 8000;

    public MainNetParams() {
        this.interval = INTERVAL;
        this.targetTimespan = TARGET_TIMESPAN;
        this.maxTarget = CoinDefinition.proofOfWorkLimit;
        this.dumpedPrivateKeyHeader = 212;
        this.addressHeader = 53;
        this.p2shHeader = 8;
        this.acceptableAddressCodes = new int[]{this.addressHeader, this.p2shHeader};
        this.port = CoinDefinition.Port;
        this.packetMagic = CoinDefinition.PacketMagic;
        this.bip32HeaderPub = 0x022D2533;
        this.bip32HeaderPriv = 0x3a8061a0;
        this.genesisBlock.setDifficultyTarget(CoinDefinition.genesisBlockDifficultyTarget);
        this.genesisBlock.setTime(CoinDefinition.genesisBlockTime);
        this.genesisBlock.setNonce(CoinDefinition.genesisBlockNonce);
        this.majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        this.majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        this.majorityWindow = MAINNET_MAJORITY_WINDOW;

        this.id = ID_MAINNET;
        this.subsidyDecreaseBlockCount = CoinDefinition.subsidyDecreaseBlockCount;
        this.spendableCoinbaseDepth = CoinDefinition.spendableCoinbaseDepth;
        String genesisHash = this.genesisBlock.getHashAsString();

        System.out.println(genesisHash);
        System.out.println(CoinDefinition.genesisHash);
        Preconditions.checkState(genesisHash.equals(CoinDefinition.genesisHash), genesisHash);
        CoinDefinition.initCheckpoints(this.checkpoints);

        this.zerocoinStartedHeight = CoinDefinition.MAINNET_ZEROCOIN_STARTING_BLOCK_HEIGHT;
        this.dnsSeeds = CoinDefinition.dnsSeeds;
        this.httpSeeds = null;

        checkpoints.put(0, Sha256Hash.wrap("00000849fd3704944ef2ef374f70d79dcc3fd58ac29dc459114aa8b0788378f3"));
        //checkpoints.put(41, Sha256Hash.wrap("000008a019d98868a739449fbae61d542bc084ed8ae3cb877c5c5b8f813554d8"));
        checkpoints.put(5000, Sha256Hash.wrap("fc2d1aff0b9f4348ee720f1c5c634c75cfc30b528bd2530bae5d5d54eecac3ed"));
        checkpoints.put(7999, Sha256Hash.wrap("71a890c048d99659ad9a1156302370c1b94438ac099a3b26dab31dd1e42aa87a"));
        checkpoints.put(20002, Sha256Hash.wrap("af7dd70e8f5e4cfff5de664fb0d629fe81d1ce7461a86f2f7fefb7572232e6d0"));
        checkpoints.put(60000, Sha256Hash.wrap("2e4db31c55c99c3e0ccf1afcf4c7e209c330c3ab8beb5705f911e04feba0ca14"));
        checkpoints.put(70015, Sha256Hash.wrap("59f52323e8cf1a5f3bef64ce06f2518ac93cd82a6b46dcc0ad76f6c027618add"));
        checkpoints.put(80000, Sha256Hash.wrap("bced4db2a98e39a64391c12cc2d3aac8f732da3d8e7918afb114846c438f8750"));
        checkpoints.put(90002, Sha256Hash.wrap("283c8a9a9b47bf1db1e6c2d043f155c402457679c0fadbacdf6037116883a913"));


        this.addrSeeds = null;
        this.strSporkKey = "0432f6a8c71a7efbe34caf81e0f832afd88a87e2a916cf2afbd98b10b615ddeed2e88c6ec6b9ec8f027e75b214af990982711f470fd799af723d72318d2d76dc9a";
        budgetPaymentsStartBlock = 100000;

        DIP0001Window = MAINNET_MAJORITY_DIP0001_WINDOW;
        DIP0001Upgrade = MAINNET_MAJORITY_DIP0001_THRESHOLD;
        DIP0001BlockHeight = 8000;

    }

    private static MainNetParams instance;

    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
