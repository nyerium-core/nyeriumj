package org.darkcoinj;

import org.nyeriumj.core.MasterNodePaymentWinner;

import java.util.ArrayList;

/**
 * Created by Eric on 2/8/2015.
 */

//
// Masternode Payments Class
// Keeps track of who should get paid for which blocks
//
public class MasterNodePayments {
    ArrayList<MasterNodePaymentWinner> vWinning;
    int nSyncedFromPeer;
    String strMasterPrivKey;
    String strTestPubKey;
    String strMainPubKey;
    boolean enabled;

    MasterNodePayments() {
        strMainPubKey = "04a0339ee97d36bb216e86017c9fd4f696f93989371f1a579edbd28e958de8e41b64bcbf9511b42a62c9416e5d64212f2fa9db262d85a45e7fb5f83e73ea620c01";
        strTestPubKey = "0430c07e112c4b4cffd0e691dda23035ccec26bf6dd169f92b8043a63eaab99639da5a30f17b0a9167ad96b0cff271524d94849e51b1735652561f224f1cd44a81";
        enabled = false;
    }
    /*
    bool SetPrivKey(std::string strPrivKey);
    bool CheckSignature(CMasternodePaymentWinner& winner);
    bool Sign(CMasternodePaymentWinner& winner);

    // Deterministically calculate a given "score" for a masternode depending on how close it's hash is
    // to the blockHeight. The further away they are the better, the furthest will win the election
    // and get paid this block
    //

    uint64_t CalculateScore(Sha256Hash blockHash, CTxIn& vin);
    bool GetWinningMasternode(int nBlockHeight, CTxIn& vinOut);
    bool AddWinningMasternode(CMasternodePaymentWinner& winner);
    bool ProcessBlock(int nBlockHeight);
    void Relay(CMasternodePaymentWinner& winner);
    void Sync(CNode* node);
    void CleanPaymentList();
    int LastPayment(CMasterNode& mn);

    //slow
    bool GetBlockPayee(int nBlockHeight, CScript& payee);
    */
}
