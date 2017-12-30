import com.plasmoxy.cvfxapp.CVFXController;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Controller extends CVFXController {
    
    public Controller() {
    
    }
    
    @Override
    protected void process(Mat f, Mat a, Mat b) {
        Core.flip(f, a, 1);
    }
    
}
