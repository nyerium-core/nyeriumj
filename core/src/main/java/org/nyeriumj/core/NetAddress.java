package org.nyeriumj.core;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetAddress extends ChildMessage {
    static final int MESSAGE_SIZE = 16;
    private static final long serialVersionUID = 7501293709324197411L;
    private InetAddress addr;

    public NetAddress(NetworkParameters params, byte[] payload, int offset, int protocolVersion) throws ProtocolException {
        super(params, payload, offset, protocolVersion);
    }

    public NetAddress(NetworkParameters params, byte[] payload, int offset, int protocolVersion, Message parent, MessageSerializer serializer) throws ProtocolException {
        super(params, payload, offset, protocolVersion, parent, serializer, Integer.MIN_VALUE);
    }

    public NetAddress(InetAddress addr) {
        this.addr = (InetAddress) Preconditions.checkNotNull(addr);
        this.length = 16;
    }

    public static NetAddress localhost(NetworkParameters params) {
        return new NetAddress(InetAddresses.forString("127.0.0.1"));
    }

    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        byte[] ipBytes = this.addr.getAddress();
        if (ipBytes.length == 4) {
            byte[] v6addr = new byte[16];
            System.arraycopy(ipBytes, 0, v6addr, 12, 4);
            v6addr[10] = (byte) -1;
            v6addr[11] = (byte) -1;
            ipBytes = v6addr;
        }
        stream.write(ipBytes);
    }

    protected void parse() throws ProtocolException {
        try {
            this.addr = InetAddress.getByAddress(readBytes(16));
            this.length = 16;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public InetAddress getAddr() {
        return this.addr;
    }

    public void setAddr(InetAddress addr) {
        unCache();
        this.addr = addr;
    }

    public String toString() {
        String address = this.addr.getHostAddress();
        return address.contains(".") ? address : "[" + address + "]";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ((NetAddress) o).addr.equals(this.addr);
    }

    public int hashCode() {
        return this.addr.hashCode();
    }
}
