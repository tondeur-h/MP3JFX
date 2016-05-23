package mp3jfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


/********************
 *
 * @author tondeur-h
 ********************/
public class FXMLmp3Controller implements Initializable {

    @FXML
    private ImageView btnClose;
    @FXML
    private ImageView btnOpen;
    @FXML
    private ImageView btnPlay;
    @FXML
    private Label fxNomMus;
    @FXML
    private ImageView btnStop;
    @FXML
    private Slider lsdVolume;
    @FXML
    private ImageView btnBack;
    @FXML
    private ImageView btnFor;
    @FXML
    private Label lbAction;
    @FXML
    private Slider lsdBalance;
    @FXML
    private Label lbTimer;
    @FXML
    private ImageView imgBack;
    @FXML
    private ProgressBar progMus;
    @FXML
    private ImageView btnNext;
    @FXML
    private ImageView btnSave;
    @FXML
    private ProgressBar progMus1;
    @FXML
    private ImageView btnEqual;


    Duration d; //durée de l'ecoute
    ScheduledService<Integer> serv; //scheduler
    FileChooser mp3File; //dialgue de recherche d'un mp3
    File fichier; //un fichier
    List<File> fichiers; //liste des fichiers a traiter
    ArrayList<File> playList; //liste des fichiers a lire
    MediaPlayer player; //le player
    Media media; //un fichier mp3 a jouer
    long totalTime=0; //temp total du morceau
    int numPiste=0; //numero piste en cours
    String nomFichier; //nom du fichier en cours
    File fileSave;
    String defaultMusicPath="C:/Users/tondeur-h.CHV/Music";
    Stage stage;
    Stage stageEQ;
    double f1,f2,f3,f4,f5,f6,f7,f8,f9,f10;
    boolean enableEQ=true;
    @FXML
    private ImageView BtnMasquer;


    /*****************************
     * passe le stage au controler
     * @param stg
     *****************************/
    public void setStage(Stage stg){
        stage=stg;
        lire_properties();
        lire_equalizer();
    }


    /**************************************
     * appelé au lencement de l'application
     * @param url
     * @param rb
     **************************************/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       //prémare une playlist vide
        playList=new ArrayList<>();

        //prépare le dialogue de recherche de fichier MP3
        mp3File = new FileChooser();
        mp3File.setTitle("Fichiers musique MP3");
        mp3File.setInitialDirectory(new File(defaultMusicPath));

        //ajuster le volume au max
        lsdVolume.setValue(10.0);

        //ajouter un scheduler pour l'affichage du temps de lecture
        serv = new ScheduledService<Integer>() {

            @Override
            protected Task<Integer> createTask() {
                return new Task() {
                    //creer une tache qui va lire le temps ecoulé
                    //cette tâche n'effecte que la variable d globale a l'application
                    @Override
                    protected Object call() throws Exception {
                        d=player.getCurrentTime();
                        return null;
                    }
                };
            }
        };
        //ajuster l'appel du scheduler toutes les secondes
        serv.setPeriod(Duration.seconds(1.0));
        //cet evenement est appelé lorsque le sheduler a fini sa tache de lecture
        //ceci permet de mettre en place un gestionnaire d'evenement
        //qui sera appelé a chaque fin de tache du scheduler
        serv.setOnSucceeded((final WorkerStateEvent workerStateEvent) -> {
        processTimer(); //et execute l'affichage du temps
        });
        //lancer le service scheduler
        serv.start();

