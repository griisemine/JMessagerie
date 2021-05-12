import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.*;
import javax.swing.event.*;

import java.util.regex.*;
import java.net.Socket;

class Connexion implements ActionListener, DocumentListener {

	private JButton bConnexion;
	private JTextField tfNom, tfIp, tfPort;
	private boolean isConnected = false;
	private Discussion eltDiscussion;
	private Socket socket;

	// Thread qui detecte la fermerture du serveur et nous deconnecte
	private Thread t = new Thread(new Runnable() {

		@Override
		public void run() {
			System.out.println("Ca demarre");
			while (socket.isClosed() == false) {
				System.out.println(socket.isClosed());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e){}
			}
			if( socket.isClosed() == true ){} // Attendre que le socket soit fermer
			debloquerInterface(); //mise a jours de l'interface
			eltDiscussion.bloquerInterface(); //mise a jour interface discussion
		}
	} );

	/**
	 * Constructeur de la class Conexxion Permet d'intialiser les elements de la
	 * class
	 */
	public Connexion(Discussion discussion) {

		this.eltDiscussion = discussion;

		bConnexion = new JButton("Connexion");
		bConnexion.setEnabled(false);
		bConnexion.addActionListener(this);
		
		tfNom = new JTextField();
		tfNom.getDocument().addDocumentListener( this );
		tfIp = new JTextField();
		tfIp.getDocument().addDocumentListener( this );
		tfPort = new JTextField();
		tfPort.getDocument().addDocumentListener( this );
	}
	
	/**
	 * Methode qui blocks 
	 * les bouton/champs de text
	 * de l'interface connexion
	 */
	private void bloquerInterface(){
		bConnexion.setText("Deconnexion");
		tfNom.setEnabled(false);
		tfIp.setEnabled(false);
		tfPort.setEnabled(false);
		isConnected = !isConnected;
	}

	/**
	 * Methode qui desblocks 
	 * les bouton/champs de text
	 * de l'interface connexion
	 */
	private void debloquerInterface(){
		bConnexion.setText("Connexion");
	//	tfNom.setText( tfNom.getText().replaceAll("#[0-9]{3}", "") );
		tfNom.setEnabled(true);
		tfIp.setEnabled(true);
		tfPort.setEnabled(true);
		isConnected = !isConnected;
		actualiserBoutton();
	}

	/**
	 * Methode qui permet de faire une demande de connexion
	 * avec les informations d'IP et de port specifier
	 * dans les champs prevu a cette effet
	 */
	public void seConnecter(){
		if(!isConnected){
			// Tentative de connexion avec les elements 
			// specifier dans les champs necessaire
			try {
				bloquerInterface(); // bloquer l'interface
				// Tenter d'initier un socket
				socket = new Socket(tfIp.getText() , Integer.parseInt(tfPort.getText()) );	
				// Envoi d'un objet demandant une nouvelle connexion
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject( (Object) new DataTransmission( tfNom.getText() ) );
				oos.flush();
				//Reception de l'autorisation du serveur pour ce connecter
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream() );
				DataTransmission dataReception =  (DataTransmission)ois.readObject();
				// Si la demande de connexion est accepter
				if( dataReception.getNewConnectionServerResponse() == true ){
					// demander a debloquer l'interface de discussion
					tfNom.setText( dataReception.getUsernameSender() );
					eltDiscussion.debloquerInterface( socket , dataReception.getUsernameSender() , dataReception.getUserListToString() );
					t.start();
				}

			} catch (IOException | ClassNotFoundException e) {
				// Si erreur debloquer interface de connexion
				System.out.println("Erreur de connexion " + e);
				debloquerInterface();
			} 
		}
	}

	/**
	 * Methode qui permet de se deconnecter d'un serveur
	 * distant si et seulement si on est connecter a un 
	 * serveur
	 */
	public void seDeconnecter(){
		if(isConnected && socket != null){
			try {
				// Envoi d'un objet demandant une nouvelle connexion
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject( (Object) new DataTransmission( tfNom.getText(), true ) );
				oos.flush();
				
				debloquerInterface(); //mise a jours de l'interface
				eltDiscussion.bloquerInterface(); //mise a jour interface discussion
			} catch (IOException e){
				System.out.println("Impossible de ce deconnecter");
			}

			
		}
	}

	/**
	 * Accesseur qui renvoie le
	 * bouton de connexion
	 * @return boutton de connexion
	 */
	public JButton getBoutton(){
		return bConnexion;
	}
	
	/**
	 * Accesseyr qui renvoie le textfield
	 * nom
	 * @return nom 
	 */
	public JTextField getTfNom(){
		return tfNom;
	}
	
	/**
	 * Accesseur qui renvoie le
	 * textfield IP
	 * @return ip 
	 */
	public JTextField getTfIp(){
		return tfIp;
	}
	
	/**
	 * Accesseur qui renvoie le 
	 * textfield port
	 * @return port
	 */
	public JTextField getTfPort(){
		return tfPort;
	}
	
	
	/**
	 * Fonction qui verifie
	 * que tous les parametres
	 * sont correctement
	 * entree dans les champs avant de 
	 * pouvoir des griser
	 * le bouton de connexion
	 */
	public void actualiserBoutton(){
		boolean bIP = Pattern.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", tfIp.getText() );
		try {
			int i = Integer.parseInt(tfPort.getText() );
			if ( bIP && i <= 65535 && i > 0 && ( (Pattern.matches("^[a-zA-Z]{3,16}$" , tfNom.getText() ) && !isConnected) || ( Pattern.matches("^[a-zA-Z]{3,16}#[0-9]{3}$" , tfNom.getText() )  && isConnected)  ) ){
				bConnexion.setEnabled(true);
			}
			else{
				bConnexion.setEnabled(false);
				System.out.println("Matcher");	
			}
		} catch (Exception e){
			bConnexion.setEnabled(false);
			return;
		}
	}

	
	/**
	 * Action a faire au moment ou
	 * on appui sur le bouton
	 * seConnecter/seDeconnecter
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(isConnected){
			System.out.println("Je suis connecter et je me deconnecte");
			seDeconnecter();
		}
		else{
			System.out.println("Je suis deconnecter et je me connecte");
			seConnecter();
		}
			
	}
	
	/**
	 * Action a faire lors d'une interaction
	 * avec les champs de text de connexion
	 */
	@Override
	public void insertUpdate(DocumentEvent e){
		actualiserBoutton();
	}
	
	/**
	 * Action a faire lors d'une interaction
	 * avec les champs de text de connexion
	 */
	@Override
	public void removeUpdate(DocumentEvent e){
		actualiserBoutton();
	}
	
	/**
	 * Action a faire lors d'une interaction
	 * avec les champs de text de connexion
	 */
	@Override
	public void changedUpdate(DocumentEvent e){
	}
}