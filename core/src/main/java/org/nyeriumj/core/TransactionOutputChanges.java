/*
 * Copyright 2012 Matt Corallo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nyeriumj.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.nyeriumj.script.ScriptOpCodes;

/**
 * <p>TransactionOutputChanges represents a delta to the set of unspent outputs. It used as a return value for
 * {@link AbstractBlockChain#connectTransactions(int, Block)}. It contains the full list of transaction outputs created
 * and spent in a block. It DOES contain outputs created that were spent later in the block, as those are needed for
 * BIP30 (no duplicate txid creation if the previous one was not fully spent prior to this block) verification.</p>
 */
public class TransactionOutputChanges {
    public final List<UTXO> txOutsCreated;
    public final List<UTXO> txOutsSpent;

    public TransactionOutputChanges(List<UTXO> txOutsCreated, List<UTXO> txOutsSpent) {
        this.txOutsCreated = txOutsCreated;
        this.txOutsSpent = txOutsSpent;
    }

    public TransactionOutputChanges(InputStream in) throws IOException {
        int numOutsCreated = (in.read() & ScriptOpCodes.OP_INVALIDOPCODE) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 8) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 16) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 24);
        txOutsCreated = new LinkedList<UTXO>();
        for (int i = 0; i < numOutsCreated; i++)
            txOutsCreated.add(new UTXO(in));

        int numOutsSpent = (in.read() & ScriptOpCodes.OP_INVALIDOPCODE) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 8) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 16) |
                ((in.read() & ScriptOpCodes.OP_INVALIDOPCODE) << 24);
        txOutsSpent = new LinkedList<UTXO>();
        for (int i = 0; i < numOutsSpent; i++)
            txOutsSpent.add(new UTXO(in));
    }

    public void serializeToStream(OutputStream bos) throws IOException {
        int numOutsCreated = this.txOutsCreated.size();
        bos.write(numOutsCreated & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsCreated >> 8) & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsCreated >> 16) & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsCreated >> 24) & ScriptOpCodes.OP_INVALIDOPCODE);
        for (UTXO output : this.txOutsCreated) {
            output.serializeToStream(bos);
        }

        int numOutsSpent = this.txOutsSpent.size();
        bos.write(numOutsSpent & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsSpent >> 8) & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsSpent >> 16) & ScriptOpCodes.OP_INVALIDOPCODE);
        bos.write((numOutsSpent >> 24) & ScriptOpCodes.OP_INVALIDOPCODE);
        for (UTXO output2 : this.txOutsSpent) {
            output2.serializeToStream(bos);
        }
    }
}
