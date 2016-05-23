package mp3jfx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 *
 * @author tondeur-h
 */
public class mp3EQ implements Initializable {
    @FXML
    private Slider freq5;
    @FXML
    private Slider freq2;
    @FXML
    private Slider freq8;
    @FXML
    private Slider freq7;
    @FXML
    private Slider freq1;
    @FXML
    private Slider freq3;
    @FXML
    private Slider freq4;
    @FXML
    private Slider freq6;
    @FXML
    private Slider freq9;
    @FXML
    private Slider freq10;
    @FXML
    private Button btnOk;
    @FXML
    private Button btnCancel;
    @FXML
    private CheckBox CBenableEQ;

    
    Stage stage;
    AudioEqualizer aEQ; 
    
    //observable list de l'equalizer
    ObservableList<EqualizerBand> olEQ=javafx.collections.FXCollections.observableArrayList();
    
    
    public void setStage(Stage stg,MediaPlayer mp){
        //recuperer le stage
        stage=stg;
        //recuperer l'equalizer du player
        aEQ=mp.getAudioEqualizer();
        //recuperer les bandes
        olEQ=aEQ.getBands();
        //lire le fonctionnement de l'equalizer 
        CBenableEQ.setSelected(aEQ.isEnabled());
        //affecter les potentiometres avec  les valeurs de l'equalizer +12 à -24 db
        //125Hz
        freq1.setValue(olEQ.get(2).getGain());
                //250Hz
                freq2.setValue(olEQ.get(3).getGain());
                        //500Hz
                        freq3.setValue(olEQ.get(4).getGain());
                                //1Khz
                                freq4.setValue(olEQ.get(5).getGain());
                                        //2Khz
                                        freq5.setValue(olEQ.get(6).getGain());
                                                //4Khz
                                                freq6.setValue(olEQ.get(7).getGain());
                                                        //8Khz
                                                        freq7.setValue(olEQ.get(8).getGain());
                                                                //16Khz
                                                                freq8.setValue(olEQ.get(9).getGain());
                                                                //nb : freq9 & 10 sont les bandes 32 & 64 Hz
                                                                    freq9.setValue(olEQ.get(0).getGain());
                                                                        freq10.setValue(olEQ.get(1).getGain());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    //ne fait rien tout dans setStage
    }

    @FXML
    private void hBtnOK(ActionEvent event) {
        //fermer et enregistrer les valeurs 
        ecrire_properties();
        stage.close();
    }

    @FXML
    private void hBtnClose(ActionEvent event) {
        //fermer sans enregistrer
        stage.close();
    }

    @FXML
    private void hFreq8(MouseEvent event) {
        //ajuster frequence 16Khz
        olEQ.get(9).setGain(freq8.getValue());
    }

    @FXML
    private void hFreq5(MouseEvent event) {
        //ajuster frequence 2Khz
        olEQ.get(6).setGain(freq5.getValue());
    }

    @FXML
    private void hFreq2(MouseEvent event) {
        //ajuster frequence 250hz
        olEQ.get(3).setGain(freq2.getValue());
    }

    @FXML
    private void hFreq7(MouseEvent event) {
        //ajuster frequence 8Khz
        olEQ.get(8).setGain(freq7.getValue());
    }

    @FXML
    private void hFreq1(MouseEvent event) {
        //ajuster frequence 125hz
        olEQ.get(2).setGain(freq1.getValue());
    }

    @FXML
    private void hFreq3(MouseEvent event) {
        //ajuster frequence 500hz
        olEQ.get(4).setGain(freq3.getValue());
}

    @FXML
    private void hFreq4(MouseEvent event) {
        //ajuster frequence 1Khz
        olEQ.get(5).setGain(freq4.getValue());
}

    @FXML
    private void hFreq6(MouseEvent event) {
        //ajuster frequence 4Khz
        olEQ.get(7).setGain(freq6.getValue());
}
    
    /*******************************************************
     * ecrire le fichier parametre du dernier dossier ouvert
     *******************************************************/
    public void ecrire_properties(){
        try{
        Properties p=new Properties();
        p.setProperty("f1", Double.toString(freq1.getValue()));
        p.setProperty("f2", Double.toString(freq2.getValue()));
        p.setProperty("f3", Double.toString(freq3.getValue()));
        p.setProperty("f4", Double.toString(freq4.getValue()));
        p.setProperty("f5", Double.toString(freq5.getValue()));
        p.setProperty("f6", Double.toString(freq6.getValue()));
        p.setProperty("f7", Double.toString(freq7.getValue()));
        p.setProperty("f8", Double.toString(freq8.getValue()));
        p.setProperty("f9", Double.toString(freq9.getValue()));
        p.setProperty("f10", Double.toString(freq10.getValue()));
        p.setProperty("enable", Boolean.toString(CBenableEQ.isSelected()));
        p.storeToXML(new FileOutputStream("equalizer.msc"), "mp3jfx");
        } catch(IOException ioe){}
    }

    @FXML
    private void hFreq9(MouseEvent event) {
        //ajuster frequence 32hz
        olEQ.get(0).setGain(freq9.getValue());
    }

    @FXML
    private void hFreq10(MouseEvent event) {
        //ajuster frequence 64hz
        olEQ.get(1).setGain(freq10.getValue());
    }

    @FXML
    private void hCBEnableEQ(ActionEvent event) {
        //autoriser ou non l'equalizer
        aEQ.setEnabled(CBenableEQ.isSelected());
    }
    
}
