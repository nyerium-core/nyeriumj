package org.nyeriumj.core;

import org.nyeriumj.utils.ListenerRegistration;
import org.nyeriumj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;
import static org.nyeriumj.core.SporkManager.SPORK_8_MASTERNODE_PAYMENT_ENFORCEMENT;

/**
 * Created by Eric on 2/21/2016.
 */
public class MasternodeSync {
    private static final Logger log = LoggerFactory.getLogger(MasternodeSync.class);
    public static final int MASTERNODE_SYNC_INITIAL       =     0;
    public static final int MASTERNODE_SYNC_SPORKS        =     1;
    public static final int  MASTERNODE_SYNC_LIST         =     2;
    public static final int  MASTERNODE_SYNC_MNW          =     3;
    public static final int  MASTERNODE_SYNC_BUDGET       =     4;
    public static final int  MASTERNODE_SYNC_BUDGET_PROP  =     10;
    public static final int  MASTERNODE_SYNC_BUDGET_FIN   =     11;
    public static final int  MASTERNODE_SYNC_FAILED       =     998;
    public static final int  MASTERNODE_SYNC_FINISHED     =     999;

    static final int MASTERNODE_SYNC_TICK_SECONDS    = 6;
    static final int MASTERNODE_SYNC_TIMEOUT_SECONDS = 30; // our blocks are 2.5 minutes so 30 seconds should be fine



    public HashMap<Sha256Hash, Integer> mapSeenSyncBudget = new HashMap();
    public HashMap<Sha256Hash, Integer> mapSeenSyncMNB = new HashMap();
    public HashMap<Sha256Hash, Integer> mapSeenSyncMNW = new HashMap();

    long lastMasternodeList;
    long lastMasternodeWinner;
    long lastBudgetItem;
    long lastFailure;
    int nCountFailures;

    // sum of all counts
    int sumMasternodeList;
    int sumMasternodeWinner;
    int sumBudgetItemProp;
    int sumBudgetItemFin;
    // peers that reported counts
    int countMasternodeList;
    int countMasternodeWinner;
    int countBudgetItemProp;
    int countBudgetItemFin;

    // Count peers we've requested the list from
    int RequestedMasternodeAssets;
    int RequestedMasternodeAttempt;

    // Time when current masternode asset sync started
    long nAssetSyncStarted;

    StoredBlock currentBlock;

    //NetworkParameters params;
    AbstractBlockChain blockChain;
    Context context;

    public int masterNodeCountFromNetwork() {
        return countMasternodeList != 0 ? sumMasternodeList / countMasternodeList : 0;
    }

    void setBlockChain(AbstractBlockChain blockChain) {
        this.blockChain = blockChain;
        updateBlockTip(blockChain.chainHead);
    }

    public MasternodeSync(Context context)
    {
        this.context = context;

        //this.mapSeenSyncBudget = new HashMap<Sha256Hash, Integer>();
        //this.mapSeenSyncMNB = new HashMap<Sha256Hash, Integer>();
        //this.mapSeenSyncMNW = new HashMap<Sha256Hash, Integer>();

        //eventListeners = new CopyOnWriteArrayList<ListenerRegistration<MasternodeSyncListener>>();

        reset();
    }


    void reset()
    {
        lastMasternodeList = Utils.currentTimeSeconds();
        lastMasternodeWinner = Utils.currentTimeSeconds();
        lastBudgetItem = Utils.currentTimeSeconds();
        mapSeenSyncMNB.clear();
        mapSeenSyncMNW.clear();
        mapSeenSyncBudget.clear();
        lastFailure = 0;
        nCountFailures = 0;
        sumMasternodeList = 0;
        sumMasternodeWinner = 0;
        sumBudgetItemProp = 0;
        sumBudgetItemFin = 0;
        countMasternodeList = 0;
        countMasternodeWinner = 0;
        countBudgetItemProp = 0;
        countBudgetItemFin = 0;
        RequestedMasternodeAssets = MASTERNODE_SYNC_INITIAL;
        RequestedMasternodeAttempt = 0;
        nAssetSyncStarted = Utils.currentTimeSeconds();
    }

