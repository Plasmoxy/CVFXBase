package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Controller extends CVFXController {
    
    private boolean showSliderState;
    
    @Override
    protected void init() {
        log("Controller init");
        
        hide(buttonA, buttonB);
        
        buttonA.setText("Slider ON");
        buttonB.setText("Slider OFF");
    }
    
    @Override
    protected void process(Mat f, Mat a, Mat b) {
        f.copyTo(a);
        f.copyTo(b);
    }
    
    @Override
    protected void sliderAChanged(Number oldVal, Number newVal) {
        if (showSliderState) {
            infoText.set(0, newVal.toString());
            updateInfoLabel();
        }
    }
    
    @Override
    protected void buttonAPressed() {
        showSliderState = true;
    }
    
    @Override
    protected void buttonBPressed() {
        showSliderState = false;
    }
    
}
