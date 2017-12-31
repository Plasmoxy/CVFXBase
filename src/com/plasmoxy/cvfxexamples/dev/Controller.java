package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXController;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Controller extends CVFXController {
    
    private boolean showSliderState, drawCircle;
    private double sliderVal;
    
    @Override
    protected void init() {
        setCameraID(1);
        
        // show only used nodes
        hideAll();
        show(buttonA, buttonB, toggleA); // slider is off by default
        
        buttonA.setText("Circle ON");
        buttonB.setText("Circle OFF");
        toggleA.setText("Draw Circle");
        sliderALabel.setText("Circle Radius");
        sliderA.setValue(0);
    }
    
    // SECTION logic
    
    @Override
    protected void process(Mat f, Mat a, Mat b) {
        if(drawCircle) Imgproc.circle(f, new Point(640/2, 480/2), (int)sliderVal, new Scalar(0, 255, 0), 2);
    }
    
    private void setCircleOn(boolean state) { // circle on or off
        drawCircle = state;
        if (state) {
            show(sliderA);
            showSliderState = true;
            sliderVal = sliderA.valueProperty().get(); // update value from slider
        } else {
            hide(sliderA);
            showSliderState = false;
        }
    }
    
    // SECTION handlijg
    
    @Override
    protected void sliderAChanged(Number oldVal, Number newVal) {
        if (showSliderState) {
            sliderVal = newVal.doubleValue();
            infoText.set(0, "Slider = " + newVal.toString());
            updateInfoLabel();
        }
    }
    
    @Override
    protected void toggleAChanged(boolean selected) {
        setCircleOn(selected);
    }
    
    @Override
    protected void buttonAPressed() {
        setCircleOn(true);
        toggleA.setSelected(true);
    }
    
    @Override
    protected void buttonBPressed() {
        setCircleOn(false);
        toggleA.setSelected(false);
    }
    
}
