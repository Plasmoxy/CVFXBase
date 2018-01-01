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
 * Abstract controller class with all the logic of OpenCV and stuff.
 * This class is instantiated externally using CVFXApp instance, NOT in fxml.
 *
 * <p>
 * My notes which are not important to the user :
 * <ul>
 *     <li>because Mat is native object, null check doesn't work, always initialize it with new Mat() instead of null
 *  ( it was a big bug in my code )</li>
 *     <li>this used jfoenix but then i just modified it to native fx node classes because the base is same
 *  ( so you can but dont have to use jfoenix or any other fxml library )</li>
 *     <li>getClass() called anywhere apparently returns the highest inherited subclass of specified instance</li>
 *     <li>to get type of field, use getType() on Field instance instead, getClass() returns Field.class</li>
 * </ul>
 *
 * @author <a target="_blank" href="http://github.com/Plasmoxy">Plasmoxy</a>
 * @version 1.2
 *
 */

public abstract class CVFXController {

    // FIELDS -- FXML Nodes --
    
    // SECTION views
    /**
     *  FXML Image views for the 3 OpenCV Mats
     */
    @FXML protected ImageView imageViewMain, imageViewAlpha, imageViewBeta;
    
    // SECTION buttons
    /**
     * Button for starting camera
     */
    @FXML protected Button cameraButton;
    
    /**
     * User modifiable Buttons for program functionality
     */
    @FXML @Hidable protected Button buttonA, buttonB, buttonC, buttonD, buttonE, buttonF;
    
    /**
     * User modifiable ToggleButtons
     */
    @FXML @Hidable protected ToggleButton toggleA, toggleB, toggleC, toggleD, toggleE, toggleF, toggleG, toggleH;
    
    // SECTION sliders
    
    /**
     * User modifiable Sliders
     */
    @FXML @Hidable protected Slider sliderA, sliderB, sliderC, sliderD, sliderE, sliderF, sliderG;
    
    /**
     * User modifiable Slider Labels
     *
     * These get detected by reflection and hidden/shown automatically together with their Slider with hide() and show()
     */
    @FXML protected Label sliderALabel, sliderBLabel, sliderCLabel, sliderDLabel, sliderELabel, sliderFLabel, sliderGLabel;
    
    // SECTION other
    
    /**
     * Array with nodes which will be hidden with hideAll method, loaded in init using reflection
     */
    private LinkedList<Node> nodesToHide = new LinkedList<>();
    
    /**
     * User modifiable label with info.
     * This is dependent on infoText List and gets updated by updateInfoLabel() method
     */
    @FXML protected Label infoLabel; // the label under views pane
    
    /**
     * List with Strings which get together contanetated in updateInfoLabel()
     */
    private List<String> infoText = new ArrayList<>(); // list with some strings which get concatenated in infoLabel

    // FIELDS -- CV --
    
    private VideoCapture videoCapture;
    private boolean cameraActive = false;
    private int cameraID = 0; // ID OF THE CAMERA
    
    /**
     * Flags for rendering, these get updated by action methods for the three rendering toggle buttons.
     * Main is active by default, just like the toggle button in fxml.
     */
    private boolean renderMainActive = true, renderAlphaActive, renderBetaActive;

    // FIELDS -- Render --
    
    /**
     * This ExecutorService updates the views at 30 FPS
     */
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
    
    // FIELDS -- SPECIFIC --
    
    private boolean loggingActive = true;
    
    // METHODS -- CONSTRUCTORS --
    
    /**
     *  Internal constructor for this superclass
     *  It just adds the strings to infoText List
     */
    public CVFXController() {
        // allocate some String objects in the infotext arraylist (32 is pretty much enough (its too much lol))
        for (int i = 0; i<32; i++) infoText.add("");
    }
    
    // METHODS -- ACCESSORS --
    
    /**
     * Sets the log function will print to standard output ( Sets the logging ).
     * @param active logging active
     */
    public void setLoggingActive(boolean active) {loggingActive = active;}
    
    /**
     * Returns boolean whether the logging is active.
     * @return logging active
     */
    public boolean isLoggingActive() {return loggingActive;}
    
    /**
     *  Returns the OpenCV capture object
     * @return videoCapture
     */
    public VideoCapture getVideoCapture() { return videoCapture; }
    
    /**
     *  Gets camera active boolean.
     * @return cameraActive
     */
    public boolean isCameraActive() { return cameraActive; }
    
    /**
     * Determines whether the Main view is rendered.
     * @return renderMainActive
     */
    public boolean isRenderMainActive() { return renderMainActive; }
    
