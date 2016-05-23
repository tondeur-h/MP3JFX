package mp3jfx;

import javafx.scene.control.ProgressBar;
import javafx.scene.media.AudioSpectrumListener;

/********************
 *
 * @author tondeur-h
 ********************/
public class spectrum implements AudioSpectrumListener{

    ProgressBar prog;
    ProgressBar prog1;
    
    //recuperer le progressBar de la fenêtre
    public spectrum(ProgressBar p,ProgressBar p1) {
    prog=p;
    prog1=p1;
    }
    
    
    /****************************************************************
     * overide method qui permet de recuperer les valeurs du spectre
     * @param d
     * @param d1
     * @param floats
     * @param floats1 
     ****************************************************************/
    @Override
    public void spectrumDataUpdate(double d, double d1, float[] floats, float[] floats1) {
        //affecter le progressBar   
        prog.setProgress(1.0-((floats[0]*-1.0)/60));
        prog1.setProgress(1.0-((floats[1]*-1.0)/60));
    }
    
}
