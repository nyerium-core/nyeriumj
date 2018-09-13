package org.nyeriumj.core;


//import org.darkcoinj.DarkSendSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.darkcoinj.DarkSendSigner;

import java.util.HashMap;

/**
 * Created by Hash Engineering on 2/20/2016.
 */
public class SporkManager {
    private static final Logger log = LoggerFactory.getLogger(SporkManager.class);

    MasternodeSignature sig;
    String strMasterPrivKey;

    HashMap<Sha256Hash, SporkMessage> mapSporks ;
    HashMap<Integer, SporkMessage> mapSporksActive ;

    AbstractBlockChain blockChain;
    Context context;

    SporkManager(Context context) {
        this.context = context;
        mapSporks = new HashMap<Sha256Hash, SporkMessage>();
        mapSporksActive = new HashMap<Integer, SporkMessage>();
    }

    void setBlockChain(AbstractBlockChain blockChain) {
        this.blockChain = blockChain;
    }

    void processSpork(Peer from, SporkMessage spork) {
        if (context.isLiteMode() && !context.allowInstantXinLiteMode()) return; //disable all darksend/masternode related functionality

        //if (chainActive.Tip() == NULL) return;

        Sha256Hash hash = spork.getHash();
        if (mapSporksActive.containsKey(spork.nSporkID)) {
            if (mapSporksActive.get(spork.nSporkID).nTimeSigned >= spork.nTimeSigned) {
                if (DarkCoinSystem.fDebug) log.info("spork - seen "+hash.toString()+" block " + blockChain.getBestChainHeight());
                return;
            } else {
                if (DarkCoinSystem.fDebug)
                    log.info("spork - got updated spork "+hash.toString()+" block " +blockChain.getBestChainHeight());
            }
        }

        log.info("spork - new "+hash.toString()+" ID "+spork.nSporkID+" Time "+spork.nTimeSigned+" bestHeight" + blockChain.getBestChainHeight());

        if (!checkSignature(spork)) {
            log.info("spork - invalid signature");
            //Misbehaving(pfrom -> GetId(), 100);
            return;
        }

        mapSporks.put(hash, spork);
        mapSporksActive.put(spork.nSporkID, spork);
        relay(spork);

        //does a task if needed
        executeSpork(spork.nSporkID, spork.nValue);
    }

    static final int SPORK_START = 10001;
    static final int SPORK_END = 10013;

    public static final int SPORK_2_SWIFTTX = 10001;
    public static final int SPORK_3_SWIFTTX_BLOCK_FILTERING = 10002;
    public static final int SPORK_5_MAX_VALUE = 10004;
    //ek--public static final int SPORK_6_NEW_SIGS = 10005;
    //ek--public static final int SPORK_7_MASTERNODE_SCANNING = 10006;
    public static final int SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT = 10007;
    public static final int SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT = 10008;
    public static final int SPORK_10_MASTERNODE_PAY_UPDATED_NODES = 10009;
    //ek --public static final int SPORK_11_LOCK_INVALID_UTXO = 10010;
    public static final int SPORK_13_ENABLE_SUPERBLOCKS = 10012;
    public static final int SPORK_14_NEW_PROTOCOL_ENFORCEMENT = 10013;
    //ek--public static final int SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2 = 10014;
    //ek--public static final int SPORK_16_ZEROCOIN_MAINTENANCE_MODE = 10015;
    //ek--public static final int SPORK_17_PROPOSAL_VETO = 10016;

    public static final long SPORK_2_SWIFTTX_DEFAULT = 0;
    public static final long SPORK_3_SWIFTTX_BLOCK_FILTERING_DEFAULT = 0;
    public static final long SPORK_5_MAX_VALUE_DEFAULT = 1000;
    //ek--public static final long SPORK_6_NEW_SIGS_DEFAULT = 4070908800L;
    //ek--public static final long SPORK_7_MASTERNODE_SCANNING_DEFAULT = 4070908800L;
    public static final long SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT = 4070908800L;
    public static final long SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT_DEFAULT = 4070908800L;
    public static final long SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT = 4070908800L;
    //ek --public static final int SPORK_11_LOCK_INVALID_UTXO_DEFAULT = 4070908800L;
    public static final long SPORK_13_ENABLE_SUPERBLOCKS_DEFAULT = 4070908800L;
    public static final long SPORK_14_NEW_PROTOCOL_ENFORCEMENT_DEFAULT = 4070908800L;
    //ek--public static final long SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2_DEFAULT = 4070908800L;
    //ek--public static final long SPORK_16_ZEROCOIN_MAINTENANCE_MODE_DEFAULT = 4070908800L;
    //ek--public static final long SPORK_17_PROPOSAL_VETO_DEFAULT = 4070908800L;

