import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * CVFXBase OpenCV Base in JavaFX by Plasmoxy
 */

public class App extends Application
{

    // FIELDS -- FX --

    FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
    Parent guiroot;
    Scene sc;
    Controller contr;

    // METHODS -- FX --

    public void start(Stage stg) throws IOException
    {
        guiroot = loader.load();
        sc = new Scene(guiroot);
        
        contr = loader.getController();

        stg.setScene(sc);
        stg.sizeToScene();
        stg.setTitle("Plasmoxy - CVFXBase");

        stg.setOnCloseRequest( event ->
        {
            System.out.println("[App] Received close signal, calling close on controller");
            contr.close();
        });

        contr.init(); // tell the controller to initialize itself now
        stg.show(); // render the stage

        // after rendering, fix the min size of it
        stg.setMinHeight(stg.getHeight());
        stg.setMinWidth(stg.getWidth());
        
        // maximize window
        stg.setMaximized(true);

        System.out.println("[App] App launched");
        
    }

    public void stop()
    {
        System.out.println("[App] App stopped");
    }

    // METHODS -- Program --

    public static void main(String[] args)
    {
        System.out.println(" --- CVFXBase OpenCV Base in JavaFX by Plasmoxy ---\n");
        System.out.println("[main] Loading OpenCV");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // load opencv
        System.out.println("[main] Launching App");
        launch(args); // launch fx

        System.out.println("[main] EXIT");
    }
}
