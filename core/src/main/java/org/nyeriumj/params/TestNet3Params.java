package org.nyeriumj.params;

import java.util.Date;
import org.nyeriumj.core.Block;
import org.nyeriumj.core.CoinDefinition;
import org.nyeriumj.core.NetworkParameters;
import org.nyeriumj.core.StoredBlock;
import org.nyeriumj.core.VerificationException;
import org.nyeriumj.store.BlockStore;
import org.nyeriumj.store.BlockStoreException;

public class TestNet3Params extends AbstractBitcoinNetParams {
    private static TestNet3Params instance;
    private static final Date testnetDiffDate = new Date(1329264000000L);

    public TestNet3Params() {
        this.id = "org.nyeriumj.test";
        this.packetMagic = CoinDefinition.testnetPacketMagic;
        this.interval = 60;
        this.targetTimespan = 60;
        this.maxTarget = CoinDefinition.proofOfWorkLimit;
        this.port = CoinDefinition.TestPort;
        this.addressHeader = 112;
        this.p2shHeader = 19;
        this.acceptableAddressCodes = new int[]{this.addressHeader, this.p2shHeader};
        this.dumpedPrivateKeyHeader = 239;
        this.genesisBlock.setTime(CoinDefinition.testnetGenesisBlockTime);
        this.genesisBlock.setDifficultyTarget(CoinDefinition.testnetGenesisBlockDifficultyTarget);
        this.genesisBlock.setNonce(CoinDefinition.testnetGenesisBlockNonce);
        this.spendableCoinbaseDepth = 10; //EK TO REVIEW
        this.subsidyDecreaseBlockCount = CoinDefinition.subsidyDecreaseBlockCount;
        String genesisHash = this.genesisBlock.getHashAsString();
        this.zerocoinStartedHeight = CoinDefinition.TESTNET_ZEROCOIN_STARTING_BLOCK_HEIGHT;
        this.dnsSeeds = CoinDefinition.testnetDnsSeeds;
        this.addrSeeds = null;
        this.bip32HeaderPub = 0x3a8061a0;
        this.bip32HeaderPriv = 0x3a805837;
        this.strSporkKey = "04e1af59b5298e695ccfe4cc59600d3b2f00d97183f3c9ef12097bef0a0d4232cb696da0deace6982a416cc0ae77bdb904422f04ff8eccb5b99d370ce8ad7ee710";
        this.majorityEnforceBlockUpgrade = 51;
        this.majorityRejectBlockOutdated = 75;
        this.majorityWindow = 100;
    }

    public static synchronized TestNet3Params get() {
        TestNet3Params testNet3Params;
        synchronized (TestNet3Params.class) {
            if (instance == null) {
                instance = new TestNet3Params();
            }
            testNet3Params = instance;
        }
        return testNet3Params;
    }

    public String getPaymentProtocolId() {
        return NetworkParameters.PAYMENT_PROTOCOL_ID_TESTNET;
    }

    public void checkDifficultyTransitions(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) throws VerificationException, BlockStoreException {
        if (isDifficultyTransitionPoint(storedPrev) || !nextBlock.getTime().after(testnetDiffDate)) {
            super.checkDifficultyTransitions(storedPrev, nextBlock, blockStore);
            return;
        }
        long timeDelta = nextBlock.getTimeSeconds() - storedPrev.getHeader().getTimeSeconds();
        if (timeDelta >= 0 && timeDelta <= 120) {
            StoredBlock cursor = storedPrev;
            while (!cursor.getHeader().equals(getGenesisBlock()) && cursor.getHeight() % getInterval() != 0 && cursor.getHeader().getDifficultyTargetAsInteger().equals(getMaxTarget())) {
                cursor = cursor.getPrev(blockStore);
            }
            if (!cursor.getHeader().getDifficultyTargetAsInteger().equals(nextBlock.getDifficultyTargetAsInteger())) {
                throw new VerificationException("Testnet block transition that is not allowed: " + Long.toHexString(cursor.getHeader().getDifficultyTarget()) + " vs " + Long.toHexString(nextBlock.getDifficultyTarget()));
            }
        }
    }
}