    void addedMasternodeList(Sha256Hash hash)
    {
//        if (!context.masternodeManager.mapSeenMasternodeBroadcast.containsKey(hash)) {
//            if ( mapSeenSyncMNB.get(hash) < MASTERNODE_SYNC_THRESHOLD) {
//                    lastMasternodeList = Utils.currentTimeSeconds();
//                    mapSeenSyncMNB.put(hash, mapSeenSyncMNB.get(hash) + 1);
//                }
//            else {
//                mapSeenSyncMNB.put(hash, 1);
//                lastMasternodeList = Utils.currentTimeSeconds();
//            }
//        }

        if(context.masternodeManager.mapSeenMasternodeBroadcast.containsKey(hash)) {
            Integer count = mapSeenSyncMNB.get(hash);
            if(count != null) {  //ek or count != null
                lastMasternodeList = Utils.currentTimeSeconds();
                mapSeenSyncMNB.put(hash, mapSeenSyncMNB.get(hash)+1);
            }
            else {
                mapSeenSyncMNB.put(hash, 1);
                lastMasternodeList = Utils.currentTimeSeconds();
            }
        } else {
            lastMasternodeList = Utils.currentTimeSeconds();
            mapSeenSyncMNB.put(hash, 1);
        }
    }

    boolean isSynced()
    {
        return RequestedMasternodeAssets == MASTERNODE_SYNC_FINISHED;
    }

    boolean isBudgetPropEmpty()
    {
        return sumBudgetItemProp==0 && countBudgetItemProp>0;
    }

    boolean isBudgetFinEmpty()
    {
        return sumBudgetItemFin==0 && countBudgetItemFin>0;
    }

    void getNextAsset()
    {
        switch(RequestedMasternodeAssets)
        {
            case(MASTERNODE_SYNC_INITIAL):
            case(MASTERNODE_SYNC_FAILED): // should never be used here actually, use Reset() instead
                clearFulfilledRequest();
                RequestedMasternodeAssets = MASTERNODE_SYNC_SPORKS;
                break;
            case(MASTERNODE_SYNC_SPORKS):
                lastMasternodeList = Utils.currentTimeSeconds();
                RequestedMasternodeAssets = MASTERNODE_SYNC_LIST;
                if (context.isLiteMode() && this.context.allowInstantXinLiteMode()) {
                    RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;
                    queueOnSyncStatusChanged(RequestedMasternodeAssets, 1.0d);
                    break;
                }
            case(MASTERNODE_SYNC_LIST):
                lastMasternodeWinner = Utils.currentTimeSeconds();
                log.info("CMasternodeSync::GetNextAsset - Sync has finished");
                RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;  //TODO:  Reactivate when sync needs Winners and Budget
                queueOnSyncStatusChanged(RequestedMasternodeAssets, 1.0d);
                break;
        }
        RequestedMasternodeAttempt = 0;
        nAssetSyncStarted = Utils.currentTimeSeconds();
    }

    public int getSyncStatusInt()
    { return RequestedMasternodeAssets; }

    public String getSyncStatus()
    {
        switch (RequestedMasternodeAssets) {
            case MASTERNODE_SYNC_INITIAL: return ("Synchronization pending...");
            case MASTERNODE_SYNC_SPORKS: return ("Synchronizing sporks...");
            case MASTERNODE_SYNC_LIST: return ("Synchronizing masternodes...");
            case MASTERNODE_SYNC_MNW: return ("Synchronizing masternode winners...");
            case MASTERNODE_SYNC_BUDGET: return ("Synchronizing governance objects...");
            case MASTERNODE_SYNC_FAILED: return ("Synchronization failed");
            case MASTERNODE_SYNC_FINISHED: return ("Synchronization finished");
        }
        return "";
    }
    public String getAssetName()
    {
        switch(RequestedMasternodeAssets)
        {
            case(MASTERNODE_SYNC_INITIAL):
                return "MASTERNODE_SYNC_INITIAL";
            case(MASTERNODE_SYNC_SPORKS):
                return "MASTERNODE_SYNC_SPORKS";
            case(MASTERNODE_SYNC_LIST):
                return "MASTERNODE_SYNC_LIST";
            case(MASTERNODE_SYNC_MNW):
                return "MASTERNODE_SYNC_MNW";
            case(MASTERNODE_SYNC_BUDGET):
                return "MASTERNODE_SYNC_BUDGET";
            case(MASTERNODE_SYNC_FAILED): // should never be used here actually, use Reset() instead
                return "MASTERNODE_SYNC_FAILED";
        }
        return "Invalid asset name";
    }


