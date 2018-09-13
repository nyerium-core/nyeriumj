package org.nyeriumj.params;

import java.math.BigInteger;
import org.nyeriumj.core.Block;
import org.nyeriumj.core.NetworkParameters;
import org.nyeriumj.script.ScriptOpCodes;

public class RegTestParams extends TestNet2Params {
    private static final BigInteger MAX_TARGET = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    public RegTestParams() {
        super();
        interval = 10000;
        maxTarget = MAX_TARGET;
        subsidyDecreaseBlockCount = ScriptOpCodes.OP_DIV;
        port = 34875;
        id = NetworkParameters.ID_REGTEST;
        majorityEnforceBlockUpgrade = MainNetParams.MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MainNetParams.MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MainNetParams.MAINNET_MAJORITY_WINDOW;
    }

    @Override
    public boolean allowEmptyPeerChain() {
        return true;
    }

    private static Block genesis;

    @Override
    public Block getGenesisBlock() {
        synchronized (RegTestParams.class) {
            if (genesis == null) {
                genesis = super.getGenesisBlock();
                genesis.setNonce(2);
                genesis.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
                genesis.setTime(1527171315);
            }
        }
        return genesis;
    }

    private static RegTestParams instance;
    public static synchronized RegTestParams get() {
        if (instance == null) {
            instance = new RegTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return NetworkParameters.PAYMENT_PROTOCOL_ID_REGTEST;
    }
}
