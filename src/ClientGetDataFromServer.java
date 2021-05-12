import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.swing.JTextPane;

public class ClientGetDataFromServer implements Runnable {
    private Socket socket;
    private JTextPane taDiscussion, taConnectes;
    private ObjectInputStream ois;
    private String username;

    /**
     * Constructeur de la class ClientGetDataFromServer
     * 
     * @param socket        le socket du client
     * @param uneDiscussion le lien vers la discussion de l'application
     * @param taConnectes   le lien vers la liste des connecte de l'application
     * @param username      le nom d'utilisateur de la personne
     */
    public ClientGetDataFromServer(Socket socket, JTextPane uneDiscussion, JTextPane taConnectes, String username) {
        this.socket = socket;
        this.taDiscussion = uneDiscussion;
        this.taConnectes = taConnectes;
        this.username = username;
    }

    /**
     * Override de la methode run qui rend la class Runnable
     */
    @Override
    public void run() {

        while (!socket.isClosed()) {
            try {
                System.out.print("Message recu");
                ois = new ObjectInputStream(socket.getInputStream());
                DataTransmission dataReception = (DataTransmission) ois.readObject();
                // Mise a jour de la list utilisateur
                if (dataReception.getIsNewUserList() == true) {
                    actualiserConnecter(dataReception.getUserListToString());
                }
                // Je me deconnecte si j'en et recu le message
                if (dataReception.getIsDeconnetionReq() == true) {
                    System.out.println("je me ferme officlement");
                    socket.close();
                    return;
                }
                // Reception d'un message prive/public
                if (dataReception.getIsPrivateMessage() == true || dataReception.getIsPublicMessage() == true) {
                    actualiserDiscussion(dataReception);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Message recu qui ne nous est surement pas destiné \\PB RESEAU\\ " + e);
            } catch (Exception e) {
                try {
                    socket.close();
                    System.out.println("Mon socket = " + socket.isClosed());
                    return;
                } catch (IOException e1) {
                    System.out.println("ERROR CLOSE Mon socket = " + socket.isClosed());
                    e1.printStackTrace();
                }
                return;
            }
        }
    }

    /**
     * Methode qui permet d'actualiser la discussion 
     * dans mon application
     * @param dt dataTransmission et la donnee recu par le client venant du serveur
     */
    private void actualiserDiscussion(DataTransmission dt){
        // Mise a jour des couleurs des chats en fonction de la methode d'envoi/reception du message
        String maDiscussion = taDiscussion.getText().replace("</body>\n</html>", "");
        maDiscussion += dt.getMessageWithHTMLFormat(username);
        taDiscussion.setText(maDiscussion); // mettre a jour la discussion
        taDiscussion.setCaretPosition(taDiscussion.getDocument().getLength() ); // met a jours la position de la scrollView
    }

    /**
     * Methode qui permet d'actualiser la liste 
     * des connectees mon application
     * @param listeConnecte une liste deja bien formee
     */
    private void actualiserConnecter(String listeConnecte){
        taConnectes.setText(listeConnecte);
    }
     
    
}