    void processSyncStatusCount(Peer peer, SyncStatusCount ssc)
    {

        if(RequestedMasternodeAssets >= MASTERNODE_SYNC_FINISHED) return;

        //this means we will receive no further communication
        switch(ssc.itemId)
        {
            case(MASTERNODE_SYNC_LIST):
                if(ssc.itemId != RequestedMasternodeAssets) return;
                sumMasternodeList += ssc.count;
                countMasternodeList++;
                peer.setMasternodeListCount(ssc.count);
                break;
            case(MASTERNODE_SYNC_MNW):
                if(ssc.itemId != RequestedMasternodeAssets) return;
                sumMasternodeWinner += ssc.count;
                countMasternodeWinner++;
                break;
            case(MASTERNODE_SYNC_BUDGET_PROP):
                if(RequestedMasternodeAssets != MASTERNODE_SYNC_BUDGET) return;
                sumBudgetItemProp += ssc.count;
                countBudgetItemProp++;
                break;
            case(MASTERNODE_SYNC_BUDGET_FIN):
                if(RequestedMasternodeAssets != MASTERNODE_SYNC_BUDGET) return;
                sumBudgetItemFin += ssc.count;
                countBudgetItemFin++;
                break;
        }

        log.info("CMasternodeSync:ProcessMessage - ssc - got inventory count {} {}", ssc.itemId, ssc.count);
        //queueOnSyncStatusChanged(RequestedMasternodeAssets);

    }

    void clearFulfilledRequest()
    {
        //TODO:get the peergroup lock
        //TRY_LOCK(cs_vNodes, lockRecv);
        //if(!lockRecv) return;

        if(context.peerGroup == null)
            return;

        ReentrantLock nodeLock = context.peerGroup.getLock();

        if(!nodeLock.tryLock())
            return;

        try {
            for (Peer pnode : context.peerGroup.getConnectedPeers())
            //BOOST_FOREACH(CNode* pnode, vNodes)
            {
                pnode.clearFulfilledRequest("spork-sync");
                pnode.clearFulfilledRequest("masternode-winner-sync");
                pnode.clearFulfilledRequest("governance-sync");
                pnode.clearFulfilledRequest("masternode-sync");
            }
        } finally {
            nodeLock.unlock();
        }
    }

    static boolean fBlockchainSynced = false;
    static long lastProcess = Utils.currentTimeSeconds();

    public boolean isBlockchainSynced()
    {
        // if the last call to this function was more than 60 minutes ago (client was in sleep mode) reset the sync process
        if(Utils.currentTimeSeconds() - lastProcess > 60*60) {
            reset();
            fBlockchainSynced = false;
        }
        lastProcess = Utils.currentTimeSeconds();

        if(fBlockchainSynced) return true;

        if(currentBlock == null) return false;

        if(currentBlock.getHeader().getTimeSeconds() + 60*60 < Utils.currentTimeSeconds())
            return false;

        fBlockchainSynced = true;

        return true;
    }


    static int tick = 0;
    static long nTimeLastProcess = Utils.currentTimeSeconds();
    static int nLastTick = 0;
    static int nLastVotes = 0;
    static long nTimeNoObjectsLeft = 0;