    // grab the spork, otherwise say it's off
    public boolean isSporkActive(int nSporkID)
    {
        long r = -1;

        if(mapSporksActive.containsKey(nSporkID)){
            r = mapSporksActive.get(nSporkID).nValue;
        } else {
            switch (nSporkID) {
                case SPORK_2_SWIFTTX:                               r = SPORK_2_SWIFTTX_DEFAULT; break;
                case SPORK_3_SWIFTTX_BLOCK_FILTERING:               r = SPORK_3_SWIFTTX_BLOCK_FILTERING_DEFAULT; break;
                case SPORK_5_MAX_VALUE:                             r = SPORK_5_MAX_VALUE_DEFAULT; break;
                case SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT:        r = SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT; break;
                case SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT:         r = SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT_DEFAULT; break;
                case SPORK_10_MASTERNODE_PAY_UPDATED_NODES:         r = SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT; break;
                //case SPORK_12_RECONSIDER_BLOCKS:                  r = SPORK_12_RECONSIDER_BLOCKS_DEFAULT; break;
                case SPORK_13_ENABLE_SUPERBLOCKS:                   r = SPORK_13_ENABLE_SUPERBLOCKS_DEFAULT; break;
                case SPORK_14_NEW_PROTOCOL_ENFORCEMENT:             r = SPORK_14_NEW_PROTOCOL_ENFORCEMENT_DEFAULT; break;
                default:
                    log.info("spork", "CSporkManager::IsSporkActive -- Unknown Spork ID" + nSporkID);
                    r = 4070908800L; // 2099-1-1 i.e. off by default
                    break;
            }
        }

        return r < Utils.currentTimeSeconds();
    }

    //ECKey sporkPrivKey;
    //byte[] sporkPubKeyId;


    // grab the value of the spork on the network, or the default
    public long getSporkValue(int nSporkID)
    {
        long r = -1;

        if(mapSporksActive.containsKey(nSporkID)){
            return mapSporksActive.get(nSporkID).nValue;
        } else {
            switch (nSporkID) {
                case SPORK_2_SWIFTTX:                               return SPORK_2_SWIFTTX_DEFAULT;
                case SPORK_3_SWIFTTX_BLOCK_FILTERING:               return SPORK_3_SWIFTTX_BLOCK_FILTERING_DEFAULT;
                case SPORK_5_MAX_VALUE:                             return SPORK_5_MAX_VALUE_DEFAULT;
                case SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT:        return SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT_DEFAULT;
                case SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT:         return SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT_DEFAULT;
                case SPORK_10_MASTERNODE_PAY_UPDATED_NODES:         return SPORK_10_MASTERNODE_PAY_UPDATED_NODES_DEFAULT;
                //case SPORK_12_RECONSIDER_BLOCKS:                  return SPORK_12_RECONSIDER_BLOCKS_DEFAULT;
                case SPORK_13_ENABLE_SUPERBLOCKS:                   return SPORK_13_ENABLE_SUPERBLOCKS_DEFAULT;
                case SPORK_14_NEW_PROTOCOL_ENFORCEMENT:             return SPORK_14_NEW_PROTOCOL_ENFORCEMENT_DEFAULT;
                default:
                    log.info("spork", "CSporkManager::GetSporkValue -- Unknown Spork ID "+ nSporkID);
                    return -1;
            }
        }
    }


    public void executeSpork(int nSporkID, long nValue)
    {
        //if(nSporkID == SPORK_11_RESET_BUDGET && nValue == 1){
        //   //budget.Clear();
        //}

        //correct fork via spork technology
        //if(nSporkID == SPORK_12_RECONSIDER_BLOCKS && nValue > 0) {
            //log.info("Spork::ExecuteSpork -- Reconsider Last %d Blocks\n", nValue);

        //    reprocessBlocks((int)nValue);
        //}
    }

