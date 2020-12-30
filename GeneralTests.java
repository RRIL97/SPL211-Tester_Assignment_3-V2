package bgu.spl.net.impl.BGRSServer.TesterOld;

import java.util.ArrayList;
import java.util.UUID;

public class GeneralTests implements  Runnable {

    private ArrayList<String>   registeredUsernames;
    private ArrayList<Client>   clients;
    private final ClientHandler cHandler;
    private final int           numOfThreads;



    public GeneralTests(ClientHandler cHandler,int numOfThreads) {
        this.cHandler     = cHandler;
        this.numOfThreads = numOfThreads;
        this.clients      = cHandler.getClients();

    }

    public ArrayList<String> registerAndLogin(int type){
        ArrayList<String> usersGenerated = new ArrayList<>();
        switch(type){

            case 0:
                for (Client c : clients) {
                    String generatedUsername = UUID.randomUUID().toString().substring(0,4);
                    cHandler.processSpecificCommandOnClient(c,"STUDENTREG", generatedUsername, generatedUsername);
                    cHandler.processSpecificCommandOnClient(c,"LOGIN", generatedUsername, generatedUsername);
                    usersGenerated.add(generatedUsername);
                }
                break;
            case 1:
                for (Client c : clients) {
                    String generatedUsername = UUID.randomUUID().toString().substring(0,4);
                    cHandler.processSpecificCommandOnClient(c,"ADMINREG", generatedUsername, generatedUsername);
                    cHandler.processSpecificCommandOnClient(c,"LOGIN", generatedUsername, generatedUsername);
                    usersGenerated.add(generatedUsername);
                }
                break;
            default: break;
        }
        return usersGenerated;
    }

    void testRegisterAndUnregisterCourse () throws InterruptedException {
            for (Client c : clients) {
                new Thread(() -> {
                    synchronized (c) {
                        boolean success;
                        success = cHandler.processSpecificCommandOnClient(c, "COURSEREG", "667", "");
                        if (success)
                            success = cHandler.processSpecificCommandOnClient(c, "UNREGISTER", "667", "");
                        if (!success)
                            System.out.println("Failed Multi Threaded Test!");
                    }
                }).start();
        }
     }
     @Override
        public void run () {
         try { 
             registeredUsernames = registerAndLogin(0);
             testRegisterAndUnregisterCourse();
             cHandler.kill();
         } catch (Exception generalTestException) {
             generalTestException.printStackTrace();
         }
     }
}
