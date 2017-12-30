package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Controller extends CVFXController {
    
    private boolean showSliderState;
    private double sliderVal;
    
    @Override
    protected void init() {
        log("Controller init");
        
        // show only used nodes
        hideAll();
        show(buttonA, buttonB, sliderA, sliderALabel);
        
        buttonA.setText("Circle ON");
        buttonB.setText("Circle OFF");
        sliderALabel.setText("CIRCLE");
    }
    
    @Override
    protected void process(Mat f, Mat a, Mat b) {
        Imgproc.circle(f, new Point(640/2, 480/2), (int)sliderVal, new Scalar(0, 255, 0), 2);
    }
    
    @Override
    protected void sliderAChanged(Number oldVal, Number newVal) {
        if (showSliderState) {
            sliderVal = newVal.doubleValue();
            infoText.set(0, "Slider = " + newVal.toString());
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
