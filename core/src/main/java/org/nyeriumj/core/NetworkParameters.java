package org.nyeriumj.core;

import com.google.common.base.Objects;
import org.nyeriumj.net.discovery.HttpDiscovery;
import org.nyeriumj.params.*;
import org.nyeriumj.script.Script;
import org.nyeriumj.script.ScriptOpCodes;
import org.nyeriumj.store.BlockStore;
import org.nyeriumj.store.BlockStoreException;
import org.nyeriumj.utils.MonetaryFormat;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;

import static org.nyeriumj.core.Coin.*;
import org.nyeriumj.utils.VersionTally;

public abstract class NetworkParameters {
    public static final byte[] SATOSHI_KEY = Utils.HEX.decode(CoinDefinition.SATOSHI_KEY);
    public static final String ID_MAINNET = CoinDefinition.ID_MAINNET;
    public static final String ID_TESTNET = CoinDefinition.ID_TESTNET;
    public static final String ID_UNITTESTNET = CoinDefinition.ID_UNITTESTNET;
    public static final String ID_REGTEST = "org.nyerium.regtest";

    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";
    public static final String PAYMENT_PROTOCOL_ID_UNIT_TESTS = "unittest";
    public static final String PAYMENT_PROTOCOL_ID_REGTEST = "regtest";

    protected int[] acceptableAddressCodes;
    protected int[] addrSeeds;
    protected int addressHeader;
    protected byte[] alertSigningKey = SATOSHI_KEY;
    protected int bip32HeaderPriv;
    protected int bip32HeaderPub;
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap();
    protected transient MessageSerializer defaultSerializer = null;
    protected String[] dnsSeeds;
    protected int dumpedPrivateKeyHeader;
    protected Block genesisBlock = createGenesis(this);
    protected HttpDiscovery.Details[] httpSeeds = {};
    protected String id;
    protected int interval;
    protected int majorityEnforceBlockUpgrade;
    protected int majorityRejectBlockOutdated;
    protected int majorityWindow;
    protected BigInteger maxTarget;
    long nStartMasternodePayments;
    protected int p2shHeader;
    protected long packetMagic;
    protected int port;
    protected int spendableCoinbaseDepth;
    String strDarksendPoolDummyAddress;
    String strMasternodePaymentsPubKey;
    protected String strSporkKey;
    protected int subsidyDecreaseBlockCount;
    protected int targetTimespan;
    protected long zerocoinStartedHeight;

    protected int budgetPaymentsStartBlock;

    /** Used to check for DIP0001 upgrade */
    protected int DIP0001Window;
    protected int DIP0001Upgrade;
    protected int DIP0001BlockHeight;
    protected boolean DIP0001ActiveAtTip = false;


    public String getSporkKey() {
        return strSporkKey;
    }

    protected NetworkParameters() {
        alertSigningKey = SATOSHI_KEY;
        genesisBlock = createGenesis(this);
    }

