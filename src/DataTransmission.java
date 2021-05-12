import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransmission implements Serializable {
    private static final long serialVersionUID = 1L;

    private String from,to,message,hm; // hm is heure/minute of send requeste
    /**
     * Varibale specifique au methode
     * qui permet de comprendre que c'est
     * une requette de connexion d'un
     * nouvelle utilisateur
     */
    private boolean isNewConnectionReq = false;
    private boolean newConnectionServerResponse = false;

    /**
     * Variable specifique au methode qui 
     * permette d'envoyer une nouvelle
     * list d'utilisateur
     */
    private String[] userList;
    private boolean isNewUserList = false;
    
    /**
     * Variable specifique au methode 
     * qui permette a utilisateur de 
     * ce deconnecter du serveur
     */
    private boolean isDeconnectionReq = false;

    /**
     * Variable specfique au methode
     * d'envoi de message elle permet de 
     * specifier si c'est un message privee
     * ou non
     */
    private boolean isPrivateMessage = false;
    private boolean isPublicMessage = false;
    private boolean isErrorMessage = false;

    /**
     * Constructeur demande de nouvelle
     * connexion au server
     * @param username
     */
    public DataTransmission(String username){
        this.from = username;
        this.isNewConnectionReq = true;
        this.hm = recupererHeureMinute();
    }

    /**
     * Constructeur qui permet a un utilisateur de ce
     * deconnecter du serveur
     * @param username
     * @param isDeconnectionReq
     */
    public DataTransmission(String username, boolean isDeconnectionReq){
        this.from = username;
        this.isDeconnectionReq = isDeconnectionReq;
        this.hm = recupererHeureMinute();
    }

    /**
     * constructeur qui permet d'envoyer un message
     * privee ou public 
     * @param username utilisateur qui envoi le message
     * @param message message
     */
    public DataTransmission(String username, String message){
        // remplir par defaut comme un messsage forAll
        this.from = username;
        this.to = "a";
        this.message = message;
        this.hm = recupererHeureMinute();
        this.isPublicMessage = true;
        // verifier si il y'a un identifcateur en debut de ligne
        if( Pattern.matches("^ *@[a-zA-Z]{3,}#[0-9][0-9][0-9] (\n|.)*", message ) ){
            // Recuperer l'identifcateur
            Matcher m = Pattern.compile("@[a-zA-Z]{3,}#[0-9][0-9][0-9]").matcher( message );
            if( m.find() ){
                this.to = m.group(0).replace("@", "");
                this.message = message.replace(m.group(0), "" );
                this.isPrivateMessage = true;
                this.isPublicMessage = false;
            }
        }
        this.message = this.message.replace("\n", "<br>"); // replacer les saut a la ligne shell par des saut a la ligne html
    }

    /**
     * Constructeur qui permet de cree un message venant 
     * du server et a destination d'un seul utilisateur
     * afin de le prevenir que son message n'a pas pu
     * etre envoyer
     * @param from l'envoyeur du message precedent
     * @param to le destinataire introuvable
     * @param errCode le code d'erreur
     */
    public DataTransmission(String from, String to, int errCode){
        this.isErrorMessage = true;
        this.isPrivateMessage = true;
        this.from = "s";
        this.to = from;
        this.hm = recupererHeureMinute();
        this.message = "Le message n'a pas pu être envoyé. " + to + " est introuvable";
    }

    /**
     * Message du serveur qui met a jours la list d'utilisateur
     * @param userList
     * @param userDisconnectName
     */
    public DataTransmission(String[] userList , String userDisconnectName , String message){
        this.from = "s";
        this.to = "a";
        this.hm = recupererHeureMinute();
        this.message = userDisconnectName + message;
        this.userList = userList;
        isNewUserList = true;
        isPublicMessage = true;
    }
    
    /**
     * Methode qui renvoi vrai/faux
     * si il s'agit d'un requete de
     * message privee
     * @return vrai/faux
     */
    public boolean getIsPrivateMessage(){
        return isPrivateMessage;
    }

    /**
     * Methode qui renvoi vrai/faux
     * si il s'agit d'un requete de
     * message privee
     * @return vrai/faux
     */
    public boolean getIsPublicMessage(){
        return isPublicMessage;
    }
    
    /**
     * Methode qui prend le nom d'utilisateur
     * de l'appelant et qui le formattat le message
     * dans le format souhaiter pour l'affichage au format 
     * HTML
     * @param username
     * @return
     */
    public String getMessageWithHTMLFormat(String username){

        //Convertir emoji
        message = convertirEnojiEnImage(message);

        // initialisation des couleurs en fonction du type de message
        String couleurEnvoi = "#5BC236";
        String couleurReceception = "#EBEBEB";
        String couleurServerEnvoi = "black";
        // Si c'est un message privee on change les couleurs
        if ( isPrivateMessage == true) {
            couleurEnvoi = "#5fc9f8";
            couleurReceception = "#147efb";
            message = "@" + to + " " + message; // former le message avec la cible si on est l'envoyeur
        }
        if( isErrorMessage == true ){
            couleurServerEnvoi = "red";
        }

        String msgHTML = "";

        // Si c'est un message serveur
        if( from.compareTo("s") == 0){
            msgHTML += "<div style=' padding: 5px 10px 5px 12px;text-align: center; margin: 0.5%; border-radius: 10px;'>"
			+ "<a> (" + hm + ") </a><strong>Serveur Info</strong><a style='color: " + couleurServerEnvoi + ";'' > : " + message + "</a>"  
		    + "</div>";
        } else if( username.compareTo(from) == 0 ){ // Verifier si on est le responsable du message
            msgHTML += "<div style='background-color: " + couleurEnvoi 
            + "; padding: 5px 10px 5px 12px; margin: 10px; text-align: right;'><a> " + message
            + " : </a> <strong style=color:'"+ createRGB(from) + ";'>Moi</strong> <a> ( " + hm + " )</a><strong> </div>"; // ajouter le  message et son style
        } else {
            msgHTML += "<div style='background-color: " + couleurReceception
            + "; padding: 5px 10px 5px 12px; margin: 10px; '><a> ( " + hm + " )</a><strong style=color:'"+ createRGB(from) + ";'>"
            + from + "</strong><a> : " + message + "</a></div>"; // ajouter le message et son style
        }

        System.out.println(msgHTML);

        return msgHTML + "</body></html>";
    }

    public static String createRGB(String s){
		// valeur de l'addition de tout les char du string
		int decaleur = 0;
		// Creation d'un tab de char pour manipuler le string
		char[] stc = new char[ s.length() ];
		// Copie de String dans c[] et calcul de la valeur de la chaine
		for(int i = 0 ; i < s.length() ; i++){
			decaleur += s.charAt(i); 
			stc[i] = s.charAt(i);
		}
		
		// intervertir les elements
		for(int i = 0 ; i < s.length() ; i++){
			char tmp = stc[ (i+decaleur) % s.length() ];
			stc[ (i+decaleur) % s.length() ] = stc[i];
			stc[i] = tmp;
		}
		s = new String(stc); // convert char to string
		
		// initalisation des codes rgb
		// mes nombre premier
		int[] monRGB = {0,0,0};
		int[] mesNbPremier = {43,53,73};
		// Parcours de la chaine en modifant les valeurs
		for(int i = s.length() -  ( s.length()/3 * 3 )  , j = 0   ; i < s.length() ; i++){
			if( i == s.length()/3 * 2 || i ==  s.length()/3 * 3 ) // passer a l'indice de nombre premier suivant
				j++;
			monRGB[j] += (char)s.charAt(i)*mesNbPremier[j];
            // melanger le tableau de int 
		    for(int k = 0 ; k < 3 ; k++){
			    int tmp = monRGB[ (k+decaleur) % 3 ];
			    monRGB[ (k+decaleur) % 3 ] = monRGB[k];
			    monRGB[k] = tmp;
	    	}
		}
		
		return String.format("rgb(%d,%d,%d)", monRGB[0]%255,monRGB[1]%150,monRGB[2]%70);
	}
	

    public void setUsernameWithId(String id){
        this.from = this.from + "#" + id;
    }

    /**
     * Methode qui renvoi le destinataire
     * du message
     * @return String du destinataire
     */
    public String getTo(){
        return to;
    }

    /**
     * Methode qui renvoi vrai ou faux
     * si il s'agit d'une requette de
     * deconnexion
     * @return vrai/faux
     */
    public boolean getIsDeconnetionReq(){
        return isDeconnectionReq;
    }


    /**
     * Recuperer le nom d'utilisateur 
     * de la personne qui transmet la requette
     * @return String du nom d'utilisateur
     */
    public String getUsernameSender(){
        return from;
    }

    /**
     * Methode qui permet de dire si la requete
     * est une requete de nouvelle connexion
     * @return vrai/faux
     */
    public boolean getIsNewConnectionReq(){
        return isNewConnectionReq;
    }

    /**
     * Methode qui permet au serveur de completer la requete
     * avant de la renvoyer au proprietaire
     * @param b vrai/faux si accepter ou non
     */
    public void setNewConnectionServerResponse(boolean b){
         newConnectionServerResponse = b;
    }
    
    /**
     * Methode qui permet de savoir si la connexion est accepter
     * par le serveur ou non 
     * @return vrai/faux
     */
    public boolean getNewConnectionServerResponse(){
        return newConnectionServerResponse;
    }

    /**
     * Methode qui rempli la list des utilisateurs
     * dans la methode suivante
     * @param userList
     */
    public void setUserList(String[] userList){
        this.userList = userList;
    }

    /**
     * Methode qui retourne la liste 
     * d'utilisateur present dans la liste
     * @return
     */
    public String[] getUserList(){
        return userList;
    }

    public String getUserListToString(){
        String userListToString = "<html><body>";
        for(int i = 0; i < userList.length ; i++)
            userListToString += String.format( "<strong style='color:%s;'>%s</strong><br>" , createRGB(userList[i]) , userList[i]  );
        userListToString += "</body></html>";
        return userListToString;
    }
    
    /**
     * Methode qui permet de savoir si
     * la tram comprends une nouvelle
     * list d'utilisateur
     * @return
     */
    public boolean getIsNewUserList(){
        return isNewUserList;
    }

    /**
     * Methode qui permet de specifier si 
     * on vas renvoyer une nouvelle list
     * d'utilisateur
     * @param b un boolean vrai/faux
     */
    public void setIsNewUserList(boolean b){
        isNewUserList = b;
    }

    /**
     * Fonction qui permet de recuperer l'heure
     * au moment de son appel
     * @return String au format HH:MM
     */
    private String recupererHeureMinute(){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime( new Date() );
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        return String.format("%02d:%02d", h,m);
     }

     /**
      * Methode qui est appeler pour convertir
      * les smyler present dans un message en 
      * photo qui seront traduis en HTML
      * @param message le message en question
      * @return retourne le message avec les balises html des smiler
      */
     private String convertirEnojiEnImage(String message){
        message = message.replace(" :) ", "<img src='https://i.ibb.co/K5J4vr2/Sans-titre-2.png'>");
        return message;
     }
}
