package com.plasmoxy.cvfxbase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Mat;

import java.io.IOException;
import java.net.URL;

/**
 * CVFXBase OpenCV Base in JavaFX
 *
 * @author Plasmoxy
 * @version 1.2
 *
 * How launch  :
 * - set the fxml location ( in constructor )
 * - load opencv before launching
 * - call launch(args) on subclass of this
 */

public abstract class CVFXApp extends Application {
    
    // FIELDS -- FX --
    
    // get absolute CVFXApp class and load resource
    private URL fxmlLocation = CVFXApp.class.getResource("cvfxgui.fxml");
    private String controllerCanonicalName = ""; // TODO : implement controller loading in code
    
    protected FXMLLoader fxmlloader;
    protected Parent guiroot;
    protected Scene mainscene;
    protected CVFXController controller;
    
    // FIELDS -- SPECIFIC --
    private boolean loggingActive = true;
    
    // METHODS -- MISC --
    
    // METHODS -- ACCESSORS --
    
    public void setLoggingActive(boolean active) {loggingActive = active;}
    public boolean isLoggingActive() {return loggingActive;}
    
    // call this before onAppStarted() ( in constructor of subclass )
    protected void setFxmlLocation(URL f) {
        fxmlLocation = f;
    }
    
    // METHODS -- FX --
    
    @Override
    public void start(Stage stg) throws IOException {
    
        log("Launching CVFXApp");
        
        // check if OpenCV is loaded
        try {
            new Mat();
        } catch(UnsatisfiedLinkError e) {
            System.err.println("FATAL ERROR - OpenCV not loaded !");
            System.exit(-1);
        }
        
        // test if fxml there is fxml defined
        if (fxmlLocation == null) {
            throw new RuntimeException("FATAL ERROR : fxmlLocation URL is null !!!");
        }
        fxmlloader = new FXMLLoader(fxmlLocation);
        log("Loaded fxml : " + fxmlLocation.getPath());
        
        // instantiate controller by getting the correct class using classloader
        // TODO : get that class by CVFXApp subclass annotation
        try {
            controller = (CVFXController) ClassLoader.getSystemClassLoader().loadClass("com.plasmoxy.cvfxexamples.dev.Controller").newInstance();
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
            System.err.println(" --- FATAL INTERNAL ERROR in classloading ---");
            ex.printStackTrace();
            System.out.println(-1);
        }
    
        log("Loaded controller : " + controller.getClass().getCanonicalName());
        
        fxmlloader.setController(controller);
        
        
        guiroot = fxmlloader.load();
        mainscene = new Scene(guiroot);
        
        stg.setScene(mainscene);
        stg.sizeToScene();
        stg.setTitle("Plasmoxy - CVFXBase");
        
        stg.setOnCloseRequest(event ->
        {
            log("Received close signal, calling closeController on controller");
            controller.closeController();
        });
        
        controller.initController(); // tell the controller to initialize itself now
        stg.show(); // render the stage
        
        // after rendering, fix the minimal size of it ( by the time this executes, the window is still in normal size )
        stg.setMinHeight(stg.getHeight());
        stg.setMinWidth(stg.getWidth());
        
        // maximize window after fixing the min size
        stg.setMaximized(true);
        
        // now after everything is rendered, call optional controller init
        controller.initAfterShow();
        
        log("CVFXApp launched");
        
    }
    
    @Override
    public void stop() {
        log("CVFXApp stopped");
    }
    
    // run code after rendering
    protected abstract void onAppStarted();
    
    // METHODS -- Other --
    
    protected void log(String text) {
        if (loggingActive) System.out.println("[CVFXApp] " + text);
    }
}
