package com.plasmoxy.cvfxbase;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CVFXController class for CVFXBase
 *
 * @author Plasmoxy
 * @version 1.0 BETA
 *
 * Some notes of mine :
 *  - because Mat is native object, null check doesn't work, always initialize it with new Mat() instead of null !
 *      ( it was a big bug in my code )
 *  - this used jfoenix but then i just modified it to native fx node classes because the base is same
 *      ( so you can but dont have to use jfoenix or any other fxml library )
 *  - getClass() called anywhere apparently returns the highest inherited subclass of specified instance
 *  - to get type of field, use getType() on Field instance instead -> getClass() returns Field.class
 */

public abstract class CVFXController {

    // FIELDS -- FXML Nodes --
    
    // SECTION views
    // alpha = smaller upper view, beta = smaller lower view
    @FXML protected ImageView imageViewMain, imageViewAlpha, imageViewBeta;
    
    // SECTION buttons
    @FXML protected Button cameraButton;
    @FXML @Hidable protected Button buttonA, buttonB, buttonC, buttonD, buttonE, buttonF;
    @FXML @Hidable protected ToggleButton toggleA, toggleB, toggleC, toggleD, toggleE, toggleF, toggleG, toggleH;
    
    // SECTION sliders
    @FXML @Hidable protected Slider sliderA, sliderB, sliderC, sliderD, sliderE, sliderF, sliderG;
    @FXML @Hidable protected Label sliderALabel, sliderBLabel, sliderCLabel, sliderDLabel, sliderELabel, sliderFLabel, sliderGLabel;
    
    // SECTION other
    
    // array with nodes which will be hidden with hideAll method, loaded in init using reflection
    protected LinkedList<Node> nodesToHide = new LinkedList<>();
    
    @FXML protected Label infoLabel; // the label under views pane
    public List<String> infoText = new ArrayList<>(); // list with some strings which get concatenated in infoLabel

    // FIELDS -- CV --

    protected VideoCapture cap;
    protected boolean cameraActive = false;
    protected int cameraID = 0; // ID OF THE CAMERA

    // flags for rendering, these get updated by action methods for the toggle buttons
    // main is active by default, just like the toggle button in fxml
    public boolean renderMainActive = true, renderAlphaActive, renderBetaActive;

    // FIELDS -- Render --

    private ScheduledExecutorService timer;
    private Runnable frameRenderer = () -> {
        Mat frame = grabFrame(), frameAlpha = new Mat(), frameBeta = new Mat(); // grab frame from camera

        // if there is a frame to process&show
        if (!frame.empty()) {
            
            // process frames and update views
            process(frame, frameAlpha, frameBeta);
            if (renderMainActive) CVUtility.setProperty(imageViewMain.imageProperty(), CVUtility.mat2Image(frame));

            // update alpha and beta views
            if (renderAlphaActive && !frameAlpha.empty())
                CVUtility.setProperty(imageViewAlpha.imageProperty(), CVUtility.mat2Image(frameAlpha));

            if (renderBetaActive && !frameBeta.empty())
                CVUtility.setProperty(imageViewBeta.imageProperty(), CVUtility.mat2Image(frameBeta));

        }


    };
    
    // METHODS -- ACCESSORS --

    // METHODS -- CONTROLLER --

    // internal init
    void initController() { // only in package
        log("Initializing controller");
        
        cap = new VideoCapture(); // create new capture object

        // fix imageViews so they don't resize
        imageViewMain.setFitWidth(640);
        imageViewMain.setPreserveRatio(true);
        imageViewAlpha.setFitWidth(320);
        imageViewAlpha.setPreserveRatio(true);
        imageViewBeta.setFitWidth(320);
        imageViewBeta.setPreserveRatio(true);
        
        // TODO : add listeners to sliders
        sliderA.valueProperty().addListener((observableValue, old_val, new_val) -> sliderAChanged(old_val, new_val));
        
        // allocate some String objects in the infotext arraylist
        for (int i = 0; i<16; i++) infoText.add("");
        
        // load node references which should be hidden using java reflection
        // this code works with annotations and is quite complicated
        
        // using CVFXController absolute class ( no need for relativity stuff like getClass())
        // this is metacode ( code processes the code )
        for (Field f : CVFXController.class.getDeclaredFields()) { // getDeclared because we need fields only in this class and we need private
            
            // to determine if f type is a Node, we check if Node can be casted (assigned) from this type
            if (Node.class.isAssignableFrom(f.getType())) {
                
                // if this Node is Hidable, add it to nodesToHide list
                if (f.isAnnotationPresent(Hidable.class)) {
                    try {
                        nodesToHide.add((Node)f.get(this));
                    } catch (IllegalAccessException e) {
                        System.out.println("FATAL INTERNAL ERROR - Node field not accessible by reflection");
                    }
                }
            }
        }
        

        init();
    }
    
