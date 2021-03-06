

import java.io.BufferedReader;
import java.io.IOException; 
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;


public class Client implements Runnable {

	enum Dir {
		NORD("N"), SUD("S"), EST("E"), OUEST("O"), JUMP_NORD("JN"), JUMP_SUD("JS"), JUMP_EST("JE"), JUMP_OUEST(
				"JO"), NORD2("N"), SUD2("S"), EST2("E"), OUEST2("O"), NORD3("N"), SUD3("S"), EST3("E"), OUEST3("O");
		String code;
		Dir(String dir) {
			code = dir;
		}
	}
	
	private String ipServer;
	private long teamId;
	private String secret;
	private int socketNumber;
	private long gameId;

	Random rand = new Random();

	public static Player currentPlayer;
	public static ArrayList<Player> ListePlayers=new ArrayList<Player>();
	public static ArrayList<Logos> ListeLogos=new ArrayList<Logos>();
	public static int compteurGlobal=0;

	public Client(String ipServer, long teamId, String secret, int socketNumber, long gameId) {
		this.ipServer = ipServer;
		this.teamId = teamId;
		this.secret = secret;
		this.socketNumber = socketNumber;
		this.gameId = gameId;
	}

	public void run() {
		System.out.println("Demarrage du client");
		Socket socket = null;
		String message;
		BufferedReader in;
		PrintWriter out;
		try {
			socket = new Socket(ipServer, socketNumber);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			System.out.println("Envoi de l'incription");
			out.println(secret + "%%inscription::" + gameId + ";" + teamId);
			out.flush();

			do {
				message = in.readLine();
				System.out.println("Message recu : " + message);
				if (message != null) {
					if (message.equalsIgnoreCase("Inscription OK")) {
						System.out.println("Je me suis bien inscrit a la battle");
					} else if (message.startsWith("worldstate::")) {
						String[] components = message.substring("worldstate::".length()).split(";", -1);
						int round = Integer.parseInt(components[0]);
						
			/*********************** zone de modif***************************/
						double startTime = (double) System.currentTimeMillis();  //start temps d'execution
						// On joue
						if(compteurGlobal<6){
							System.out.println("a qui le tour: "+compteurGlobal);
						}else{
							compteurGlobal=0;
							System.out.println("a qui le tour: "+compteurGlobal);
						}
						updateWorld(components); //mise a jour du monde
						System.out.println("currentPlayer:"+currentPlayer.getNomPlayer()+"nbJump: "+currentPlayer.getNbJump());
//						Requete d'action
						String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
								+ moteurInference().code;
//						String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
//								+ computeDirection().code;
						System.out.println(action);
						out.println(action);
						//ajout du currentPlayer a sa place
						ListePlayers.add(compteurGlobal,currentPlayer);
						
						double finTime = (double) System.currentTimeMillis(); 		//fin temps d'execution
				        double totalTime = (double) ((finTime - startTime) / 1000); //calcul temps d'execution
						
				        System.out.println("temps d'execution: "+totalTime +"s");
						compteurGlobal++;
//							if(round==1){ // le round commence a 1...
//								initWorld(components); //construction du monde
//								String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
//										+ moteurInference().code;
//								System.out.println(action);
//								out.println(action);
//							}else{
//								updateWorld(components); //mise a jour du monde
//								String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
//										+ moteurInference().code;
//								System.out.println(action);
//								out.println(action);
//						}
						
//						String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
//								+ computeDirection().code;
//						String action = secret + "%%action::" + teamId + ";" + gameId + ";" + round + ";"
//						+ trouverLogoProche().code;
//						System.out.println(action);
//						out.println(action);
		/*********************** fin zone de modif***************************/			
						out.flush();
					} else if (message.equalsIgnoreCase("Inscription KO")) {
						System.out.println("inscription KO");
					} else if (message.equalsIgnoreCase("game over")) {
						System.out.println("game over");
						System.exit(0);
					} else if (message.equalsIgnoreCase("action OK")) {
						System.out.println("Action bien pris en compte");
					}
				}
				System.out.println("Pour voir la partie : http://" + ipServer + ":8080/?gameId=" + gameId);
			} while (message != null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// Tant pis
				}
			}

		}
	}
	
	public static void setHit() {
		int posX = currentPlayer.getPositionX();
		int posY = currentPlayer.getPositionY();
		String hitID = null; 
		int nbHit = 0;
		
		for(int i=0;i<ListePlayers.size();i++){
			Player hit = ListePlayers.get(i);
			for(int j = posX-1; j < posX+1; posX++) {
				for(int k = posY-1; k < posY+1; posY++) {
					if(hit.getPositionX() == j && hit.getPositionY() == k) {
						hitID = hit.getNomPlayer();
						nbHit++;
					}
				}
			}
		}
		
		if(nbHit == 1) {
			currentPlayer.setLastHit(hitID);
		}
	}

