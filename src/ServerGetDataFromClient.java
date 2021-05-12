import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class ServerGetDataFromClient implements Runnable {
    private static HashMap<Socket,String> maTableSocketUser = new HashMap<Socket,String>();
    private Socket monSocket;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    private static int staticID = 1;
    private int usrID = staticID;

    public ServerGetDataFromClient(Socket socket){
        monSocket = socket;
        maTableSocketUser.put(socket, "Inconnu"); // On ne connait pas encore son nom;
        staticID += 1;
     }

    /**
     * Methode redefini des thread
     * cette fonction gere la reception et la retransmission
     * des messages
     */
     @Override
     public void run() {
        while( !monSocket.isClosed() ){
            try {
                ois = new ObjectInputStream(monSocket.getInputStream() );
                DataTransmission dataReception =  (DataTransmission)ois.readObject();
                
                // Demande de connexion au serveur
                if( dataReception.getIsNewConnectionReq() == true ){
                    // Verifier si un utilisateur est pas deja present dans la liste
                    dataReception.setUsernameWithId( String.format("%03d", usrID) );
                    boolean estPresent = usernameDansLaListe( dataReception.getUsernameSender() );
                    if( estPresent == true ){ // utilisateur dans la liste
                        dataReception.setNewConnectionServerResponse( false );
                    } else {
                        dataReception.setNewConnectionServerResponse( true );
                        maTableSocketUser.replace(monSocket, dataReception.getUsernameSender() );
                        dataReception.setIsNewUserList(true);
                        dataReception.setUserList( getUserList() );
                    }
                    // Envoyer la reponse
                    envoyerAToutLeMonde(dataReception);
                    envoyerAToutLeMonde( new DataTransmission( getUserList() , dataReception.getUsernameSender() , " nous a rejoint !" ));
                }
                // Demande de deconnexion du serveur
                if( dataReception.getIsDeconnetionReq() == true ){
                    // Renvoyer le message a l'utilisateur pour lui confirmer la deconnexion
                    oos = new ObjectOutputStream( monSocket.getOutputStream() );
                    oos.writeObject(dataReception);
                    oos.flush();
                    // Supprimer l'utilisateur de ma table
                    maTableSocketUser.remove(monSocket);
                    // Envoyer la mise a jour de la liste d'utilisateur
                    envoyerAToutLeMonde( new DataTransmission( getUserList() , dataReception.getUsernameSender() , " nous a quitté !" ));
                    return; // Quitter le serveur
                }

                // Envoi d'un message privee
                if( dataReception.getIsPrivateMessage() == true ){
                    envoyerAUnUtilisateur(dataReception);
                }

                // Envoi d'un message publique
                if( dataReception.getIsPublicMessage() == true ){
                    System.out.println("Message public recu : " + dataReception.getUsernameSender() + " Pour " +dataReception.getTo() );
                    envoyerAToutLeMonde(dataReception);
                }
                
            }catch ( ClassNotFoundException e ){
                System.out.println("THREAD ID = " + usrID + " " + e);
            } 
            catch( EOFException e){
                return;
            }
            catch(Exception  e) {
                System.out.println("ERR THREAD ID = " + usrID + " " + e);
                return;
            } 
        }
     }

    /**
     * Methode qui verifie si un nom d'utilsateur
     * existe deja dans la liste des utilsateurs ou non
     * @param username non d'utilisateur a verifier
     * @return vrai/faux
     */
    private boolean usernameDansLaListe(String username){
        for ( Map.Entry<Socket, String> entry : maTableSocketUser.entrySet() ) {
            Socket key = entry.getKey();
            if( maTableSocketUser.get(key).compareTo(username) == 0 ){
                System.out.println("ET DEJA DANS LA LIST : " + maTableSocketUser.get(key) + "  compare to " + username);
                return true;
            }
                
        }
         return false;
     }
    
    /**
    * Methode qui cree une list d'utilisateur et qui la renvoie 
    * sous la forme d'un tableau de string
    * @return
    */
    private String[] getUserList(){
        String[] userList = new String[ maTableSocketUser.size() ];
        int i = 0;
        for ( Map.Entry<Socket, String> entry : maTableSocketUser.entrySet() ) {
            Socket key = entry.getKey();
            userList[i++] = maTableSocketUser.get(key);
        }
        return userList;
    }

    /**
     * Methode qui permet d'envoyer un message a tous le monde
     * @param dt
     */
    private void envoyerAToutLeMonde(DataTransmission dt){
        for ( Map.Entry<Socket, String> entry : maTableSocketUser.entrySet() ) {
            Socket key = entry.getKey();
            try {
                System.out.println("Envoi du message à " + maTableSocketUser.get(key) + " mon socket = " + key);
                oos = new ObjectOutputStream(key.getOutputStream());
                oos.writeObject(dt);
                oos.flush();
            } catch (IOException e) {
                // Connexion perdu avec un membre du serveur ?
                System.out.println("Impossible d'envoi le message "+ e);
            }
        }
    }

    /**
     * Methode qui envoi un message a un utilisateur specifique
     * @param dt donner a transmettre
     */
    private void envoyerAUnUtilisateur(DataTransmission dt){
        try {
            // me transmettre le message
            oos = new ObjectOutputStream(monSocket.getOutputStream());
            oos.writeObject(dt);
            oos.flush();
            boolean userFind = false;
            // Transmettre le message a la cible
            for ( Map.Entry<Socket, String> entry : maTableSocketUser.entrySet() ) {
                Socket key = entry.getKey();
                if( maTableSocketUser.get(key).compareTo( dt.getTo() ) == 0 ){
                    userFind = true;
                    oos = new ObjectOutputStream(key.getOutputStream());
                    oos.writeObject(dt);
                    oos.flush();
                    return;
                }
            }
            if(userFind == false ){
                oos = new ObjectOutputStream(monSocket.getOutputStream());
                oos.writeObject( new DataTransmission( dt.getUsernameSender() , dt.getTo(), 1));
                oos.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}