        //drag and drop
         fxNomMus.setOnDragOver((DragEvent event) -> {
             Dragboard db = event.getDragboard();
             if (db.hasFiles()) {
                 event.acceptTransferModes(TransferMode.LINK);
             } else {
                 event.consume();
             }
        });

    }


    /*****************************
     * clic sur le bouton "close"
     * @param event
     *****************************/
    @FXML
    private void hClose(MouseEvent event) {
    //quitter la plateforme
         ecrire_properties();
         System.exit(0);
        //Platform.exit();
    }


    /****************************
     * clic sur le bouton "open"
     * @param event
     ****************************/
    @FXML
    private void hbtnOpen(MouseEvent event) {
        //filtrer sur les fichiers mp3 uniquement
        mp3File.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers mp3,m3u", "*.mp3;*.m3u"));
        //ouvrir le dialogue openFile
        //si le dossier de lecture n'existe plus
        File defaulDIR=new File(defaultMusicPath);
        //alors prendre celui de l'application par defaut
        if (!defaulDIR.exists()) defaulDIR=new File(System.getProperty("user.dir"));
         mp3File.setInitialDirectory(defaulDIR);
        fichiers=mp3File.showOpenMultipleDialog(((Node)event.getTarget()).getScene().getWindow());

        //si des fichiers ont été choisi
            if (fichiers.size()>0){
            //vider l'ancienne playlist
                playList.clear();

           //ecrire dernier properties
            defaultMusicPath=fichiers.get(0).getPath().substring(0, (fichiers.get(0).getPath().length()-fichiers.get(0).getName().length()));
            ecrire_properties();
            //remplacer les fichiers m3u par le contenu
            replaceM3U();

            //ouvrir le média numero 0 et le lire
            numPiste=-1;
            next();
        }
    }


    /***************************
     * clic sur le bouton "play"
     * @param event
     ***************************/
    @FXML
    private void hbtnPlay(MouseEvent event) {
        //si lecteur est en mode stop alors appeler next
        if (player==null){next();return;}

        //si un morceau est déja en lecture alors mettre en pause et inversement
        if (player.getStatus()==MediaPlayer.Status.PLAYING){
            player.pause();
            lbAction.setText("PAUSE");
            return;
        }

        //jouer le morceau
        player.play();
        //afficher l'action
        lbAction.setText("PLAY");
            //utiliser une seule bande du spectre
            player.setAudioSpectrumNumBands(2);
            //echantillonner toutes les 0.1 secondes
            player.setAudioSpectrumInterval(0.1);
            //creer l'appel au listener AudioSpectrumListener
               player.setAudioSpectrumListener(new spectrum(progMus,progMus1));
               //recuperer le temps total du morceau pour l'affichage
                //totalTime=(long)media.getDuration().toSeconds();
                //et afficher cette valeur
                processTimer();
    }


    /***************************
     * clic sur le bouton "stop"
     * @param event
     ***************************/
    @FXML
    private void hbtnStop(MouseEvent event) {
        //arreter la musique
        player.stop();
        //afficher l'evenement
        lbAction.setText("STOP");
    }


    /***********************************************
     * modification du volume sonore avec le curseur
     * @param event
     ***********************************************/
    @FXML
    private void hsldVolume(MouseEvent event) {
        player.setVolume(lsdVolume.getValue() / 10);
    }


    /********************************
     * clic sur le bouton "backward"
     * @param event
     ********************************/
    @FXML
    private void hbtnBack(MouseEvent event) {
        //reculer de 5 secondes par clic
        player.seek(player.getCurrentTime().subtract(Duration.seconds(5.0)));
    }


    /*******************************
     * clic sur le bouton "forward"
     * @param event
     *******************************/
    @FXML
    private void hbtnFor(MouseEvent event) {
        //avancer de 5 secondes
        player.seek(player.getCurrentTime().add(Duration.seconds(5.0)));
    }


    /*****************************
     * modification de la balance
     * centre = stereo
     * haut = right
     * bas = left
     * @param event
     *****************************/
    @FXML
    private void hsldBalance(MouseEvent event) {
        //affecter la balance
        player.setBalance(lsdBalance.getValue() - 1.0);
    }


    /**********************************************************
     * est appeler par la tâche planifié pour afficher le temps
     **********************************************************/
    public void processTimer() {
        //recuperer valeur entiere du temp ecoulé
        long secondes=Math.round(d.toSeconds());
        //recuperer le temps total du morceau pour l'affichage
        totalTime=(long)media.getDuration().toSeconds();
        //afficher ce temp
        lbTimer.setText(timeConversion(secondes)+"/"+timeConversion(totalTime));
        System.gc();
    }


    /*************************************
     * ajouter zero si valeur sur 1 digit
     * @param valeur
     * @return
     *************************************/
    public String completeZero(long valeur){
     String re=Integer.toString((int)valeur);
     if (re.length()<2){
         return "0"+re;
     }
        return re;
    }


    /**************************************************************
     * convertir un nombre de secondes en "heures:minutes:secondes"
     * @param totalSeconds
     * @return
     **************************************************************/
    public String timeConversion(long totalSeconds) {

    final int MINUTES_IN_AN_HOUR = 60;
    final int SECONDS_IN_A_MINUTE = 60;

    long seconds = totalSeconds % SECONDS_IN_A_MINUTE;
    long totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
    long minutes = totalMinutes % MINUTES_IN_AN_HOUR;
    long hours = totalMinutes / MINUTES_IN_AN_HOUR;

    return completeZero(minutes) + ":" + completeZero(seconds);
}


    /****************************
     * passer au morceau suivant
     ****************************/
    public void next(){
        numPiste++;
        try{
        player.stop();
        } catch (Exception e){}
        if (numPiste==playList.size()){numPiste=0;}
            //ouvrir le média suivant
            nomFichier=manageFile(playList.get(numPiste).getPath());

            System.out.println(numPiste+" : "+nomFichier);
            //si erreur d'ouverture du fichier passer au suivant...
            try {media = new Media(nomFichier);}catch(MediaException me){next();}

            //afficher le nom du fichier
            fxNomMus.setText("["+(numPiste+1)+"] "+playList.get(numPiste).getName());
                    //creer l'objet player permet d'obtenir des infos sur le média
                    player = new MediaPlayer(media);
                    //appliquer les valeurs de l'equalizer
                    player.getAudioEqualizer().setEnabled(enableEQ);
                    player.getAudioEqualizer().getBands().get(0).setGain(f9);
                    player.getAudioEqualizer().getBands().get(1).setGain(f10);
                    player.getAudioEqualizer().getBands().get(2).setGain(f1);
                    player.getAudioEqualizer().getBands().get(3).setGain(f2);
                    player.getAudioEqualizer().getBands().get(4).setGain(f3);
                    player.getAudioEqualizer().getBands().get(5).setGain(f4);
                    player.getAudioEqualizer().getBands().get(6).setGain(f5);
                    player.getAudioEqualizer().getBands().get(7).setGain(f6);
                    player.getAudioEqualizer().getBands().get(8).setGain(f7);
                    player.getAudioEqualizer().getBands().get(9).setGain(f8);

                    //mettre le player en mode stop a la fin de la lecture
                    player.setOnEndOfMedia(()->next());
                    //si erreur de lecture
                    player.setOnError(()->lbAction.setText("ERROR"));
                    //afficher STOP
                    lbAction.setText("STOP");
                    //remettre les compteurs a zéro et les afficher
                    d=Duration.ZERO;
                    totalTime=0;
                    processTimer();
                    hbtnPlay(null);
        }


  /****************************************
   * convertir chaine en url file://xxxxx
   * sauf si cela est deja une url
   * @param source
   * @return
   ***************************************/
  public String manageFile(String source){
      String retour;
      //lire prefixe
      String prefixe = source.substring(0,4);
      //si prefixe est file ou http ou https alors sortir car ne rien changer
      if (prefixe.compareToIgnoreCase("http")==0 || prefixe.compareToIgnoreCase("file")==0)
      {
          source=source.replaceAll(" ", "%20");
          source=source.replaceAll("\\\\", "/");
          return source;
      }
      //si autre c'est surement du local alors transformer en url
      retour="file:///" + source.replaceAll("\\\\", "/");
      retour=retour.replaceAll(" ", "%20");
      return retour;
  }


    /****************************
     * appeler le morceau suivant
     * @param event
     ****************************/
    @FXML
    private void hbtnNext(MouseEvent event) {
        next();
    }


    /******************************************
     * sauvegarder la playlist sous format m3u
     * @param event
     ******************************************/
    @FXML
    private void hbtnSave(MouseEvent event) {
    //demander le nom du fichier
        FileChooser saveDlg=new FileChooser();
        saveDlg.setTitle("Fichiers playlist m3u");
        saveDlg.setInitialDirectory(new File(defaultMusicPath));
        saveDlg.getExtensionFilters().add(new FileChooser.ExtensionFilter("fichier M3U", "*.m3u"));

        fileSave=saveDlg.showSaveDialog(((Node)event.getTarget()).getScene().getWindow());
        if (fileSave!=null){
            try{
                //ecrire les données
                PrintWriter pw=new PrintWriter(fileSave);
                playList.stream().forEach((f) -> {
                    pw.println(f.getPath());
                });
                pw.close();
            }catch(FileNotFoundException fne){}
        }
    }


    /**********************************************
     * remplacer les fichiers m3u par leur contenu
     **********************************************/
    public void replaceM3U(){
        //pour chaque fichier controler si c'est un m3u
        String extension="";
        int index;
        int compteur=0;
        for (File f:fichiers){
            //lire l'extension
            if ((index=f.getName().lastIndexOf('.')) > 0) {extension = f.getName().substring(index+1);}
            //si extension est mp3 ajouter simplement
            if (extension.compareToIgnoreCase("mp3")==0){
                playList.add(f);
            }
            //si extension est m3u
            if (extension.compareToIgnoreCase("m3u")==0){
                //alors le traiter
                BufferedReader fr=null;
                try {
                    //ouvrir le fichier
                    fr = new BufferedReader(new FileReader (f));
                    while (fr.ready()){
                        //lire la ligne et traiter celle ci
                        expandm3u(fr.readLine());
                    }
                    //fermer le fichier
                    fr.close();
                    //
                } catch (IOException ioe) {
                } finally {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                    }
                }
            }
            compteur++;
        }
    }


    /**************************************
     * traiter chaque lignes du fichier m3u
     * @param line
     **************************************/
    public void expandm3u(String line){
        //pas de traitement recursif pour l'instant.
        //on ne traite pas les m3u inclus
        //on ne traite pas les sous...sous répertoires.

      //si c'est un http ou file, ne peut être tester et ajouter sans contrôle.
      String prefixe = line.substring(0,5);
      //si prefixe est file ou http alors ne rien changer
      if (prefixe.compareToIgnoreCase("http:")==0 || prefixe.compareToIgnoreCase("file:")==0){
      //Si c'est un https convertir en http (on fait un pari)
      if (prefixe.compareToIgnoreCase("https")==0){line=line.replaceFirst("https", "http");}
      playList.add(new File(line));
      return;
      }

        //Tester si c'est un fichier valide
        File f=new File(line);
       //Oui ajouter la ligne
       //Non abandonner
        if (f.isFile()){playList.add(new File(line));return;}

        //Tester si c'est un répertoire valide

        //Non abandonner
        if (f.isDirectory()){
                    //Oui rechercher les fichiers mp3 dans ce répertoire
                    File[] fs=f.listFiles((File pathname) -> pathname.getPath().endsWith(".mp3"));
                    for (File fi:fs){
                        playList.add(fi);
                    }
        }
    }



    /*******************************************************
     * ecrire le fichier parametre du dernier dossier ouvert
     *******************************************************/
    public void ecrire_properties(){
        try{
        Properties p=new Properties();
        p.setProperty("lastmusicpath", defaultMusicPath);
        p.setProperty("posx", Double.toString(stage.getX()));
        p.setProperty("posy",Double.toString(stage.getY()));

        p.storeToXML(new FileOutputStream("mp3jfx.msc"), "mp3jfx");
        } catch(IOException ioe){}
    }


    /*****************************************************
     * lire le fichier parametre du dernier dossier ouvert
     *****************************************************/
    public void lire_properties(){
        try{
        Properties p=new Properties();
        p.loadFromXML(new FileInputStream("mp3jfx.msc"));
        defaultMusicPath=p.getProperty("lastmusicpath", "c:/");
        String posx=p.getProperty("posx", "400.0");
        String posy=p.getProperty("posy", "200.0");
        stage.setX(Double.parseDouble(posx));
        stage.setY(Double.parseDouble(posy));
        } catch(Exception ioe){stage.centerOnScreen();}
    }

    /*****************************************************
     * lire le fichier equalizer pour ajuster a chaque player
     *****************************************************/
    public void lire_equalizer(){
        try{
        Properties p=new Properties();
        p.loadFromXML(new FileInputStream("equalizer.msc"));
        String F1=p.getProperty("f1", "0.0");
        String F2=p.getProperty("f2", "0.0");
        String F3=p.getProperty("f3", "0.0");
        String F4=p.getProperty("f4", "0.0");
        String F5=p.getProperty("f5", "0.0");
        String F6=p.getProperty("f6", "0.0");
        String F7=p.getProperty("f7", "0.0");
        String F8=p.getProperty("f8", "0.0");
        String F9=p.getProperty("f9", "0.0");
        String F10=p.getProperty("f10", "0.0");
        String Enable=p.getProperty("enable", "true");

        f1=Double.parseDouble(F1);
        f2=Double.parseDouble(F2);
        f3=Double.parseDouble(F3);
        f4=Double.parseDouble(F4);
        f5=Double.parseDouble(F5);
        f6=Double.parseDouble(F6);
        f7=Double.parseDouble(F7);
        f8=Double.parseDouble(F8);
        f9=Double.parseDouble(F9);
        f10=Double.parseDouble(F10);
        enableEQ=Boolean.valueOf(Enable);
        } catch(Exception ioe){stage.centerOnScreen();}
    }



    /**
     * drag and drop accept uniqument des mp3
     * @param event
     */
    @FXML
    private void hdragdropMusic(DragEvent event) {
    //draganddrop
        Dragboard db=event.getDragboard();
        if (db.hasFiles()){
            for (File fdd:db.getFiles()){
                if(fdd.getPath().endsWith(".mp3")){playList.add(fdd);}
            }
        }

    }


    /*********************************
     * afficher la playlist en cours
     * @param event
     *********************************/
    @FXML
    private void hbtnPlayList(MouseEvent event) {
        //ajouter une liste
        ObservableList<String> olv=javafx.collections.FXCollections.observableArrayList();
        olv.clear();
        ListView<String> lv=new ListView<>();
        lv.setItems(olv);
        lv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        //draganddrop pour la playlist
        lv.setOnDragOver(dragevent->{
            Dragboard db = dragevent.getDragboard();
             if (db.hasFiles()) {
                 dragevent.acceptTransferModes(TransferMode.LINK);
             } else {
                 event.consume();
             }
        });
        //set draganddrop
         //draganddrop
        lv.setOnDragDropped(draglv->{
        Dragboard db=draglv.getDragboard();
        if (db.hasFiles()){
            for (File fdd:db.getFiles()){
                if(fdd.getPath().endsWith(".mp3"))
                {playList.add(fdd);olv.add(fdd.getPath());}
            }
        }
        });

        //ajouter un HBox
        HBox hb=new HBox(10.0);

        //label
        Label lbar=new Label("Order playlist or drag and drop on me! ");
        hb.getChildren().add(lbar);

        //ajouter un bouton supprimer
        Button btnSupp=new Button("Supprimer");
        btnSupp.setTooltip(new Tooltip("Supprimer un morceau de la playList..."));
        btnSupp.setOnAction(eh->{
            int numsel=lv.getSelectionModel().getSelectedIndex();
            olv.remove(numsel);
            playList.remove(numsel);
                            }
        );
        hb.getChildren().add(btnSupp);

        //ajouter un bouton monter
        Button btnUP=new Button("Monter");
        btnUP.setTooltip(new Tooltip("Remonter le morceau vers le début"));
        btnUP.setOnAction(ehUP->{
            int numsel=lv.getSelectionModel().getSelectedIndex();
            if (numsel>0){
                String selsource=olv.get(numsel);
                String seldest=olv.get(numsel-1);
                olv.set(numsel, seldest);
                olv.set(numsel-1,selsource);

                File selFsource=playList.get(numsel);
                File selFdest=playList.get(numsel-1);
                playList.set(numsel, selFdest);
                playList.set(numsel-1, selFsource);
            }
        }
        );
        hb.getChildren().add(btnUP);

        //ajouter un bouton descendre
        Button btnDOWN=new Button("Descendre");
        btnDOWN.setTooltip(new Tooltip("Descendre le morceau vers le bas"));
        btnDOWN.setOnAction(ehDOWN->{
            int numsel=lv.getSelectionModel().getSelectedIndex();
            if (numsel<olv.size()){
                String selsource=olv.get(numsel);
                String seldest=olv.get(numsel+1);
                olv.set(numsel, seldest);
                olv.set(numsel+1,selsource);

                File selFsource=playList.get(numsel);
                File selFdest=playList.get(numsel+1);
                playList.set(numsel, selFdest);
                playList.set(numsel+1, selFsource);

            }

        }
        );
        hb.getChildren().add(btnDOWN);


        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("mp3jfx");
        alert.getDialogPane().setHeader(lbar);
        alert.setHeaderText("PLAYLIST");
        alert.setResizable(true);
        alert.getDialogPane().setExpandableContent(lv);
        alert.getDialogPane().setContent(hb);
        alert.getDialogPane().setExpanded(true);

        String affiche="";
        int val=1;
        for (File fdd:playList){
          //  affiche=affiche+"["+val+"] "+fdd.getPath()+"\n";
          olv.add("["+val+"] "+fdd.getPath());
            val++;
        }
        //alert.setContentText(affiche);
        alert.showAndWait();
    }


    /*************************
     * ouvrir l'equalizer...
     * @param event
     *************************/
    @FXML
    private void hBtnEqual(MouseEvent event) {
        try {
            //charger la fenêtre principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLmp3EQ.fxml"));
            stageEQ=new Stage(StageStyle.UTILITY);
            stageEQ.setResizable(false);
            stageEQ.setScene(new Scene(loader.load()));
            //recuperer le controleur
            final mp3EQ controller = loader.getController();
            //passer le stage au controleur
            controller.setStage(stageEQ,player);
            stageEQ.setTitle("mp3jfx - Equalizer");
            stageEQ.setAlwaysOnTop(true);
            stageEQ.showAndWait();

        } catch (IOException ex) {
            Logger.getLogger(FXMLmp3Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void hMasquer(MouseEvent event) {
        stage.hide();
    }

}