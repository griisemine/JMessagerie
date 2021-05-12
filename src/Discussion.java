import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.swing.*;
import java.net.Socket;

public class Discussion implements ActionListener {
    private JButton nEnvoyer;
    private JTextPane taConnectes, taDiscussion;
    private JTextArea taMessage;
    private String username;
    private Socket socket;
    private Thread refreshDiscussion;

    /**
     * Constructeur du systeme de discussion
     * cette class gere les message et les
     * connexion.
     */
    public Discussion() {

        nEnvoyer = new JButton("Envoyer");
        nEnvoyer.addActionListener(this);
        nEnvoyer.setEnabled(false);

        taConnectes = new JTextPane();
        taConnectes.setContentType("text/html");
        taConnectes.setEditable(false);

        taDiscussion = new JTextPane();
        taDiscussion.setContentType("text/html");
        taDiscussion.setText("<html><body style=' color: white;'></body></html>");

        taDiscussion.setEditable(false);

        taMessage = new JTextArea();
        taMessage.setEnabled(false);
        taMessage.setRows(5);
    }

    /**
     * Accesseur permettant de renvoyez le bouton d'envoie
     * 
     * @return
     */
    public JButton getEnvoyer() {
        return nEnvoyer;
    }

    /**
     * Accesseur permettant de renvoyez le textfield des connectes
     * 
     * @return
     */
    public JTextPane getConnectes() {
        return taConnectes;
    }

    /**
     * Accesseur permettant de renvoyez le textfield des discussion
     * 
     * @return
     */
    public JTextPane getDiscussion() {
        return taDiscussion;
    }

    /**
     * Accesseur permettant de renvoyez le textfield des message
     * 
     * @return
     */
    public JTextArea getMessage() {
        return taMessage;
    }

         /**
      * Debloquer les elements de discussion de l'interface
      * et cree un thread qui vas rafraichir les discussion
      * @param socket le socket de ca connexion
      * @param nom le nom de l'utilisateur choisi
      */
      public void debloquerInterface(Socket socket, String username, String userList) {
        this.username = username;
        this.socket = socket;
        nEnvoyer.setEnabled(true);
        taMessage.setEnabled(true);
        taConnectes.setText( userList );
        refreshDiscussion = new Thread(new ClientGetDataFromServer(socket,taDiscussion,taConnectes, username) );

        refreshDiscussion.start();
    }

    /**
     * Bloquer les elements de discussion de l'interface
     * ce charge egalement de fermer le thrad
     */
    public void bloquerInterface() {
        System.out.println("Fermer la discussion");
        nEnvoyer.setEnabled(false);
        taMessage.setEnabled(false);
        refreshDiscussion.interrupt();
        taConnectes.setText("");
        refreshDiscussion.interrupt();
    }

    public void envoyerMessage(){
        /**
         * Tentative d'envoie du message au serveur
         */
        try {
            ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
            oos.writeObject( new DataTransmission(username, taMessage.getText() ) );  // ecriture de la requete
            oos.flush();
            taMessage.setText("");
        } catch (IOException e) {
            taMessage.setText("staut = " + socket.isClosed() );
        }
    }

    /**
     * Action a realiser lors de l'action du
     * bouton envoyer le message
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if( taMessage.getText().length() > 0 ){
            envoyerMessage();
        }
    }
    

}