/*********************** modif libre***************************/	
//Fonction de construction du monde
	public void initWorld(String[] tab){
				
		//Creation des logos
		String listeLogos=tab[2];
		String[] logos=listeLogos.split(":");
		//suppression des logos
		//remplissage de la liste de logo
		for(int j=0;j<logos.length;j++){
			String[] attributs=logos[j].split(",");
			ListeLogos.add(new Logos("Logo_"+j,Integer.parseInt(attributs[0]),Integer.parseInt(attributs[1]),true));
		}
		//Decoupage de la chaine d'entree
		String listePlayer=tab[1];
		String[] player=listePlayer.split(":");
		String listeCaddies=tab[3];
		String[] caddies=listeCaddies.split(":");
		//creation des players
		for(int i=0;i<player.length;i++){
			String[] attributsP=player[i].split(",");
			String[] attributsC=caddies[i].split(",");
			ListePlayers.add(new Player(attributsP[0], //nom
					Integer.parseInt(attributsP[1]), //posX
					Integer.parseInt(attributsP[2]), //posY
					Integer.parseInt(attributsP[3]), //score
					attributsP[4], //etat
					Integer.parseInt(attributsC[1]), //caddiePosX
					Integer.parseInt(attributsC[2]), //caddiesPosY
					false, //a un logo
					null, //dernier tape
					0) //nbJump
				); 
		}
		//retirer l'element currentplayer de la listeplayer
		currentPlayer=ListePlayers.get(compteurGlobal);
		ListePlayers.remove(compteurGlobal);
	}
	
	//Fonction de mise a jour du monde
	public void updateWorld(String[] tab){
		//si pas initier, initialisation du monde
		if(ListePlayers.size()==0){
			System.out.println("Initialisation du monde");
			initWorld(tab);
		}else{
			//mise a jour du monde
			//Repartition des differentes listes		
			String listePlayer=tab[1];
			String[] player=listePlayer.split(":");
//			String listeCaddies=tab[3];
//			String[] caddies=listeCaddies.split(":");
			
			//Mise a jour des logos
			String listeLogos=tab[2];
			String[] logos=listeLogos.split(":");
			
			//creation des logos
			ListeLogos.clear();
			for(int j=0;j<logos.length;j++){
				String[] attributs=logos[j].split(",");
				ListeLogos.add(new Logos("Logo_"+j,Integer.parseInt(attributs[0]),Integer.parseInt(attributs[1]),true));
//				ListeLogos.get(j).setLogPositionX(Integer.parseInt(attributs[0]));
//				ListeLogos.get(j).setLogPositionY(Integer.parseInt(attributs[1]));
//				ListeLogos.get(j).setEstDispo(true);
			}
			
			//mise a jour des objets player
			for(int i=0;i<player.length;i++){
				
				String[] attributsP=player[i].split(",");  //attributs des players
				ListePlayers.get(i).setPositionX(Integer.parseInt(attributsP[1])); //posX
				ListePlayers.get(i).setPositionY(Integer.parseInt(attributsP[2])); //posY
				ListePlayers.get(i).setScrore(Integer.parseInt(attributsP[3])); //score
				ListePlayers.get(i).setEtat(attributsP[4]); //etat
				ListePlayers.get(i).setHasLogo(false);
				
				//Les joueurs portent-ils des logos?
				for(int j=0;j<ListeLogos.size();j++){
					if(ListeLogos.get(j).getLogPositionX()==ListePlayers.get(i).getPositionX() && ListeLogos.get(j).getLogPositionY()==ListePlayers.get(i).getPositionY()){
						ListePlayers.get(i).setHasLogo(true);
						ListeLogos.get(j).setEstDispo(false);
					}
					
					//Si le logo est sur un caddie alors il est pas dispo
					if(ListeLogos.get(j).getLogPositionX()==ListePlayers.get(i).getCaddiePosX() && ListeLogos.get(j).getLogPositionY()==ListePlayers.get(i).getCaddiePosY()){
						ListeLogos.get(j).setEstDispo(false);
//						ListeLogos.remove(j);
					}
				}
				//les joueurs ont-ils depose leur logo?
				if(ListePlayers.get(i).getHasLogo()){
					if(ListePlayers.get(i).getPositionX()==ListePlayers.get(i).getCaddiePosX() && ListePlayers.get(i).getPositionY()==ListePlayers.get(i).getCaddiePosY()){
						ListePlayers.get(i).setHasLogo(false);
					}
				}
//						System.out.println("ennemis"+ListePlayers.get(i).toString());
//						System.out.println("nom: "+ListePlayers.get(i).getNomPlayer()+"\tposX: "+ListePlayers.get(i).getPositionX()+"\tposY: "+ListePlayers.get(i).getPositionY()+"\tscore: "+ListePlayers.get(i).getScrore()+"\tetat: "+ListePlayers.get(i).getEtat());						
			}	
			currentPlayer=ListePlayers.get(compteurGlobal);
			ListePlayers.remove(compteurGlobal);
			}
//			System.out.println("player="+monPlayer.toString());
		//retirer l'element currentplayer de la listeplayer
			
	}
	
	public static Dir trouverLogoProche(){
		Dir reponse = null;
		int monPlayerPosX=currentPlayer.getPositionX();
		int monPlayerPosY=currentPlayer.getPositionY();
		ArrayList<Logos> listeLogosLibres = new ArrayList<Logos>();
//		System.out.println(logoPlusProche);
//		System.out.println("taille"+ListeLogos.size());
		for(int i=0;i<ListeLogos.size();i++){
			if(ListeLogos.get(i).isEstDispo()) {
				listeLogosLibres.add(ListeLogos.get(i));
			}
		}
		
		Logos logoPlusProche=listeLogosLibres.get(0);
		int distanceLogoPlusProche=Math.abs(monPlayerPosX-logoPlusProche.getLogPositionX())+Math.abs(monPlayerPosY-logoPlusProche.getLogPositionY());
		
		for(int i=0;i<listeLogosLibres.size();i++){
			int calcul=Math.abs(monPlayerPosX-listeLogosLibres.get(i).getLogPositionX())+Math.abs(monPlayerPosY-listeLogosLibres.get(i).getLogPositionY());
			System.out.println("dist="+calcul+ " dispo=" +listeLogosLibres.get(i).isEstDispo() + " position: " +listeLogosLibres.get(i).getLogPositionX()+","+listeLogosLibres.get(i).getLogPositionY());
			if(calcul<=distanceLogoPlusProche){
				logoPlusProche=ListeLogos.get(i);
				distanceLogoPlusProche=calcul;
			}
		}
		
		System.out.println("va vers le logo"+logoPlusProche.getIdLogo()+";"+logoPlusProche.getLogPositionX()+";"+logoPlusProche.getLogPositionY());
		if(monPlayerPosX<logoPlusProche.getLogPositionX()){
//			reponse=Dir.EST;
//			currentPlayer.setNbJump(currentPlayer.getNbJump()+1);
			reponse = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.EST, 0);
		}else if(monPlayerPosX>logoPlusProche.getLogPositionX()){
//			reponse=Dir.OUEST;
//			currentPlayer.setNbJump(currentPlayer.getNbJump()+1);
			reponse = checkFuturePosition(monPlayerPosX-1,monPlayerPosY, Dir.OUEST, 0);
		}else if(monPlayerPosX==logoPlusProche.getLogPositionX()){
			if(monPlayerPosY<logoPlusProche.getLogPositionY()){
//				reponse=Dir.SUD;
				reponse = checkFuturePosition(monPlayerPosX,monPlayerPosY+1, Dir.SUD, 0);
			}else if(monPlayerPosY>logoPlusProche.getLogPositionY()){
//				reponse=Dir.NORD;
				reponse = checkFuturePosition(monPlayerPosX,monPlayerPosY-1, Dir.NORD, 0);
			}
		}
