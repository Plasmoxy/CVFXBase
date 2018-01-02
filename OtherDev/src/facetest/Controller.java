package facetest;

import com.plasmoxy.cvfxbase.CVFXController;
import javafx.application.Platform;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class Controller extends CVFXController{

	private int absoluteFaceSize;
	private CascadeClassifier faceCascade = new CascadeClassifier(), eyeCascade = new CascadeClassifier();
	private boolean faceDetectActive;

	@Override
	protected void init() {
		faceCascade.load("C:\\QUICKLIB\\opencv301\\build\\etc\\haarcascades\\haarcascade_frontalface_alt.xml");
		eyeCascade.load("C:\\QUICKLIB\\opencv301\\build\\etc\\haarcascades\\haarcascade_eye.xml");

		toggleA.setText("Detect face");

		hideAll();
		show(toggleA);
	}

	@Override
	protected void process(Mat f, Mat a, Mat b) {
		if(faceDetectActive) detectAndDisplay(f);
	}

	@Override
	protected void toggleAChanged(boolean active) {
		faceDetectActive = active;
	}

	private void detectAndDisplay(Mat frame)
	{
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		MatOfRect eyes = new MatOfRect();

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (20% of the frame height, in our case)
		if (this.absoluteFaceSize == 0)
		{
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0)
			{
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		// detect
		faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		eyeCascade.detectMultiScale(frame, eyes);

		for (Rect face : faces.toArray()) {

			//Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 3);

			for (Rect eye : eyes.toArray()) {
				if (eye.tl().y < (face.tl().y + face.br().y)*0.5 && eye.tl().x < face.br().x && eye.tl().y > face.tl().y && eye.tl().x > face.tl().x) {
					Imgproc.circle(frame, new Point(eye.x + eye.width*0.5, eye.y + eye.height*0.5), 40, new Scalar(0, 0, 0), 2);
					//Imgproc.rectangle(frame, new Point(eye.x, eye.y), new Point(eye.x + eye.width, eye.y + eye.height), new Scalar(200, 200, 100),2);
				}
			}

		}


	}
}
