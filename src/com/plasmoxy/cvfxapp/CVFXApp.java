package com.plasmoxy.cvfxapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * CVFXBase OpenCV Base in JavaFX
 *
 * @author Plasmoxy
 * @version 1.0 BETA
 * <p>
 * How launch  :
 * - set the fxml location ( in constructor )
 * - load opencv before launching
 * - call launch(args) on subclass of this
 */

public abstract class CVFXApp extends Application {
    
    // FIELDS -- FX --
    
    private URL fxmlLocation;
    
    protected FXMLLoader fxmlloader;
    protected Parent guiroot;
    protected Scene mainscene;
    protected CVFXController controller;
    
    // METHODS -- ACCESSORS --
    
    protected void setFxmlLocation(URL f) {
        fxmlLocation = f;
    }
    
    // METHODS -- FX --
    
    @Override
    public void start(Stage stg) throws IOException {
        
        if (fxmlLocation == null) throw new RuntimeException("ERROR : fxmlLocation resource is null !!!");
        
        fxmlloader = new FXMLLoader(fxmlLocation);
        
        log("Launching CVFXApp");
        
        guiroot = fxmlloader.load();
        mainscene = new Scene(guiroot);
        
        controller = fxmlloader.getController();
        
        stg.setScene(mainscene);
        stg.sizeToScene();
        stg.setTitle("Plasmoxy - CVFXBase");
        
        stg.setOnCloseRequest(event ->
        {
            log("Received closeController signal, calling closeController on controller");
            controller.closeController();
        });
        
        controller.initController(); // tell the controller to initialize itself now
        stg.show(); // render the stage
        
        // after rendering, fix the min size of it
        stg.setMinHeight(stg.getHeight());
        stg.setMinWidth(stg.getWidth());
        
        // maximize window
        stg.setMaximized(true);
        
        log("CVFXApp launched");
        
    }
    
    @Override
    public void stop() {
        log("CVFXApp stopped");
    }
    
    // METHODS -- Other --
    
    protected void log(String text) {
        System.out.println("[CVFXApp] " + text);
    }
}