//		boolean directionKO=true;
//		int i=0;
//		while(directionKO){
//			if(monPlayerPosX<logoPlusProche.getLogPositionX() && monPlayerPosX+1!=ListePlayers.get(i).getPositionX()){
//				reponse=Dir.EST;
//				directionKO=false;
//			}else if(monPlayerPosX>logoPlusProche.getLogPositionX() && monPlayerPosX-1!=ListePlayers.get(i).getPositionX()){
//				reponse=Dir.OUEST;
//				directionKO=false;
//			}else{
//				if(monPlayerPosY<logoPlusProche.getLogPositionY() && monPlayerPosY+1!=ListePlayers.get(i).getPositionY()){
//					reponse=Dir.SUD;
//					directionKO=false;
//				}else if(monPlayerPosY>logoPlusProche.getLogPositionY() && monPlayerPosY-1!=ListePlayers.get(i).getPositionY()){
//					reponse=Dir.NORD;
//					directionKO=false;
//				}
//			}
//			i++;
//		}		
		
		return reponse;
	}
	
	private static Dir checkFuturePosition(int x, int y, Dir dir, int tour) {
		Dir dirDefinitif = null;
		Dir jumpDir = null;
		Dir inverseDir = null;
		Dir inverseDir2 = null;
		ArrayList<Dir> ListeJump = new ArrayList<Dir>();
		boolean ennemiFound = false;
		int i = 0;

		ListeJump.add(Dir.JUMP_EST);
		ListeJump.add(Dir.JUMP_OUEST);
		ListeJump.add(Dir.JUMP_NORD);
		ListeJump.add(Dir.JUMP_SUD);
		
		System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition tour " +tour+ " dir de base " +dir.code);
		if(dir == Dir.EST || dir == Dir.JUMP_EST) {
			jumpDir = Dir.JUMP_EST;
			inverseDir = Dir.NORD;
			inverseDir2 = Dir.SUD;
		}
		if(dir == Dir.OUEST || dir == Dir.JUMP_OUEST) {
			jumpDir = Dir.JUMP_OUEST;
			inverseDir = Dir.SUD;
			inverseDir2 = Dir.NORD;
		}
		if(dir == Dir.NORD || dir == Dir.JUMP_NORD) {
			jumpDir = Dir.JUMP_NORD;
			inverseDir = (currentPlayer.getHasLogo() ? Dir.OUEST : Dir.EST);
			inverseDir2 = (!currentPlayer.getHasLogo() ? Dir.EST : Dir.OUEST);
		}
		if(dir == Dir.SUD || dir == Dir.JUMP_SUD) {
			jumpDir = Dir.JUMP_SUD;
			inverseDir = (currentPlayer.getHasLogo() ? Dir.OUEST : Dir.EST);
			inverseDir2 = (!currentPlayer.getHasLogo() ? Dir.EST : Dir.OUEST);
		}
		
		while(ennemiFound == false && i < ListePlayers.size()) {
			int posX = ListePlayers.get(i).getPositionX();
			int posY = ListePlayers.get(i).getPositionY();
			if(posX == x && posY == y) {
				System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition L� ! UN ENNEMI !! ");
				System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition objectif : " +x+","+y+" ennemi " +ListePlayers.get(i).getPositionX() + "," +ListePlayers.get(i).getPositionY());
				
				
				//On est dans le cas simple : on tente le jumpdir
				if(jumpDir != null && currentPlayer.getNbJump() < 3) {
					System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition JUMPPPP !! ");
					switch(dir) {
						case EST:
							dirDefinitif = checkFuturePosition(x+1,y,jumpDir,0);
							break;
						case OUEST:
							dirDefinitif = checkFuturePosition(x-1,y,jumpDir,0);
							break;
						case SUD:
							dirDefinitif = checkFuturePosition(x,y+1,jumpDir,0);
							break;
						case NORD:
							dirDefinitif = checkFuturePosition(x,y-1,jumpDir,0);
							break;
						default:
							dirDefinitif = null;
					}
				}
				
				if(dirDefinitif == null) {
					System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition NO JUMP => ALTERNATIF tour " +tour+ " / dir " +dir.code);
					switch(tour) {
						case 0:
							dirDefinitif = checkFuturePosition(x+1,y,inverseDir,1);
							break;
							
						case 1:
							dirDefinitif = checkFuturePosition(x,y+1,inverseDir2,2);
							break;
						
						default:
							dirDefinitif = dir;
					}
				}
			} else {
				System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition GO GO GO!! tour " +tour+ " / dir " +dir.code);
				dirDefinitif = dir;
			}
			
			i++;
		}
		if(ListeJump.contains(dirDefinitif)) {
			currentPlayer.setNbJump(currentPlayer.getNbJump()+1);
		}

		System.out.println("joueur "+currentPlayer.getNomPlayer()+ " checkFuturePosition dir " +dirDefinitif.code);

		//setHit();
		
		return dirDefinitif;
	}

	public static Dir ramenerLogo(){
		Dir reponse = null;
		int monPlayerPosX=currentPlayer.getPositionX();
		int monPlayerPosY=currentPlayer.getPositionY();
		int monCaddiePosX=currentPlayer.getCaddiePosX();
		int monCaddiePosY=currentPlayer.getCaddiePosY();
		
		if(monPlayerPosY<monCaddiePosY){
//			reponse=Dir.SUD;
			reponse = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.SUD, 0);
		}else if(monPlayerPosY>monCaddiePosY){
//			reponse=Dir.NORD;
			reponse = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.NORD, 0);
		}else if(monPlayerPosY==monCaddiePosY){
			if(monPlayerPosX>monCaddiePosX){
//				reponse=Dir.OUEST;
				reponse = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.OUEST, 0);
			}else if(monPlayerPosX<monCaddiePosX){
//				reponse=Dir.EST;
				reponse = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.EST, 0);
			}
		}
		