    void reprocessBlocks(int nBlocks)
    {
        //DashJ will not do this for now.

        /*
        std::map<uint256, long>::iterator it = mapRejectedBlocks.begin();
        while(it != mapRejectedBlocks.end()){
            //use a window twice as large as is usual for the nBlocks we want to reset
            if((*it).second  > GetTime() - (nBlocks*60*5)) {
                BlockMap::iterator mi = mapBlockIndex.find((*it).first);
                if (mi != mapBlockIndex.end() && (*mi).second) {
                    LOCK(cs_main);

                    CBlockIndex* pindex = (*mi).second;
                    LogPrintf("ReprocessBlocks - %s\n", (*it).first.ToString());

                    CValidationState state;
                    ReconsiderBlock(state, pindex);
                }
            }
            ++it;
        }

        CValidationState state;
        {
            LOCK(cs_main);
            DisconnectBlocksAndReprocess(nBlocks);
        }

        if (state.IsValid()) {
            ActivateBestChain(state);
        }
        */
    }

//    // grab the spork, otherwise say it's off
//    public boolean isSporkActive(int nSporkID) {
//        long r = -1;
//
//        if (mapSporksActive.containsKey(nSporkID)) {
//            r = mapSporksActive.get(nSporkID).nValue;
//        } else if (mapSporkDefaults.containsKey(nSporkID)) {
//            r = mapSporkDefaults.get(nSporkID);
//        } else {
//            log.warn("spork--CSporkManager::IsSporkActive -- Unknown Spork ID {}", nSporkID);
//            r = 4070908800L; // 2099-1-1 i.e. off by default
//        }
//
//       return r < Utils.currentTimeSeconds();
//
//    }

//    // grab the value of the spork on the network, or the default
//    public long getSporkValue(int nSporkID) {
//        if (mapSporksActive.containsKey(nSporkID))
//            return mapSporksActive.get(nSporkID).nValue;
//
//        if (mapSporkDefaults.containsKey(nSporkID)) {
//            return mapSporkDefaults.get(nSporkID);
//        }
//
//        log.info("spork--CSporkManager::GetSporkValue -- Unknown Spork ID {}", nSporkID);
//        return -1;
//    }


    boolean checkSignature(SporkMessage spork)
    {
        //note: need to investigate why this is failing
        //std::string strMessage = boost::lexical_cast<std::string>(spork.nSporkID) + boost::lexical_cast<std::string>(spork.nValue) + boost::lexical_cast<std::string>(spork.nTimeSigned);
        String strMessage = "" + spork.nSporkID + spork.nValue + spork.nTimeSigned;
        PublicKey pubkey = new PublicKey(Utils.HEX.decode(context.getParams().getSporkKey()));

        StringBuilder errorMessage = new StringBuilder();
        if(!DarkSendSigner.verifyMessage(pubkey, spork.sig, strMessage, errorMessage)){
            return false;
        }

        return true;
    }

    boolean sign(SporkMessage spork)
    {
        //std::string strMessage = boost::lexical_cast<std::string>(spork.nSporkID) + boost::lexical_cast<std::string>(spork.nValue) + boost::lexical_cast<std::string>(spork.nTimeSigned);
        String strMessage = "" + spork.nSporkID + spork.nValue + spork.nTimeSigned;

        ECKey key2;
        PublicKey pubkey2;
        StringBuilder errorMessage = new StringBuilder();

        if((key2 = DarkSendSigner.setKey(strMasterPrivKey, errorMessage)) == null)
        {
            log.info("CMasternodePayments::Sign - ERROR: Invalid masternodeprivkey: "+ errorMessage);
            return false;
        }
        pubkey2 = new PublicKey(key2.getPubKey());

        if((spork.sig = DarkSendSigner.signMessage(strMessage, errorMessage, key2)) == null) {
            log.info("CMasternodePayments::Sign - Sign message failed");
            return false;
        }

        if(!DarkSendSigner.verifyMessage(pubkey2, spork.sig, strMessage, errorMessage)) {
            log.info("CMasternodePayments::Sign - Verify message failed");
            return false;
        }

        return true;
    }

    boolean updateSpork(int nSporkID, long nValue)
    {

        SporkMessage msg = new SporkMessage(context.getParams());
        msg.nSporkID = nSporkID;
        msg.nValue = nValue;
        msg.nTimeSigned = Utils.currentTimeSeconds();
        if(sign(msg)){
            relay(msg);
            mapSporks.put(msg.getHash(), msg);
            mapSporksActive.put(nSporkID, msg);
            return true;
        }

        return false;
    }

