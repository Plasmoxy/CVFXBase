package com.plasmoxy.cvfxbase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Mat;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * CVFXBase OpenCV Base in JavaFX
 *
 * @author Plasmoxy
 * @version 1.2
 *
 * How launch :
 * - extend this and CVFXController class
 * - in subclass of this add @ControllerClass annotation to main constructor ( non-parameter ) and put controller class in this annotation
 * - load opencv before launching
 * - call launch(args) on subclass of this
 */

public abstract class CVFXApp extends Application {
    
    // FIELDS -- META --
    
    public static final String VERSION = "v1.2";
    
    // FIELDS -- FX --
    
    // get absolute CVFXApp class and load resource
    private URL fxmlLocation = CVFXApp.class.getResource("cvfxgui.fxml");
    
    protected FXMLLoader fxmlloader;
    protected Parent guiroot;
    protected Scene mainscene;
    protected CVFXController controller;
    protected Stage appstage;
    
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
    
    protected void setStageTitle(String title) {
        if(appstage == null) System.out.println("ERROR : Stage not yet initialized.");
        else appstage.setTitle(title);
    }
    
    // METHODS -- FX --
    
    @Override
    public void start(Stage stg) throws IOException {
        
        log("Launching CVFXApp");
        appstage = stg; // set the stage reference to a field
        
        // check if OpenCV is loaded
        try {
            new Mat();
        } catch(UnsatisfiedLinkError e) {
            System.err.println("FATAL ERROR - OpenCV not loaded !");
            System.exit(-1);
        }
        
        // test if there is fxml defined
        if (fxmlLocation == null) {
            throw new RuntimeException("FATAL ERROR : fxmlLocation URL is null !!!");
        }
        fxmlloader = new FXMLLoader(fxmlLocation);
        log("Loaded fxml : " + fxmlLocation.getPath());
        
        
        // process annotations of subclass constructor
        String annotatedTitle = "";
        try {
            // get constructors of the inherited app subclass
            Constructor[] constructors = getClass().getConstructors();
            if (constructors.length == 0) {
                System.out.println("FATAL INTERNAL ERROR : App class must have a non-parameter constructor declared.");
                System.exit(-1);
            }
            // get the annotation from subclass default non-paramenter constructor ( this constructor must be always present )
            ControllerClass contrClassAnnot = (ControllerClass)constructors[0].getAnnotation(ControllerClass.class);
            if (contrClassAnnot == null) {
                System.out.println("FATAL INTERNAL ERROR : No controller class annotation @ControllerClass(Class<? extends CVFXController> controller) was specified in default constructor !!!");
                System.exit(-1);
            }
            
            // get the controller class from annotation and instantiate it into controller field
            controller = contrClassAnnot.value().newInstance();
            
            // get AppTitle annotation and pass it to the title string
            AppTitle appTitleAnnot = (AppTitle)(constructors[0]).getAnnotation(AppTitle.class);
            if (appTitleAnnot != null) {
                annotatedTitle = appTitleAnnot.value();
            }
            
        } catch(InstantiationException e) {
            System.out.println("FATAL INTERNAL ERROR : Error on controller class instantiation.");
            e.printStackTrace();
            System.exit(-1);
        } catch(IllegalAccessException e) {
            System.out.println("FATAL INTERNAL ERROR : Reflection cannot access controller class.");
            e.printStackTrace();
            System.exit(-1);
        }
    
        log("Loaded controller : " + controller.getClass().getCanonicalName());
        
        // set this controller instance as controller for the fxml
        fxmlloader.setController(controller);
        
        // load fxml and setup gui
        
        guiroot = fxmlloader.load();
        mainscene = new Scene(guiroot);
        
        stg.setScene(mainscene);
        stg.sizeToScene();
        stg.setTitle(annotatedTitle + " [ CVFXBase " + VERSION + " ]");
        
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
        
        onAppStarted();
        
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