//		boolean directionKO=true;
//		int i=0;
//		while(directionKO){
//			if(monPlayerPosY<monCaddiePosY && monPlayerPosY+1!=ListePlayers.get(i).getPositionX()){
//				reponse=Dir.SUD;
//				directionKO=false;
//			}else if(monPlayerPosY>monCaddiePosY && monPlayerPosY-1!=ListePlayers.get(i).getPositionY()){
//				reponse=Dir.NORD;
//				directionKO=false;
//			}else{
//				if(monPlayerPosX>monCaddiePosX && monPlayerPosX-1!=ListePlayers.get(i).getPositionX()){
//					reponse=Dir.OUEST;
//					directionKO=false;
//				}else if(monPlayerPosX<monCaddiePosX && monPlayerPosX+1!=ListePlayers.get(i).getPositionX()){
//					reponse=Dir.EST;
//					directionKO=false;
//				}
//			}
//			i++;
//		}
		return reponse;
	}
	
	public Dir attaque(){
		System.out.println(currentPlayer.getNomPlayer() + " ATTTTAAAAQQQUUUUEEE !");
		Dir direction=null;
		int monPlayerPosX=currentPlayer.getPositionX();
		int monPlayerPosY=currentPlayer.getPositionY();
		ArrayList<Player> ListePlayerLogos = new ArrayList<Player>();
		ArrayList<Player> ListePlayerATaper = new ArrayList<Player>();
		
		Player playerPlusProche=ListePlayers.get(0);
		int distanceplayerPlusProche=Math.abs(monPlayerPosX-playerPlusProche.getPositionX())+Math.abs(monPlayerPosY-playerPlusProche.getPositionY());
		
		for(int i=0;i<ListePlayers.size();i++){
			if(ListePlayers.get(i).getHasLogo()) {
				if(ListePlayers.get(i).getNomPlayer() != currentPlayer.getLastHit()) {
					ListePlayerLogos.add(ListePlayers.get(i));
				}
			}
		}
		
		if(ListePlayerLogos.size() > 0) {
			ListePlayerATaper = ListePlayerLogos;
		} else {
			ListePlayerATaper = ListePlayers;
		}
		
		for(int i=0;i<ListePlayerATaper.size();i++){
			int calcul=Math.abs(monPlayerPosX-ListePlayerATaper.get(i).getPositionX())+Math.abs(monPlayerPosY-ListePlayerATaper.get(i).getPositionY());
			if(calcul<=distanceplayerPlusProche){
				playerPlusProche=ListePlayerATaper.get(i);
				distanceplayerPlusProche=calcul;					
			}
		}
		System.out.println(playerPlusProche.getNomPlayer() + " dist="+distanceplayerPlusProche);
		
		if(monPlayerPosY<playerPlusProche.getPositionY()){
			direction = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.SUD, 0);
		}else if(monPlayerPosY>playerPlusProche.getPositionY()){
			direction = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.NORD, 0);
		}else if(monPlayerPosX<playerPlusProche.getPositionX()){
			direction = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.EST, 0);
