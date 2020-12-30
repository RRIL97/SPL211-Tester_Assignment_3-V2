package bgu.spl.net.impl.BGRSServer.TesterOld;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Client implements Runnable{

    private Socket             socket;
    private DataInputStream    socketReader;
    private DataOutputStream   socketWriter;

    private final  String  ip  ;
    private final  int     port;
    private final  int     threadId;

    private boolean isConnected = false;
    private boolean keepAlive = true;

    private byte[] readBytes = new byte[256];

    public Client(String ip, int port, int threadId){
      this.ip = ip;
      this.port = port;
      this.threadId = threadId;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void addToReadBuffer(int read, byte nextByte)
    {
        if(readBytes.length <= read)
            readBytes = Arrays.copyOf(readBytes,read * 2);
        readBytes[read] = nextByte;
    }

    <T extends Packet> void processSending(T packet){
       try {
           ByteBuffer buffer = packet.getPacketBytes();
           byte [] actualBytesToSend = buffer.array();

           socketWriter.write(actualBytesToSend, 0, actualBytesToSend.length);
           socketWriter.flush();
       }catch(Exception sendDataException){
            sendDataException.printStackTrace();
       }
    }
    private byte[] shrinkByteArray(byte [] input,int size){
        byte [] newByteArray = new byte[size];
        if (size >= 0) System.arraycopy(input, 0, newByteArray, 0, size);
        return newByteArray;
    }

    byte []  processRead(short commandId){
        try {

            boolean continueRead = false;
            if (commandId == 6 || commandId == 7 || commandId == 8 || commandId == 11)
                continueRead = true;

            for (int i = 0; i < 4 || continueRead; i++) {
                byte currentByte = socketReader.readByte();
                addToReadBuffer(i, currentByte);
                if(currentByte == 0x0 && i >= 4)
                    continueRead = false;
                if(currentByte == 13)
                    continueRead = false;
                if(!continueRead) {
                    readBytes = shrinkByteArray(readBytes, i + 1);
                }
            }
        }catch(Exception processReadException){
            processReadException.printStackTrace();
        }
        return readBytes;
    }

    public String getLastResponseAsStr(){
        byte [] bytes = readBytes;
        if(bytes.length <= 3)
        {
            short opCode;
            byte [] opCodeResponse = {bytes[0],bytes[1]};
            short   opCodeConvertedResponse  = bytesToShort(opCodeResponse);

            if(opCodeConvertedResponse == 12)
                return "SUCCESS";
            else return "FAILURE";
        }else
        {
            byte [] newBytes = new byte[readBytes.length];
            System.out.println("Cleaned Read Bytes | " + Arrays.toString(readBytes));
            for(int i = 4 ; i < newBytes.length; i++){
                newBytes[i] = readBytes[i-4];
            }
            return new String(newBytes);
        }
    }

    public boolean sendCommandAndValidateResponse(short commandId,String firstParamStr,String secondParamStr){
      byte [] opCode      = shortToBytes(commandId);
      switch(commandId)
      {
          case 1: //AdminReg
          case 2:
          case 3: //Login
              LoginPacket myLoginPacket = new LoginPacket(opCode,firstParamStr,secondParamStr);
              processSending(myLoginPacket);
              break;
          case 4:
              NoParamPacket noParamPacket = new NoParamPacket(opCode);
              processSending(noParamPacket);
              break;
          case 5: // Course Reg
          case 6: // KdamCheck
          case 7: // CourseStat (isAdmin)
          case 9: // IsRegistered
          case 10:
              oneFieldPacket OneFieldPacket = new oneFieldPacket(opCode,shortToBytes((short)Integer.parseInt(firstParamStr)));
              processSending(OneFieldPacket);
              break;
          case 8:
              break;
          case 11:
              break;
          default:
              break;
      }
      byte [] returnedBytesFromServer = processRead(commandId);
      byte [] opCodeResponse = {returnedBytesFromServer[0],returnedBytesFromServer[1]};
      short  opCodeConvertedResponse  = bytesToShort(opCodeResponse);
      return opCodeConvertedResponse == 12; //ACK
    }
    public void shutDown(){
        keepAlive = false;
    }

    public void waitForConnection(){
        try{
         while(!isConnected) //Wait till connected..
             Thread.sleep(50);
        }catch(Exception waitForConnectionException){
            waitForConnectionException.printStackTrace();
        }
    }
    @Override
    public void run(){
         try{
               socket       = new Socket(ip,port);
               socketReader = new DataInputStream (socket.getInputStream());
               socketWriter = new DataOutputStream(socket.getOutputStream());

               isConnected = true;
               while(keepAlive) //Don't let the thread die.
                   Thread.sleep(1000);
               socket.close();
         }catch(Exception connectorException){
              connectorException.printStackTrace();
         }
    }
}