    // external additional init
    protected abstract void init();
    
    // init after app is displayed
    protected void initAfterShow() {}

    protected void closeController() { // external
        stopRendering();
    }

    // METHODS -- GUI --
    
    private void updateStartText() {
        cameraButton.setText("Start Camera " + String.valueOf(cameraID));
    }
    
    // method to update the status info label
    public void updateInfoLabel() {
        StringBuilder temp = new StringBuilder();
        for (String s : infoText) temp.append(s);
        Platform.runLater(() -> infoLabel.setText( (cameraActive ? "[ Rendering Active ] " : "[ Rendering stopped ] ") + temp.toString()));
    }
    
    // methods for showing and hiding elements of gui <thread unsafe>
    public void hide(Node... ns) {
        for (Node n : ns) n.setVisible(false);
    }
    public void show(Node... ns) {
        for (Node n : ns) n.setVisible(true);
    }
    public void hideAll() {
        for (Node n : nodesToHide) {
            hide(n);
        }
    }
    
    
    // SECTION slider handling
    // these methods are called in initController method
    // they can be overridden, but don't have to be...
    
    protected void sliderAChanged(Number oldVal, Number newVal) {}
    
    // SECTION button handling
    
    @FXML protected void buttonAPressed() {}
    @FXML protected void buttonBPressed() {}
    @FXML protected void buttonCPressed() {}
    @FXML protected void buttonDPressed() {}
    @FXML protected void buttonEPressed() {}
    @FXML protected void buttonFPressed() {}

    @FXML
    private void startCamera() {
        if (!cameraActive) {
            cap.open(cameraID);

            if (cap.isOpened()) {
                cameraActive = true;
                startRendering();
                cameraButton.setText("Stop Camera");
            } else {
                log("CANNOT OPEN CAMERA");
                cameraButton.setText("ERROR");
            }

        } else {
            stopRendering();
            updateStartText();
        }
    }

    @FXML
    private void increaseCamera() {
        if (cameraActive) stopRendering();
        cameraID++;
        updateStartText();
    }

    @FXML
    private void decreaseCamera() {
        if (cameraActive) stopRendering();
        if (cameraID>0) cameraID--;
        updateStartText();
    }
    
    // SECTION toggle button handling

    @FXML private void renderMainAction(ActionEvent e) {
        renderMainActive = ((ToggleButton)e.getSource()).isSelected();
    }
    @FXML private void renderAlphaAction(ActionEvent e) {
        renderAlphaActive = ((ToggleButton)e.getSource()).isSelected();
    }
    @FXML private void renderBetaAction(ActionEvent e) {
        renderBetaActive = ((ToggleButton)e.getSource()).isSelected();
    }
    
    @FXML private void toggleAAction() {toggleAChanged(toggleA.isSelected());}
    @FXML private void toggleBAction() {toggleBChanged(toggleB.isSelected());}
    @FXML private void toggleCAction() {toggleCChanged(toggleC.isSelected());}
    @FXML private void toggleDAction() {toggleDChanged(toggleD.isSelected());}
    @FXML private void toggleEAction() {toggleEChanged(toggleE.isSelected());}
    @FXML private void toggleFAction() {toggleFChanged(toggleF.isSelected());}
    @FXML private void toggleGAction() {toggleGChanged(toggleG.isSelected());}
    @FXML private void toggleHAction() {toggleHChanged(toggleH.isSelected());}
    
    // - Methods for primitive usage -, can but don't have to be overridden
    protected void toggleAChanged(boolean selected) {}
    protected void toggleBChanged(boolean selected) {}
    protected void toggleCChanged(boolean selected) {}
    protected void toggleDChanged(boolean selected) {}
    protected void toggleEChanged(boolean selected) {}
    protected void toggleFChanged(boolean selected) {}
    protected void toggleGChanged(boolean selected) {}
    protected void toggleHChanged(boolean selected) {}

    // METHODS -- CV --

    // grab mat from video capture
    protected Mat grabFrame() {
        Mat frame = new Mat(); // empty mat
        if (cap.isOpened()) {
            try {
                cap.read(frame);
            } catch (Exception ex) {
                log("[CV] Error during image processing.");
            }
        }
        return frame;
    }

    protected void startRendering() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameRenderer, 0, 33, TimeUnit.MILLISECONDS);
        
        updateInfoLabel();
        log("Rendering started");
    }

    protected void stopRendering() {
        cameraActive = false;

        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        updateInfoLabel();
        
        log("Rendering stopped");

        if (cap.isOpened()) cap.release();
    }
    
    // METHODS -- SPECIFIC --

    // process the frame here
    // frames - imageViewMain, imageViewAlpha, imageViewBeta
    protected abstract void process(Mat f, Mat a, Mat b);
    
    
    // METHODS -- OTHER --
    
    protected void log(String text) {
        System.out.println("[CVFXController] " + text);
    }

}
