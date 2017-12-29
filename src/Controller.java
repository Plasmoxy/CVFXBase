import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Controller class for CVFXBase by Plasmoxy
 * MAKE SURE YOU SET THE CORRECT PACKAGE PATH TO THIS CLASS IN FXML !!!
 *
 * Some notes from Sebb :
 *  - because Mat is native object, null check doesn't work, always initialize it with new Mat() instead of null !
 *      ( it was a big bug in my code )
 */

public class Controller { // control class for gui.fxml

    // FIELDS -- Nodes --

    @FXML JFXButton cameraButton;
    @FXML ImageView imageView; // the biggest imageView
    @FXML ImageView imageViewAlpha; // small upper imageView
    @FXML ImageView imageViewBeta; // small lower imageView
    @FXML Label infoLabelA;

    // FIELDS -- CV --

    private VideoCapture cap;
    private boolean cameraActive = false;
    private int cameraID = 0; // ID OF THE CAMERA

    // use these flags in cv too
    private volatile boolean renderMainActive = true, renderAlphaActive, renderBetaActive;

    // FIELDS -- Render --

    private ScheduledExecutorService timer;
    private Runnable frameRenderer = () -> {
        Mat frame = grabFrame(), frameAlpha = new Mat(), frameBeta = new Mat(); // grab frame from camera

        // if there is a frame to process&show
        if (!frame.empty()) {
            
            // process frames and update views
            process(frame, frameAlpha, frameBeta);
            if (renderMainActive) CVUtility.setProperty(imageView.imageProperty(), CVUtility.mat2Image(frame));

            // update alpha and beta views
            if (renderAlphaActive && !frameAlpha.empty())
                CVUtility.setProperty(imageViewAlpha.imageProperty(), CVUtility.mat2Image(frameAlpha));

            if (renderBetaActive && !frameBeta.empty())
                CVUtility.setProperty(imageViewBeta.imageProperty(), CVUtility.mat2Image(frameBeta));

        }


    };

    // METHODS -- CONTROLLER --

    public void init() { // external
        System.out.println("[Controller] Initializing controller");
        cap = new VideoCapture();

        // fix imageViews so they don't resize
        imageView.setFitWidth(640);
        imageView.setPreserveRatio(true);
        imageViewAlpha.setFitWidth(320);
        imageViewAlpha.setPreserveRatio(true);
        imageViewBeta.setFitWidth(320);
        imageViewBeta.setPreserveRatio(true);

    }

    protected void close() { // external
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

    private void updateStartText() {
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
        renderMainActive = ((JFXToggleButton)e.getSource()).isSelected();
    }

    @FXML
    protected void renderAlphaAction(ActionEvent e) {
        renderAlphaActive = ((JFXToggleButton)e.getSource()).isSelected();
    }

    @FXML
    protected void renderBetaAction(ActionEvent e) {
        renderBetaActive = ((JFXToggleButton)e.getSource()).isSelected();
    }

    // METHODS -- CV --

    // grab mat from video capture
    private Mat grabFrame() {
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

    private void startRendering() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameRenderer, 0, 33, TimeUnit.MILLISECONDS);
        infoLabelA.setText("- Rendering active -");
        System.out.println("[Controller] Rendering started");
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

        infoLabelA.setText("- Rendering stopped -");
        System.out.println("[Controller] Rendering stopped");

        if (cap.isOpened()) cap.release();
    }

    // process the frame here
    // frames - imageView, imageViewAlpha, imageViewBeta
    private void process(Mat f, Mat a, Mat b) {
        if (renderAlphaActive) Core.flip(f, a, 1);
        if (renderBetaActive) Core.flip(f, b, 0);
    }

}
