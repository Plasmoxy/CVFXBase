package facetest;

import com.plasmoxy.cvfxbase.CVFXController;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Controller extends CVFXController{

	private boolean screenshotTakingActive;

	@Override
	protected void init() {
		show(buttonA);
		buttonA.setText("Screenshot");
	}

	@Override
	protected void process(Mat f, Mat a, Mat b) {
		Core.flip(f, f, 1);
		Imgproc.cvtColor(f, f, Imgproc.COLOR_BGR2RGB);
		if (screenshotTakingActive) {
			Imgcodecs.imwrite("screenshot.jpg", f);
			screenshotTakingActive = false;
		}
		Core.flip(f, a, 1);
		Core.flip(f, b, 0);
	}

	@Override
	protected void buttonAPressed() {
		screenshotTakingActive = true;
	}
}