    /**
     * Determines whether the Alpha view is rendered.
     * @return renderAlphaActive
     */
    public boolean isRenderAlphaActive() { return renderAlphaActive; }
    
    /**
     * Determines whether the Beta view is rendered.
     * @return renderBetaActive
     */
    public boolean isRenderBetaActive() { return renderBetaActive; }
    
    /**
     * Sets the render active state of Main view
     * @param active renderMainActive
     */
    public void setRenderMainActive(boolean active) { renderMainActive = active; }
    
    /**
     * Sets the render active state of ALpha view
     * @param active renderAlphaActive
     */
    public void setRenderAlphaActive(boolean active) { renderAlphaActive = active; }
    
    /**
     * Sets the render active state of Beta view
     * @param active renderBetaActive
     */
    public void setRenderBetaActive(boolean active) { renderBetaActive = active; }
    
    /**
     *  Sets the camera id ( cannot be lower than zero ). Use this only in init method !
     * @param id Camera id.
     */
    protected void setCameraID(int id) {
        cameraID = id < 0 ? 0 : id;
        updateStartButtonText();
    }
    
    /**
     * Gets the camera id.
     * @return cameraID
     */
    public int getCameraID() { return cameraID;}
    
    /**
     * Sets a String text in infoText List and updates the info label with updateInfoLabel() method
     * @param position The text field number/position
     * @param text The actual text string which should be set.
     */
    public void setInfoText(int position, String text) {
        if (position < 32) {
            infoText.set(position, text);
        } else {
            log("LOGIC ERROR : You cant set a text field higher than 31 !");
        }
        updateInfoLabel(); // TODO : ?
    }
    
    /**
     * Returns the text of a infoText list by given position
     * @param position The position/number of the text field
     * @return The text string.
     */
    public String getInfoText(int position) {
        if (position < 32) {
            return infoText.get(position);
        } else {
            log("LOGIC ERROR : There is no such text field.");
            return "ERROR";
        }
    }

    // METHODS -- CONTROLLER --
    
