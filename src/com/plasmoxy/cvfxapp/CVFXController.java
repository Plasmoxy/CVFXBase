package com.plasmoxy.cvfxapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

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
 *  - this used jfoenix but then i just modified it to native fx node interfaces because the base is same
 */

public abstract class CVFXController { // control class for gui.fxml

    // FIELDS -- Nodes --

    @FXML protected Button cameraButton;
    @FXML protected ImageView imageViewMain; // the biggest imageViewMain
    @FXML protected ImageView imageViewAlpha; // small upper imageViewMain
    @FXML protected ImageView imageViewBeta; // small lower imageViewMain
    @FXML protected Label infoLabelA;

    // FIELDS -- CV --

    protected VideoCapture cap;
    protected boolean cameraActive = false;
    protected int cameraID = 0; // ID OF THE CAMERA

    // use these flags in cv too
    protected boolean renderMainActive = true, renderAlphaActive, renderBetaActive;

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

    // METHODS -- CONTROLLER --

    protected void initController() { // only in package
        System.out.println("[CVFXController] Initializing controller");
        cap = new VideoCapture();

        // fix imageViews so they don't resize
        imageViewMain.setFitWidth(640);
        imageViewMain.setPreserveRatio(true);
        imageViewAlpha.setFitWidth(320);
        imageViewAlpha.setPreserveRatio(true);
        imageViewBeta.setFitWidth(320);
        imageViewBeta.setPreserveRatio(true);

    }

    protected void closeController() { // external
        stopRendering();
    }

    // METHODS -- FXML --

    @FXML
    protected void startCamera(ActionEvent event) {
        if (!cameraActive) {
            cap.open(cameraID);

            if (cap.isOpened()) {
                cameraActive = true;
                startRendering();
                cameraButton.setText("Stop Camera");
            } else {
                System.err.println("Cant open camera");
                cameraButton.setText("ERROR");
            }

        } else {
            stopRendering();
            updateStartText();
        }
    }

    protected void updateStartText() {
        cameraButton.setText("Start Camera " + String.valueOf(cameraID));
    }

    @FXML
    protected void increaseCamera(ActionEvent e) {
        if (cameraActive) stopRendering();
        cameraID++;
        updateStartText();
    }

    @FXML
    protected void decreaseCamera(ActionEvent e) {
        if (cameraActive) stopRendering();
        if (cameraID>0) cameraID--;
        updateStartText();
    }

    @FXML
    protected void renderMainAction(ActionEvent e) {
        renderMainActive = ((ToggleButton)e.getSource()).isSelected();
    }

    @FXML
    protected void renderAlphaAction(ActionEvent e) {
        renderAlphaActive = ((ToggleButton)e.getSource()).isSelected();
    }

    @FXML
    protected void renderBetaAction(ActionEvent e) {
        renderBetaActive = ((ToggleButton)e.getSource()).isSelected();
    }

    // METHODS -- CV --

    // grab mat from video capture
    protected Mat grabFrame() {
        Mat frame = new Mat(); // empty mat
        if (cap.isOpened()) {
            try {
                cap.read(frame);
            } catch (Exception ex) {
                System.err.println("[CV] Error during image processing.");
            }
        }
        return frame;
    }

    protected void startRendering() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameRenderer, 0, 33, TimeUnit.MILLISECONDS);
        infoLabelA.setText("- Rendering active -");
        System.out.println("[CVFXController] Rendering started");
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

        infoLabelA.setText("- Rendering stopped -");
        System.out.println("[CVFXController] Rendering stopped");

        if (cap.isOpened()) cap.release();
    }
    
    // METHODS -- SPECIFIC --

    // process the frame here
    // frames - imageViewMain, imageViewAlpha, imageViewBeta
    protected abstract void process(Mat f, Mat a, Mat b);

}
