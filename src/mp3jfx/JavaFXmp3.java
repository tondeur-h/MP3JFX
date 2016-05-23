package mp3jfx;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;

public class JavaFXmp3 extends Application {

    private double initX;
    private double initY;
    private boolean firstTime;
    private TrayIcon trayIcon;

    /********************
     *
     * @param stage
     * @throws Exception
     ********************/
    @Override
    public void start(Stage stage) throws Exception {

        createTrayIcon(stage);
        firstTime = true;
        Platform.setImplicitExit(false);

        //charger la fenêtre principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLmp3.fxml"));
        //récuperer le root
        Parent root = (Parent) loader.load();
        //recuperer le controleur
        final FXMLmp3Controller controller = loader.getController();

         //passer le stage au controleur
        controller.setStage(stage);

        //rendre transparent l'AnchorPane
        root.setStyle("-fx-background-color:transparent");

        //conserver le point de départ du drag and drop
        root.setOnMousePressed((MouseEvent me) -> {
            initX = me.getScreenX() - stage.getX();
            initY = me.getScreenY() - stage.getY();
        });


        //Déplacer la fenêtre
        root.setOnMouseDragged((MouseEvent me) -> {
            stage.setX(me.getScreenX()-initX);
            stage.setY(me.getScreenY()-initY);
        });

        //rendre transparent la scene
        Scene scene = new Scene(root, Color.TRANSPARENT);

        stage.setScene(scene);

//rentre transparent le Stage
        stage.initStyle(StageStyle.TRANSPARENT);
       //stage.centerOnScreen();
       stage.setAlwaysOnTop(true);
        stage.show();

        //masquer la fenêtre au démarrage
        hide(stage);
    }

    /**
     * for packaging
     * @param args
     */
    public static void main(String[] args) {
        Application.launch(args);
    }


    /**************************************
     * Inserer la fenêtre dans le systray
     * @param stage
     **************************************/
        public void createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            java.awt.Image image = null;
            try {
                image=ImageIO.read(this.getClass().getResourceAsStream("imp16.png"));
                //image = ImageIO.read(url);
            } catch (IOException ex) {
                System.out.println(ex);
            }


            stage.setOnCloseRequest((WindowEvent t) -> {
                hide(stage);
            });
            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = (java.awt.event.ActionEvent e) -> {
                System.exit(0);
            };

            ActionListener showListener = (java.awt.event.ActionEvent e) -> {
                Platform.runLater(() -> {
                    stage.show();
                });
            };

            ActionListener hideListener = (java.awt.event.ActionEvent e) -> {
                Platform.runLater(() -> {
                    stage.hide();
                });
            };

            // create a popup menu
            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Afficher");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem hideItem = new MenuItem("Masquer");
            hideItem.addActionListener(hideListener);
            popup.add(hideItem);

            MenuItem separator=new MenuItem("-");
            popup.add(separator);

            MenuItem closeItem = new MenuItem("Quitter");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "MP3Jfx V1.0", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
            // ...
        }
    }


    /*****************************
     * Message si premier masquage
     *****************************/
    public void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("MP3Jfx Version 1.0","Copyright (C) Tondeur Hervé 2016\nLicence GPL V3",TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }


    /********************************
     * Masquer la fenêtre principale
     * @param stage
     *********************************/
    public void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                //stage.hide();
                showProgramIsMinimizedMsg();
            } else {
                System.exit(0);
            }
        });
    }



}