    public void processTick() {
        if(tick++ % MASTERNODE_SYNC_TICK_SECONDS != 0) return;


            int mnCount = this.context.masternodeManager.countEnabled();
            if (isSynced()) {
                if (this.context.masternodeManager.countEnabled() == 0) {
                    reset();
                } else {
                    return;
                }
            }
            if (this.RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED && this.lastFailure + 60 < Utils.currentTimeSeconds()) {
                reset();
            } else if (this.RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED) {
                return;
            }
            //log.info("CMasternodeSync::Process() - tick {} RequestedMasternodeAttempt {} RequestedMasternodeAssets {} nSyncProgress {}", new Object[]{Integer.valueOf(tick), Integer.valueOf(this.RequestedMasternodeAttempt), Integer.valueOf(this.RequestedMasternodeAssets), Double.valueOf((((double) this.RequestedMasternodeAttempt) + (((double) (this.RequestedMasternodeAssets - 1)) * 8.0d)) / 32.0d)});
            //queueOnSyncStatusChanged(this.RequestedMasternodeAssets, nSyncProgress);
            double nSyncProgress = (RequestedMasternodeAttempt + (RequestedMasternodeAssets - 1) * 8.0D) / 32.0D;
            log.info("CMasternodeSync::Process() - tick {} RequestedMasternodeAttempt {} RequestedMasternodeAssets {} nSyncProgress {}", new Object[] { Integer.valueOf(tick), Integer.valueOf(RequestedMasternodeAttempt), Integer.valueOf(RequestedMasternodeAssets), Double.valueOf(nSyncProgress) });
            //queueOnSyncStatusChanged(RequestedMasternodeAssets, nSyncProgress);


            if (this.RequestedMasternodeAssets == 0) {
                getNextAsset();
            }
            if ((!this.context.getParams().getId().equals(NetworkParameters.ID_REGTEST) || isBlockchainSynced() || this.RequestedMasternodeAssets <= 1) && this.context.peerGroup != null) {
                ReentrantLock nodeLock = this.context.peerGroup.getLock();
                if (nodeLock.tryLock()) {
                    for (Peer pnode : this.context.peerGroup.getConnectedPeers()) {
                        if (this.context.getParams().getId().equals(NetworkParameters.ID_REGTEST)) {
                            if (this.RequestedMasternodeAttempt <= 2) {
                                pnode.sendMessage(new GetSporksMessage(this.context.getParams()));
                            } else if (this.RequestedMasternodeAttempt < 4) {
                                this.context.masternodeManager.dsegUpdate(pnode);
                            } else if (this.RequestedMasternodeAttempt < 6) {
                                pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(this.context.getParams(), this.context.masternodeManager.countEnabled()));
                                pnode.sendMessage(new GetMasternodeVoteSyncMessage(this.context.getParams(), Sha256Hash.ZERO_HASH));
                            } else {
                                this.RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;
                            }
                            this.RequestedMasternodeAttempt++;
                            nodeLock.unlock();
                            return;
                        }
                        if (this.RequestedMasternodeAssets == 1) {
                            if (pnode.hasFulfilledRequest("spork-sync")) {
                                continue;
                            } else {
                                pnode.fulfilledRequest("spork-sync");
                                pnode.sendMessage(new GetSporksMessage(this.context.getParams()));
                                if (this.RequestedMasternodeAssets == 1) {
                                    getNextAsset();
                                    nodeLock.unlock();
                                    return;
                                }
                            }
                        }
                        if (this.RequestedMasternodeAssets != 2) {
                            try {
                                if (this.RequestedMasternodeAssets == 3) {
                                    if (pnode.getPeerVersionMessage().clientVersion < this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                                        continue;
                                    } else if (this.lastMasternodeWinner < Utils.currentTimeSeconds() - 30) {
                                        getNextAsset();
                                        nodeLock.unlock();
                                        return;
                                    } else if (this.context.masternodePayments.isEnoughData(mnCount)) {
                                        getNextAsset();
                                        nodeLock.unlock();
                                        return;
                                    } else if (!pnode.hasFulfilledRequest("masternode-winner-sync")) {
                                        pnode.fulfilledRequest("masternode-winner-sync");
                                        pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(this.context.getParams(), mnCount));
                                        this.RequestedMasternodeAttempt++;
                                        nodeLock.unlock();
                                        return;
                                    }
                                } else if (this.RequestedMasternodeAssets == 4 && pnode.getPeerVersionMessage().clientVersion >= this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                                    if (this.lastBudgetItem < Utils.currentTimeSeconds() - 30) {
                                        getNextAsset();
                                        this.context.activeMasternode.manageStatus();
                                        nodeLock.unlock();
                                        return;
                                    } else if (!pnode.hasFulfilledRequest("governance-sync")) {
                                        pnode.fulfilledRequest("governance-sync");
                                        pnode.sendMessage(new GetMasternodeVoteSyncMessage(this.context.getParams(), Sha256Hash.ZERO_HASH));
                                        this.RequestedMasternodeAttempt++;
                                        nodeLock.unlock();
                                        return;
                                    }
                                }
                            } finally {
                                nodeLock.unlock();
                            }
                        } else if (pnode.getPeerVersionMessage().clientVersion < this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                            continue;
                        } else if (mnCount > this.context.masternodeManager.getEstimatedMasternodes((int) (((double) this.currentBlock.getHeight()) * 0.9d))) {
                            getNextAsset();
                            nodeLock.unlock();
                            return;
                        } else if (this.lastMasternodeList < Utils.currentTimeSeconds() - 30) {
                            getNextAsset();
                            nodeLock.unlock();
                            return;
                        } else if (!pnode.hasFulfilledRequest("masternode-sync")) {
                            pnode.fulfilledRequest("masternode-sync");
                            this.context.masternodeManager.dsegUpdate(pnode);
                            this.RequestedMasternodeAttempt++;
                            return;
                        }
                    }
                    nodeLock.unlock();
                }
            }

    }

    /* ek public void processTick() {
        int i = tick;
        tick = i + 1;
        if (i % 6 == 0 && this.currentBlock != null) {
            int mnCount = this.context.masternodeManager.countEnabled();
            if (isSynced()) {
                if (this.context.masternodeManager.countEnabled() == 0) {
                    reset();
                } else {
                    return;
                }
            }
            if (this.RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED && this.lastFailure + 60 < Utils.currentTimeSeconds()) {
                reset();
            } else if (this.RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED) {
                return;
            }

            double nSyncProgress = (RequestedMasternodeAttempt + (RequestedMasternodeAssets - 1) * 8.0D) / 32.0D;
            log.info("CMasternodeSync::Process() - tick {} RequestedMasternodeAttempt {} RequestedMasternodeAssets {} nSyncProgress {}", new Object[] { Integer.valueOf(tick), Integer.valueOf(RequestedMasternodeAttempt), Integer.valueOf(RequestedMasternodeAssets), Double.valueOf(nSyncProgress) });
            queueOnSyncStatusChanged(RequestedMasternodeAssets, nSyncProgress);

            if (this.RequestedMasternodeAssets == 0) {
                getNextAsset();
            }
            if ((!this.context.getParams().getId().equals(NetworkParameters.ID_REGTEST) || isBlockchainSynced() || this.RequestedMasternodeAssets <= 1) && this.context.peerGroup != null) {
                ReentrantLock nodeLock = this.context.peerGroup.getLock();
                if (nodeLock.tryLock()) {
                    for (Peer pnode : this.context.peerGroup.getConnectedPeers()) {
                        if (this.context.getParams().getId().equals(NetworkParameters.ID_REGTEST)) {
                            if (this.RequestedMasternodeAttempt <= 2) {
                                pnode.sendMessage(new GetSporksMessage(this.context.getParams()));
                            } else if (this.RequestedMasternodeAttempt < 4) {
                                this.context.masternodeManager.dsegUpdate(pnode);
                            } else if (this.RequestedMasternodeAttempt < 6) {
                                pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(this.context.getParams(), this.context.masternodeManager.countEnabled()));
                                pnode.sendMessage(new GetMasternodeVoteSyncMessage(this.context.getParams(), Sha256Hash.ZERO_HASH));
                            } else {
                                this.RequestedMasternodeAssets = 999;
                            }
                            this.RequestedMasternodeAttempt++;
                            nodeLock.unlock();
                            return;
                        }
                        if (this.RequestedMasternodeAssets == 1) {
                            if (pnode.hasFulfilledRequest("getspork")) {
                                continue;
                            } else {
                                pnode.fulfilledRequest("getspork");
                                pnode.sendMessage(new GetSporksMessage(this.context.getParams()));
                                if (this.RequestedMasternodeAssets == 1) {
                                    getNextAsset();
                                    nodeLock.unlock();
                                    return;
                                }
                            }
                        }
                        if (this.RequestedMasternodeAssets != 2) {
                            try {
                                if (this.RequestedMasternodeAssets == 3) {
                                    if (pnode.getPeerVersionMessage().clientVersion < this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                                        continue;
                                    } else if (this.lastMasternodeWinner < Utils.currentTimeSeconds() - 30) {
                                        getNextAsset();
                                        nodeLock.unlock();
                                        return;
                                    } else if (this.context.masternodePayments.isEnoughData(mnCount)) {
                                        getNextAsset();
                                        nodeLock.unlock();
                                        return;
                                    } else if (!pnode.hasFulfilledRequest("mnsync")) {
                                        pnode.fulfilledRequest("mnsync");
                                        pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(this.context.getParams(), mnCount));
                                        this.RequestedMasternodeAttempt++;
                                        nodeLock.unlock();
                                        return;
                                    }
                                } else if (this.RequestedMasternodeAssets == 4 && pnode.getPeerVersionMessage().clientVersion >= this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                                    if (this.lastBudgetItem < Utils.currentTimeSeconds() - 30) {
                                        getNextAsset();
                                        this.context.activeMasternode.manageStatus();
                                        nodeLock.unlock();
                                        return;
                                    } else if (!pnode.hasFulfilledRequest("mnwsync")) {
                                        pnode.fulfilledRequest("mnwsync");
                                        pnode.sendMessage(new GetMasternodeVoteSyncMessage(this.context.getParams(), Sha256Hash.ZERO_HASH));
                                        this.RequestedMasternodeAttempt++;
                                        nodeLock.unlock();
                                        return;
                                    }
                                }
                            } finally {
                                nodeLock.unlock();
                            }
                        } else if (pnode.getPeerVersionMessage().clientVersion < this.context.masternodePayments.getMinMasternodePaymentsProto()) {
                            continue;
                        } else if (mnCount > this.context.masternodeManager.getEstimatedMasternodes((int) (((double) this.currentBlock.getHeight()) * 0.9d))) {
                            getNextAsset();
                            nodeLock.unlock();
                            return;
                        } else if (this.lastMasternodeList < Utils.currentTimeSeconds() - 30) {
                            getNextAsset();
                            nodeLock.unlock();
                            return;
                        } else if (!pnode.hasFulfilledRequest("busync")) {
                            pnode.fulfilledRequest("busync");
                            this.context.masternodeManager.dsegUpdate(pnode);
                            this.RequestedMasternodeAttempt++;
                            return;
                        }
                    }
                    nodeLock.unlock();
                }
            }
        }
    }
*/ //ek

    /******************************************************************************************************************/

    //region Event listeners
    private transient CopyOnWriteArrayList<ListenerRegistration<MasternodeSyncListener>> eventListeners;
    /**
     * Adds an event listener object. Methods on this object are called when something interesting happens,
     * like receiving money. Runs the listener methods in the user thread.
     */
    public void addEventListener(MasternodeSyncListener listener) {
        addEventListener(listener, Threading.USER_THREAD);
    }

    /**
     * Adds an event listener object. Methods on this object are called when something interesting happens,
     * like receiving money. The listener is executed by the given executor.
     */
    public void addEventListener(MasternodeSyncListener listener, Executor executor) {
        // This is thread safe, so we don't need to take the lock.
        eventListeners.add(new ListenerRegistration<MasternodeSyncListener>(listener, executor));
        //keychain.addEventListener(listener, executor);
    }

    /**
     * Removes the given event listener object. Returns true if the listener was removed, false if that listener
     * was never added.
     */
    public boolean removeEventListener(MasternodeSyncListener listener) {
        //keychain.removeEventListener(listener);
        return ListenerRegistration.removeFromList(listener, eventListeners);
    }

    private void queueOnSyncStatusChanged(final int newStatus, final double syncStatus) {
        //checkState(lock.isHeldByCurrentThread());
        for (final ListenerRegistration<MasternodeSyncListener> registration : eventListeners) {
            if (registration.executor == Threading.SAME_THREAD) {
                registration.listener.onSyncStatusChanged(newStatus, syncStatus);
            } else {
                registration.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        registration.listener.onSyncStatusChanged(newStatus, syncStatus);
                    }
                });
            }
        }
    }
