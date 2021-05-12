import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

public class Server {

    private int port;
    private ServerSocket monServeur = null;
    private ServerLaunch sl;


    /**
     * Constructeur du serveur 
     * permet de cree et lancer le serveur
     * @param port port de connexion
     * @param host ADDR du serveur
     */
    public Server(int port){
        this.port = port;
        etablirConnexion();
        lanceToi();
    }
    
    /**
     * Methode qui retourne vrai ou faux 
     * si le serveur est lancer ou non
     * @return true/false
     */
    public boolean isCloseed(){
        return monServeur.isClosed();
    }

    /**
     * Methode qui retourne l'IP du server
     * @return SocketAddress
     */
    public SocketAddress getInetAddress(){
        return monServeur.getLocalSocketAddress();
    }

    /**
     * Methode qui retourne le port du serveur
     * @return Integer
     */
    public Integer getPort(){
        return monServeur.getLocalPort();
    }

    /**
     * Permet de faire une tentative de creation
     * sur la base des informations disponible du 
     * port et addr
     */
    private void etablirConnexion(){
        try {
            monServeur = new ServerSocket(port,100, InetAddress.getLocalHost() );
        } catch (IOException e) {
            System.out.println("Impossible de cree le server " + e);
        }
    }

    /**
     * Methode priver qui ce lance en meme temps que le constructeur
     * cette methode permet de demander au serveur de lancer son thread
     * de serveur
     */
    private void lanceToi(){

        if(monServeur.isClosed()== true ){
            System.out.println("Impossible de lancer le serveur");
            return;
        }
        Thread t = new Thread( sl = new ServerLaunch(monServeur) );
        t.start();
    }

    /**
     * Methode qui ferme le serveur
     * et deconnecte les sockets
     * @throws IOException
     */
    public void close() {
        try {
            monServeur.close();
        } catch (IOException e1) {
            System.out.print("Impossible de fermer le serveur");
        }
        List<Socket> ls = sl.getMesClients();
        for(int i = 0 ; i < ls.size() ; i++){
            try {
                ls.get(i).close();
            } catch (IOException e) {
                System.out.print("Impossible de fermer ce socket");
            }
        }
            
    }

}

class ServerLaunch implements Runnable {

    private ServerSocket monServeur = null;
    private List<Socket> mesClients;

    public ServerLaunch(ServerSocket unServeur){
        monServeur = unServeur;
        mesClients = new LinkedList<Socket>();
    }

    public List<Socket> getMesClients(){
        return mesClients;
    }

    /**
     * Methode qui override runnable
     * pour lancer le serveur sur ca
     * demande de connexion entrante
     */
    @Override
    public void run() {

        while(monServeur.isClosed() == false){
            try {
                //On attend une connexion d'un client
                Socket client = monServeur.accept();
                //Une fois reçue, on la traite dans un thread séparé
                Thread t = new Thread( new ServerGetDataFromClient(client) );
                mesClients.add(client);
                t.start();
                
             } catch (IOException e) {
                e.printStackTrace();
             }
        }

    }
    
}