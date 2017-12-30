package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXController;
import javafx.application.Platform;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Controller extends CVFXController {
    
    @Override
    protected void process(Mat f, Mat a, Mat b) {
        Core.flip(f, a, 1);
        
        infoText = Double.toString(Math.random());
        updateInfoLabel();
    }
    
}
