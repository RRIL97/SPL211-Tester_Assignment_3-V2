package bgu.spl.net.impl.BGRSServer.TesterOld;

import java.nio.ByteBuffer;

public class NoParamPacket extends Packet{

    private byte [] opCode;

    public NoParamPacket(byte [] opCode){
        this.opCode = opCode;
    }

    ByteBuffer getPacketBytes(){
        ByteBuffer packetBytes = ByteBuffer.allocate(2);
        for (byte b : opCode) packetBytes.put(b);
        return packetBytes;
    }
}
