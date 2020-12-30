package bgu.spl.net.impl.BGRSServer.TesterOld;
import  java.util.ArrayList;
import  java.util.HashMap;
import  java.util.concurrent.ConcurrentHashMap;
import  java.util.concurrent.CountDownLatch;
import  java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler {
    private final int               numOfClients;
    private final ArrayList<Client> connectedClients = new ArrayList<>();

    private final String ip;
    private final int    port;

    public static final HashMap<String,Integer> commands = new HashMap<>();
    public static final HashMap<String,Integer> numParamsInCommand = new HashMap<>();

    public ClientHandler(String ip, int port ,int numOfClients){
    this.numOfClients = numOfClients;
    this.ip           = ip;
    this.port         = port;

    commands.put("ADMINREG",1);
    numParamsInCommand.put("ADMINREG",2);
    commands.put("STUDENTREG",2);
    numParamsInCommand.put("STUDENTREG",2);
    commands.put("LOGIN",3);
    numParamsInCommand.put("LOGIN",2);
    commands.put("LOGOUT",4);
    numParamsInCommand.put("LOGOUT",0);
    commands.put("COURSEREG",5);
    numParamsInCommand.put("COURSEREG",1);
    commands.put("KDAMCHECK",6);
    numParamsInCommand.put("KDAMCHECK",1);
    commands.put("COURSESTAT",7);
    numParamsInCommand.put("COURSESTAT",1);
    commands.put("STUDENTSTAT",8);
    numParamsInCommand.put("STUDENTSTAT",1);
    commands.put("ISREGISTERED",9);
    numParamsInCommand.put("ISREGISTERED",1);
    commands.put("UNREGISTER",10);
    numParamsInCommand.put("UNREGISTER",1);
    commands.put("MYCOURSES",11);
    numParamsInCommand.put("MYCOURSES",0);
    }

    public void kill(){
        for(Client c : connectedClients)
            c.shutDown();
    }

    public ConcurrentHashMap<Client,Boolean> processSpecificCommandsOnClients(String command,String firstParam, String secondParam, int extraParam) {
        ConcurrentHashMap<Client,Boolean> commandsStatuses = new ConcurrentHashMap<>();
        try {
            CountDownLatch waitFinishedProcessing = new CountDownLatch(connectedClients.size());
            for (Client c : connectedClients) {
                new Thread(() -> {
                    synchronized (c) {
                            commandsStatuses.put(c, c.sendCommandAndValidateResponse((short) (commands.get(command).intValue()), firstParam, secondParam));
                            waitFinishedProcessing.countDown();
                    }
                }
                ).start();
            }
            waitFinishedProcessing.await();
        }catch(Exception e){
            e.printStackTrace();
        }
        return commandsStatuses;
    }


    public boolean processSpecificCommandOnClient(Client c, String command,String firstParam, String secondParam) {
      AtomicBoolean commandSuccess = new AtomicBoolean(false);
        try {
            CountDownLatch commandFinished = new CountDownLatch(1);
                new Thread(() -> {
                    synchronized (c) {
                        commandSuccess.set(c.sendCommandAndValidateResponse((short) (commands.get(command).intValue()), firstParam, secondParam));
                        commandFinished.countDown();
                    }
                }
                ).start();
         commandFinished.await();
        }catch(Exception e){
            e.printStackTrace();
        }
        return commandSuccess.get();
    }

    public ArrayList<Client> getClients(){
        return connectedClients;
    }
    public void initiateClients(){
        try{
            for(int i = 0 ;i < numOfClients; i++)
            {
                Client workerClient = new Client(ip,port,i);
                Thread workerThread = new Thread(workerClient);
                connectedClients.add(workerClient);
                workerThread.start();
                workerClient.waitForConnection(); //Blocking.. Wait For All To Connect.
            }
            System.out.println("Created "+numOfClients+" Clients..");
        }catch(Exception clientHandlerException){
            clientHandlerException.printStackTrace();
        }
    }
}