        private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n, Block.BLOCK_VERSION_GENESIS);
        Transaction t = new Transaction(n);
        try {
            byte[] bytes = Utils.HEX.decode(CoinDefinition.genesisTxInBytes);
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode(CoinDefinition.genesisTxOutBytes));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, Coin.valueOf(CoinDefinition.genesisBlockValue, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }


    public static final int TARGET_TIMESPAN = CoinDefinition.TARGET_TIMESPAN;
    public static final int TARGET_SPACING = CoinDefinition.TARGET_SPACING;
    public static final int INTERVAL = CoinDefinition.INTERVAL;

    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400;


    /**
     * The maximum number of coins to be generated
     */
    public static final long MAX_COINS = CoinDefinition.MAX_COINS;

    /**
     * The maximum money to be generated
     */

    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);


    /**
     * Alias for TestNet3Params.get(), use that instead.
     */
    @Deprecated
    public static NetworkParameters testNet() {
        return TestNet3Params.get();
    }

    /**
     * Alias for TestNet2Params.get(), use that instead.
     */
    @Deprecated
    public static NetworkParameters testNet2() {
        return TestNet2Params.get();
    }

    /**
     * Alias for TestNet3Params.get(), use that instead.
     */
    @Deprecated
    public static NetworkParameters testNet3() {
        return TestNet3Params.get();
    }

    /**
     * Alias for MainNetParams.get(), use that instead
     */
    @Deprecated
    public static NetworkParameters prodNet() {
        return MainNetParams.get();
    }

    /**
     * Returns a testnet context modified to allow any difficulty target.
     */
    @Deprecated
    public static NetworkParameters unitTests() {
        return UnitTestParams.get();
    }

    /**
     * Returns a standard regression test context (similar to unitTests)
     */
    @Deprecated
    public static NetworkParameters regTests() {
        return RegTestParams.get();
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    public abstract String getPaymentProtocolId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId().equals(((NetworkParameters) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Returns the network parameters for the given string ID or NULL if not recognized.
     */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_MAINNET)) {
            return MainNetParams.get();
        } else if (id.equals(ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (id.equals(ID_UNITTESTNET)) {
            return UnitTestParams.get();
        } else if (id.equals(ID_REGTEST)) {
            return RegTestParams.get();
        } else {
            return null;
        }
    }

    /**
     * Returns the network parameters for the given string paymentProtocolID or NULL if not recognized.
     */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
            return MainNetParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_UNIT_TESTS)) {
            return UnitTestParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_REGTEST)) {
            return RegTestParams.get();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Throws an exception if the block's difficulty is not correct.
     *
     * @throws VerificationException if the block's difficulty is not correct.
     */
    public abstract void checkDifficultyTransitions(StoredBlock storedPrev, Block next, final BlockStore blockStore) throws VerificationException, BlockStoreException;

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    public int getSubsidyDecreaseBlockCount() {
        return subsidyDecreaseBlockCount;
    }

    public int getBudgetPaymentsStartBlock() {
        return budgetPaymentsStartBlock;
    }

    /**
     * Returns DNS names that when resolved, give IP addresses of active peers.
     */
    public String[] getDnsSeeds() {
        return dnsSeeds;
    }

    /**
     * Returns IP address of active peers.
     */
    public int[] getAddrSeeds() {
        return addrSeeds;
    }

    /**
     * Returns discovery objects for seeds implementing the Cartographer protocol. See {@link org.nyeriumj.net.discovery.HttpDiscovery} for more info.
     */
    public HttpDiscovery.Details[] getHttpSeeds() {
        return httpSeeds;
    }

    /**
     * <p>Genesis block for this chain.</p>
     * <p>
     * <p>The first block in every chain is a well known constant shared between all Bitcoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     * <p>
     * <p>The genesis blocks for both test and main networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public Block getGenesisBlock() {
        return genesisBlock;
    }

    /**
     * Default TCP port on which to connect to nodes.
     */
    public int getPort() {
        return port;
    }

    /**
     * The header bytes that identify the start of a packet on this network.
     */
    public long getPacketMagic() {
        return packetMagic;
    }

    /**
     * First byte of a base58 encoded address. See {@link org.nyeriumj.core.Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }
    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /**
     * First byte of a base58 encoded dumped private key. See {@link org.nyeriumj.core.DumpedPrivateKey}.
     */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and main Bitcoin networks use 2 weeks (1209600 seconds).
     */
    public int getTargetTimespan() {
        return targetTimespan;
    }

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public int[] getAcceptableAddressCodes() {
        return acceptableAddressCodes;
    }

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks.
     */
    public boolean allowEmptyPeerChain() {
        return true;
    }

    /**
     * How many blocks pass between difficulty adjustment periods. Bitcoin standardises this to be 2015.
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Maximum target represents the easiest allowable proof of work.
     */
    public BigInteger getMaxTarget() {
        return maxTarget;
    }

    /**
     * The key used to sign {@link org.nyeriumj.core.AlertMessage}s. You can use {@link org.nyeriumj.core.ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public byte[] getAlertSigningKey() {
        return alertSigningKey;
    }

    /**
     * Returns the 4 byte header for BIP32 (HD) wallet - public key part.
     */
    public int getBip32HeaderPub() {
        return bip32HeaderPub;
    }

    /**
     * Returns the 4 byte header for BIP32 (HD) wallet - private key part.
     */
    public int getBip32HeaderPriv() {
        return bip32HeaderPriv;
    }

    /**
     * Returns the number of coins that will be produced in total, on this
     * network. Where not applicable, a very large number of coins is returned
     * instead (i.e. the main coin issue for Dogecoin).
     */
    public abstract Coin getMaxMoney();

    /**
     * Any standard (ie pay-to-address) output smaller than this value will
     * most likely be rejected by the network.
     */
    public abstract Coin getMinNonDustOutput();

    /**
     * The monetary object for this currency.
     */
    public abstract MonetaryFormat getMonetaryFormat();

    /**
     * Scheme part for URIs, for example "bitcoin".
     */
    public abstract String getUriScheme();

    /**
     * Returns whether this network has a maximum number of coins (finite supply) or
     * not. Always returns true for Bitcoin, but exists to be overriden for other
     * networks.
     */
    public abstract boolean hasMaxMoney();

    /**
     * Return the default serializer for this network. This is a shared serializer.
     *
     * @return
     */
    public final MessageSerializer getDefaultSerializer() {
        // Construct a default serializer if we don't have one
        if (null == this.defaultSerializer) {
            // Don't grab a lock unless we absolutely need it
            synchronized (this) {
                // Now we have a lock, double check there's still no serializer
                // and create one if so.
                if (null == this.defaultSerializer) {
                    // As the serializers are intended to be immutable, creating
                    // two due to a race condition should not be a problem, however
                    // to be safe we ensure only one exists for each network.
                    this.defaultSerializer = getSerializer(false);
                }
            }
        }
        return defaultSerializer;
    }

    /**
     * Construct and return a custom serializer.
     */
    public abstract BitcoinSerializer getSerializer(boolean parseRetain);

    /**
     * The number of blocks in the last {@link this.getMajorityWindow()} blocks
     * at which to trigger a notice to the user to upgrade their client, where
     * the client does not understand those blocks.
     */
    public int getMajorityEnforceBlockUpgrade() {
        return majorityEnforceBlockUpgrade;
    }

    /**
     * The number of blocks in the last {@link this.getMajorityWindow()} blocks
     * at which to enforce the requirement that all new blocks are of the
     * newer type (i.e. outdated blocks are rejected).
     */
    public int getMajorityRejectBlockOutdated() {
        return majorityRejectBlockOutdated;
    }

    /**
     * The sampling window from which the version numbers of blocks are taken
     * in order to determine if a new block version is now the majority.
     */
    public int getMajorityWindow() {
        return majorityWindow;
    }

    /**
     * The flags indicating which block validation tests should be applied to
     * the given block. Enables support for alternative blockchains which enable
     * tests based on different criteria.
     *
     * @param block  block to determine flags for.
     * @param height height of the block, if known, null otherwise. Returned
     *               tests should be a safe subset if block height is unknown.
     */
    public EnumSet<Block.VerifyFlag> getBlockVerificationFlags(final Block block,
                                                               final VersionTally tally, final Integer height) {
        final EnumSet<Block.VerifyFlag> flags = EnumSet.noneOf(Block.VerifyFlag.class);

        if (block.isBIP34()) {
            final Integer count = tally.getCountAtOrAbove(Block.BLOCK_VERSION_BIP34);
            if (null != count && count >= getMajorityEnforceBlockUpgrade()) {
                flags.add(Block.VerifyFlag.HEIGHT_IN_COINBASE);
            }
        }
        return flags;
    }

    /**
     * The flags indicating which script validation tests should be applied to
     * the given transaction. Enables support for alternative blockchains which enable
     * tests based on different criteria.
     *
     * @param block       block the transaction belongs to.
     * @param transaction to determine flags for.
     * @param height      height of the block, if known, null otherwise. Returned
     *                    tests should be a safe subset if block height is unknown.
     */
    public EnumSet<Script.VerifyFlag> getTransactionVerificationFlags(final Block block,
                                                                      final Transaction transaction, final VersionTally tally, final Integer height) {
        final EnumSet<Script.VerifyFlag> verifyFlags = EnumSet.noneOf(Script.VerifyFlag.class);
        if (block.getTimeSeconds() >= NetworkParameters.BIP16_ENFORCE_TIME)
            verifyFlags.add(Script.VerifyFlag.P2SH);

        // Start enforcing CHECKLOCKTIMEVERIFY, (BIP65) for block.nVersion=4
        // blocks, when 75% of the network has upgraded:
        if (block.getVersion() >= Block.BLOCK_VERSION_BIP65 &&
                tally.getCountAtOrAbove(Block.BLOCK_VERSION_BIP65) > this.getMajorityEnforceBlockUpgrade()) {
            verifyFlags.add(Script.VerifyFlag.CHECKLOCKTIMEVERIFY);
        }

        return verifyFlags;
    }

    public abstract int getProtocolVersionNum(final ProtocolVersion version);

    public static enum ProtocolVersion {
        MINIMUM(CoinDefinition.MIN_PROTOCOL_VERSION),
        PONG(60001),
        BLOOM_FILTER(CoinDefinition.MIN_PROTOCOL_VERSION),
        CURRENT(CoinDefinition.PROTOCOL_VERSION);

        private final int bitcoinProtocol;

        ProtocolVersion(final int bitcoinProtocol) {
            this.bitcoinProtocol = bitcoinProtocol;
        }

        public int getBitcoinProtocolVersion() {
            return bitcoinProtocol;
        }
    }

    //Nyerium Specific
    public long getZerocoinStartedHeight() {
        return this.zerocoinStartedHeight;
    }

    public boolean isDIP0001ActiveAtTip() { return DIP0001ActiveAtTip; }
    public void setDIPActiveAtTip(boolean active) { DIP0001ActiveAtTip = active; }
    public int getDIP0001BlockHeight() { return DIP0001BlockHeight; }

}