//    void acceptedBlockHeader(StoredBlock pindexNew)
//    {
//        log.info("mnsync--CMasternodeSync::AcceptedBlockHeader -- pindexNew->nHeight: " + pindexNew.getHeight());
//
//        if (!isBlockchainSynced()) {
//            // Postpone timeout each time new block header arrives while we are still syncing blockchain
//            BumpAssetLastTime("CMasternodeSync::AcceptedBlockHeader");
//        }
//    }

//    void notifyHeaderTip(StoredBlock pindexNew, boolean fInitialDownload)
//    {
//        log.info("mnsync--CMasternodeSync::NotifyHeaderTip -- pindexNew->nHeight: "+pindexNew.getHeight()+" fInitialDownload="+fInitialDownload);
//
//        if (isFailed() || isSynced() /*|| !pindexBestHeader*/)
//            return;
//
//        if (!isBlockchainSynced()) {
//            // Postpone timeout each time new block arrives while we are still syncing blockchain
//            BumpAssetLastTime("CMasternodeSync::NotifyHeaderTip");
//        }
//    }
    public void updateBlockTip(StoredBlock tip) {
        currentBlock = tip;
    }


//    static boolean fReachedBestHeader = false;
//    void updateBlockTip(StoredBlock pindexNew)
//    {
//        log.info("mnsync--CMasternodeSync::UpdatedBlockTip -- pindexNew->nHeight:  "+pindexNew.getHeight()+" fInitialDownload="+fInitialDownload);
//
//        if (isFailed() || isSynced() /*|| !pindexBestHeader*/)
//            return;
//
//        if (!isBlockchainSynced()) {
//            // Postpone timeout each time new block arrives while we are still syncing blockchain
//            BumpAssetLastTime("CMasternodeSync::UpdatedBlockTip");
//        }
//
//        if (fInitialDownload) {
//            // switched too early
//            if (isBlockchainSynced()) {
//                reset();
//            }
//
//            // no need to check any further while still in IBD mode
//            return;
//        }
//
//        // Note: since we sync headers first, it should be ok to use this
//        StoredBlock pindexBestHeader = blockChain.getChainHead();
//
//        boolean fReachedBestHeaderNew = pindexNew.getHeader().getHash().equals(pindexBestHeader.getHeader().getHash());
//
//        if (fReachedBestHeader && !fReachedBestHeaderNew) {
//            // Switching from true to false means that we previousely stuck syncing headers for some reason,
//            // probably initial timeout was not enough,
//            // because there is no way we can update tip not having best header
//            reset();
//            fReachedBestHeader = false;
//            return;
//        }
//
//        fReachedBestHeader = fReachedBestHeaderNew;
//
//        log.info("mnsync", "CMasternodeSync::UpdatedBlockTip -- pindexNew->nHeight: "+pindexNew.getHeight()+" pindexBestHeader->nHeight: "+pindexBestHeader.getHeight()+" fInitialDownload="+fInitialDownload+" fReachedBestHeader="+
//                fReachedBestHeader);
//
//        if (!isBlockchainSynced() && fReachedBestHeader) {
//            // Reached best header while being in initial mode.
//            // We must be at the tip already, let's move to the next asset.
//            getNextAsset();
//        }
//    }
}
