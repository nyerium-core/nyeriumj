package org.nyeriumj.core;

import java.math.BigInteger;
import java.util.Map;

public class CoinDefinition {
    public static final int AddressHeader = 53;
    public static final String BLOCKEXPLORER_ADDRESS_PATH = "address/";
    public static final String BLOCKEXPLORER_BASE_URL_PROD = "http://167.99.200.1:3001/";
    public static final String BLOCKEXPLORER_BASE_URL_TEST = "http://167.99.200.1:3001/";
    public static final String BLOCKEXPLORER_BLOCK_PATH = "block/";
    public static final String BLOCKEXPLORER_TRANSACTION_PATH = "tx/";
    public static final int BLOCK_CURRENTVERSION = 2;
    public static final long DEFAULT_MIN_TX_FEE = 10000;
    public static final String DONATION_ADDRESS = "NfH3kcTePzXcWV76TEWbU6Q4R6gWhd6GwL";
    public static final long DUST_LIMIT = 30000;
    public static final String ID_MAINNET = "org.nyerium.production";
    public static final String ID_TESTNET = "org.nyerium.test";
    public static final String ID_UNITTESTNET = "com.google.nyerium.unittest";
    public static final long INSTANTX_FEE = 100000;
    public static final int INTERVAL = 60;
    public static final long MAINNET_ZEROCOIN_STARTING_BLOCK_HEIGHT = 100000;
    public static final int MAX_BLOCK_SIZE = 1000000;
    public static final long MAX_COINS = 120000000;
    public static final int MIN_PROTOCOL_VERSION = 70003;
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED = "[X]";
    public static final String PATTERN_PRIVATE_KEY_START_UNCOMPRESSED = "[7]";
    public static final int PROTOCOL_VERSION = 70003;
    public static final long PacketMagic = 0x6a4b4c5d;
    public static final int Port = 57418;
    public static final String SATOSHI_KEY = "049020f785c6eafdc1fa96335104e5c7d8d2d57c616b9137011dbce6e83952bc5021a15025fdd689172fb73c0f092003b3da8b64d96d41d6efe6f1cc00e0c622dd";
    public static final int TARGET_SPACING = 60;
    public static final int TARGET_TIMESPAN = 60;
    public static final String TESTNET_SATOSHI_KEY = "0430c07e112c4b4cffd0e691dda23035ccec26bf6dd169f92b8043a63eaab99639da5a30f17b0a9167ad96b0cff271524d94849e51b1735652561f224f1cd44a81";
    public static final long TESTNET_ZEROCOIN_STARTING_BLOCK_HEIGHT = 50000;
    public static final int TestPort = 51434;
    public static final String UNITTEST_ADDRESS = "XgxQxd6B8iYgEEryemnJrpvoWZ3149MCkK";
    public static final String UNITTEST_ADDRESS_PRIVATE_KEY = "XDtvHyDHk4S3WJvwjxSANCpZiLLkKzoDnjrcRhca2iLQRtGEz1JZ";
    public static final String UNSPENT_API_URL = "https://chainz.cryptoid.info/dash/api.dws?q=unspent";
    public static final UnspentAPIType UnspentAPI = UnspentAPIType.Cryptoid;
    public static final CoinHash coinPOWHash = CoinHash.XEVAN;
    public static boolean checkpointFileSupport = true;
    public static final String coinName = "Nyerium";
    public static final CoinPrecision coinPrecision = CoinPrecision.Coins;
    public static final String coinTicker = "NYEX";
    public static final String coinURIScheme = "nyerium";
    public static final String cryptsyMarketCurrency = "BTC";
    public static final String cryptsyMarketId = "155";
    public static String[] dnsSeeds = new String[]{"138.68.189.143", "188.166.157.24", "138.68.178.26",};
    public static final int dumpedPrivateKeyHeader = 128; //EK or 128
    public static final boolean feeCanBeRaised = false;
    public static long genesisBlockDifficultyTarget = (0x1e0ffff0L);
    public static long genesisBlockTime = 1527171315L;
    public static long genesisBlockNonce = (795796);
    public static int genesisBlockValue = 0;
//    public static String genesisTxInBytes = "04ffff001d01044c55552e532e204e657773202620576f726c64205265706f7274204a616e203238203230313620576974682048697320416273656e63652c205472756d7020446f6d696e6174657320416e6f7468657220446562617465";
    public static String genesisTxInBytes = "04ffff001d01044c4e323520417072696c20323031382c20546865206368616e63656c6c6f7220697320617420697420616761696e207769746820612062657474657220746f6b656e2e204361722062756d70204e5945";
    public static String genesisTxOutBytes = "04a0339ee97d36bb216e86017c9fd4f696f93989371f1a579edbd28e958de8e41b64bcbf9511b42a62c9416e5d64212f2fa9db262d85a45e7fb5f83e73ea620c01";
//    public static String genesisTxOutBytes = "04a0339ee97d36bb216e86017c9fd4f696f93989371f1a579edbd28e958de8e41b64bcbf9511b42a62c9416e5d64212f2fa9db262d85a45e7fb5f83e73ea620c01";
    public static String genesisHash = "00000849fd3704944ef2ef374f70d79dcc3fd58ac29dc459114aa8b0788378f3";
    public static String genesisMerkleRoot = "1fa76a782dd8eee188cfbe0653fdf81bfd5c105f808bd5c9a42e23cb77635802";
    public static int minBroadcastConnections = 0;
    //public static final long oldPacketMagic = -71256357;
    public static final int p2shHeader = 8;
    public static BigInteger proofOfWorkLimit = Utils.decodeCompactBits(504365055); //EK try 8000
    public static int spendableCoinbaseDepth = 10; //EK or 100
    public static int subsidyDecreaseBlockCount = 210000;
    public static final boolean supportsBloomFiltering = true;
    public static final boolean supportsTestNet = false;
    public static final int testnetAddressHeader = 112;
    static public String[] testnetDnsSeeds = new String[] {"13.127.206.177", "80.211.145.115", "13.127.26.220",};    public static long testnetGenesisBlockDifficultyTarget = (0x1e0ffff0L);
    public static long testnetGenesisBlockNonce = (795796);
    public static long testnetGenesisBlockTime = 1527171315L;
    public static final String testnetGenesisHash = "00000849fd3704944ef2ef374f70d79dcc3fd58ac29dc459114aa8b0788378f3";
    public static final long testnetPacketMagic = (0x04f7e6dbc);
    public static final int testnetp2shHeader = 19;

    enum CoinHash {
        SHA256,
        scrypt,
        x11,
        XEVAN
    }

    public enum CoinPrecision {
        Coins,
        Millicoins
    }

    public enum UnspentAPIType {
        BitEasy,
        Blockr,
        Abe,
        Cryptoid
    }

    public static final int getInterval(int height, boolean testNet) {
        return INTERVAL;      //108
    }
    public static final int getIntervalCheckpoints() {
        return INTERVAL;

    }
    public static final int getTargetTimespan(int height, boolean testNet) {
        return TARGET_TIMESPAN;    //72 min
    }

    public static final Coin GetBlockReward(int height) {
        Coin nSubsidy = Coin.valueOf(100, 0);
        if (height == 1) {
            nSubsidy = Coin.valueOf(420000, 0);
        }
        return nSubsidy;
    }

    public static void initCheckpoints(Map<Integer, Sha256Hash> map) {
    }
}
