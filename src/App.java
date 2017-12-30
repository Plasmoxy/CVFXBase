import com.plasmoxy.cvfxapp.CVFXApp;
import org.opencv.core.Core;

public class App extends CVFXApp {
    
    public App() {
        setFxmlLocation(getClass().getResource("gui.fxml"));
    }
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
    
}