    /**
     * This method internally initializes the controller.
     * It is called internally from the CVFXApp instance.
     * It creates the videoCapture object, links listener,sets the sizes of views and similar.
     * It also parses the fields and checks for fields with @Hidable annotation and adds them to nodesToHide List.
     *
     * This method is used only in package and mustn't be overridden.
     */
    void initController() { // only in package
        log("Initializing controller");
        
        videoCapture = new VideoCapture(); // create new capture object

        // fix imageViews so they don't resize
        imageViewMain.setFitWidth(640);
        imageViewMain.setPreserveRatio(true);
        imageViewAlpha.setFitWidth(320);
        imageViewAlpha.setPreserveRatio(true);
        imageViewBeta.setFitWidth(320);
        imageViewBeta.setPreserveRatio(true);
        
        // add listeners to sliders
        sliderA.valueProperty().addListener((observableValue, old_val, new_val) -> sliderAChanged(old_val, new_val));
        sliderB.valueProperty().addListener((observableValue, old_val, new_val) -> sliderBChanged(old_val, new_val));
        sliderC.valueProperty().addListener((observableValue, old_val, new_val) -> sliderCChanged(old_val, new_val));
        sliderD.valueProperty().addListener((observableValue, old_val, new_val) -> sliderDChanged(old_val, new_val));
        sliderE.valueProperty().addListener((observableValue, old_val, new_val) -> sliderEChanged(old_val, new_val));
        sliderF.valueProperty().addListener((observableValue, old_val, new_val) -> sliderFChanged(old_val, new_val));
        sliderG.valueProperty().addListener((observableValue, old_val, new_val) -> sliderGChanged(old_val, new_val));
        
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
                        System.exit(-1);
                    }
                }
            }
        }
        

        init();
    }
    
    /**
     * The abstract initialization method, has to be overridden in subclass.
     * By the time this method is called, all the fx stuff should be already initialized.
     *
     * USE THIS METHOD AS A MAIN METHOD IN YOUR CVFXBase PROJECT ( respectively, instead of controller constructor )
     * ( although feel free add your own constructor, but the superclass constructor must me called )
     */
    protected abstract void init();
    
    /**
     * This is an another initialization method.
     * It is called when the JavaFX Stage is shown (resp. rendered).
     * This method isn't mandatory.
     */
    protected void initAfterShow() {}
    
    /**
     * Gets called by App when it's closing.
     * It stops the rendering ( and closes the capture device )
     */
    protected void closeController() { // external
        stopRendering();
    }

    // METHODS -- GUI --
    
    /**
     * Updates the start button text ( dependent on cameraID )
     */
    private void updateStartButtonText() {
        cameraButton.setText("Start Camera " + String.valueOf(cameraID));
    }
    
    /**
     * Updates the info label ( concatenates the Strings in infoText field )
     */
    public void updateInfoLabel() {
        StringBuilder temp = new StringBuilder();
        for (String s : infoText) temp.append(s);
        Platform.runLater(() -> infoLabel.setText( (cameraActive ? "[ Rendering Active ] " : "[ Rendering stopped ] ") + temp.toString()));
    }
    
    /**
     * Visibility utility method for show and hide methods - automatic label hiding/showing for sliders
     * @param n Node to change visibility
     * @param visible visible
     */
    private void setVisibleDetected(Node n, boolean visible) {
        if ( Slider.class.isAssignableFrom(n.getClass())) {
            n.setVisible(visible);
            try {
                // grab a label field of slider, construct node from this instance and set its visibility
                ((Node)CVFXController.class.getDeclaredField(n.getId() + "Label").get(this)).setVisible(visible);
            } catch(NoSuchFieldException|IllegalAccessException ex) {
                System.out.println("FATAL INTERNAL ERROR : slider filed has no label field in code : " + n.getId());
                System.exit(-1);
            }
        } else {
            n.setVisible(visible);
        }
    }
    
    /**
     * Hides an user modifiable element (node) in GUI.
     * @param nodes Varargs with nodes which should be hidden.
     */
    public void hide(Node... nodes) {
        for (Node n : nodes) {
            setVisibleDetected(n, false);
        }
    }
    
    /**
     * Shows an user modifiable element (node) in GUI.
     * @param nodes Varargs with nodes which should be shown.
     */
    public void show(Node... nodes) {
        for (Node n : nodes) {
            setVisibleDetected(n, true);
        }
    }
    
    /**
     * Hides all user modifiable elements - only internal elements marked with @Hidable annotation
     */
    public void hideAll() {
        for (Node n : nodesToHide) {
            hide(n);
        }
    }
    
    
    // SECTION slider handling
    // these methods are called in initController method
    // they can be overridden, but don't have to be...
    /** This method executes, when sliderA changes (use newVal to get value and process it ) */
    protected void sliderAChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderB changes (use newVal to get value and process it ) */
    protected void sliderBChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderC changes (use newVal to get value and process it ) */
    protected void sliderCChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderD changes (use newVal to get value and process it ) */
    protected void sliderDChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderE changes (use newVal to get value and process it ) */
    protected void sliderEChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderF changes (use newVal to get value and process it ) */
    protected void sliderFChanged(Number oldVal, Number newVal) {}
    /** This method executes, when sliderG changes (use newVal to get value and process it ) */
    protected void sliderGChanged(Number oldVal, Number newVal) {}
    
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
            videoCapture.open(cameraID);

            if (videoCapture.isOpened()) {
                cameraActive = true;
                startRendering();
                cameraButton.setText("Stop Camera");
            } else {
                log("CANNOT OPEN CAMERA");
                cameraButton.setText("ERROR");
            }

        } else {
            stopRendering();
            updateStartButtonText();
        }
    }

    @FXML
    private void increaseCamera() {
        if (cameraActive) stopRendering();
        cameraID++;
        updateStartButtonText();
    }

    @FXML
    private void decreaseCamera() {
        if (cameraActive) stopRendering();
        if (cameraID>0) cameraID--;
        updateStartButtonText();
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
    private Mat grabFrame() {
        Mat frame = new Mat(); // empty mat
        if (videoCapture.isOpened()) {
            try {
                videoCapture.read(frame);
            } catch (Exception ex) {
                log("[CV] Error during image processing.");
            }
        }
        return frame;
    }

    private void startRendering() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameRenderer, 0, 33, TimeUnit.MILLISECONDS);
        
        updateInfoLabel();
        log("Rendering started");
    }

    private void stopRendering() {
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

        if (videoCapture.isOpened()) videoCapture.release();
    }
    
    // METHODS -- SPECIFIC --

    // process the frame here
    // frames - imageViewMain, imageViewAlpha, imageViewBeta
    protected abstract void process(Mat f, Mat a, Mat b);
    
    
    // METHODS -- OTHER --
    
    protected void log(String text) {
        if (loggingActive) System.out.println("[CVFXController] " + text);
    }

}