//			currentPlayer.setNbJump(currentPlayer.getNbJump()+1);
		}else if(monPlayerPosX>playerPlusProche.getPositionX()){
			direction = checkFuturePosition(monPlayerPosX+1,monPlayerPosY, Dir.OUEST, 0);
//			currentPlayer.setNbJump(currentPlayer.getNbJump()+1);
		}
				
		return direction;
	}
	
	public Dir moteurInference(){
		Dir reponse = null;
		int i=0;
		boolean nbLogoLibre=false;
		while(nbLogoLibre==false && i < ListeLogos.size()){
			if(ListeLogos.get(i).isEstDispo()){
				nbLogoLibre =true;
			}else i++;
		}

		if(currentPlayer.getHasLogo()){
			System.out.println(currentPlayer.getNomPlayer() + " ram�ne le logo");
			reponse=ramenerLogo();
		} else if(nbLogoLibre == true){
			System.out.println(currentPlayer.getNomPlayer() + " cherche le logo");
			reponse=trouverLogoProche();
		} else {
			System.out.println(currentPlayer.getNomPlayer() + " attaque");
			reponse=attaque();
		}
		
		return reponse;
	}
	
	/******************code donne***************/
	public Dir computeDirection() {
		return Dir.values()[rand.nextInt(Dir.values().length)];
	}
}