    void relay(SporkMessage msg) {
        //InventoryItem inv = new InventoryItem(InventoryItem.Type.Spork, msg.getHash());
        //RelayInv(inv);
        //TODO:  Not needed in SPV
    }


    boolean setPrivKey(String strPrivKey)
    {
        SporkMessage msg = new SporkMessage(context.getParams());

        // Test signing successful, proceed
        strMasterPrivKey = strPrivKey;

        sign(msg);

        if(checkSignature(msg)){
            log.info("SporkManager::setPrivKey - Successfully initialized as spork signer\n");
            return true;
        } else {
            return false;
        }
    }

    int getSporkIDByName(String strName) {
        if (strName == "SPORK_2_SWIFTTX") return SPORK_2_SWIFTTX;
        if (strName == "SPORK_3_SWIFTTX_BLOCK_FILTERING") return SPORK_3_SWIFTTX_BLOCK_FILTERING;
        if (strName == "SPORK_5_MAX_VALUE") return SPORK_5_MAX_VALUE;
//        if (strName == "SPORK_7_MASTERNODE_SCANNING") return SPORK_7_MASTERNODE_SCANNING;
        if (strName == "SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT") return SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT;
        if (strName == "SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT") return SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT;
        if (strName == "SPORK_10_MASTERNODE_PAY_UPDATED_NODES") return SPORK_10_MASTERNODE_PAY_UPDATED_NODES;
        //if (strName == "SPORK_11_LOCK_INVALID_UTXO") return SPORK_11_LOCK_INVALID_UTXO;
        if (strName == "SPORK_13_ENABLE_SUPERBLOCKS") return SPORK_13_ENABLE_SUPERBLOCKS;
        if (strName == "SPORK_14_NEW_PROTOCOL_ENFORCEMENT") return SPORK_14_NEW_PROTOCOL_ENFORCEMENT;
//        if (strName == "SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2") return SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2;
//        if (strName == "SPORK_16_ZEROCOIN_MAINTENANCE_MODE") return SPORK_16_ZEROCOIN_MAINTENANCE_MODE;
//        if (strName == "SPORK_17_PROPOSAL_VETO") return SPORK_17_PROPOSAL_VETO;

        log.info("spork", "CSporkManager::GetSporkIDByName -- Unknown Spork name: " + strName);
        return -1;
    }

    String getSporkNameByID(int id) {
        if (id == SPORK_2_SWIFTTX) return "SPORK_2_SWIFTTX";
        if (id == SPORK_3_SWIFTTX_BLOCK_FILTERING) return "SPORK_3_SWIFTTX_BLOCK_FILTERING";
        if (id == SPORK_5_MAX_VALUE) return "SPORK_5_MAX_VALUE";
        //if (id == SPORK_7_MASTERNODE_SCANNING) return "SPORK_7_MASTERNODE_SCANNING";
        if (id == SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT) return "SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT";
        if (id == SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT) return "SPORK_9_MASTERNODE_BUDGET_ENFORCEMENT";
        if (id == SPORK_10_MASTERNODE_PAY_UPDATED_NODES) return "SPORK_10_MASTERNODE_PAY_UPDATED_NODES";
       // if (id == SPORK_11_LOCK_INVALID_UTXO) return "SPORK_11_LOCK_INVALID_UTXO";
        if (id == SPORK_13_ENABLE_SUPERBLOCKS) return "SPORK_13_ENABLE_SUPERBLOCKS";
        if (id == SPORK_14_NEW_PROTOCOL_ENFORCEMENT) return "SPORK_14_NEW_PROTOCOL_ENFORCEMENT";
//        if (id == SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2) return "SPORK_15_NEW_PROTOCOL_ENFORCEMENT_2";
//        if (id == SPORK_16_ZEROCOIN_MAINTENANCE_MODE) return "SPORK_16_ZEROCOIN_MAINTENANCE_MODE";
//        if (id == SPORK_17_PROPOSAL_VETO) return "SPORK_17_PROPOSAL_VETO";

        log.info("spork", "CSporkManager::GetSporkNameByID -- Unknown Spork ID " + id);
        return "Unknown";
    }
}