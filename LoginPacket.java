package bgu.spl.net.impl.BGRSServer.TesterOld;


import java.nio.ByteBuffer;

public class LoginPacket extends Packet {

    private byte [] opCode;
    private String  username;
    private String  password;

    public LoginPacket(byte [] opCode,String username,String password){
        this.opCode = opCode;
        this.username = username;
        this.password = password;
    }

    ByteBuffer getPacketBytes(){
        byte [] usernameBytes = username.getBytes();
        byte [] passwordBytes = password.getBytes();

        int OpCodeLength = opCode.length;
        int usernameLengthInBytes = usernameBytes.length;
        int passwordLengthInBytes = passwordBytes.length;
        ByteBuffer packetBytes = ByteBuffer.allocate(OpCodeLength + usernameLengthInBytes + passwordLengthInBytes + 2);

        for (byte b : opCode) packetBytes.put(b);
        for (byte usernameByte : usernameBytes) packetBytes.put(usernameByte);
        packetBytes.put((byte)0);
        for (byte passwordByte : passwordBytes) packetBytes.put(passwordByte);
        packetBytes.put((byte)0);
        return packetBytes;
    }
}
