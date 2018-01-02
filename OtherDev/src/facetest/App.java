package facetest;

import com.plasmoxy.cvfxbase.AppTitle;
import com.plasmoxy.cvfxbase.CVFXApp;
import com.plasmoxy.cvfxbase.ControllerClass;
import org.opencv.core.Core;

public class App extends CVFXApp {

	@ControllerClass(Controller.class)
	@AppTitle("OtherDev/facetest")
	public App() {}

	@Override
	protected void onAppStarted() {}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
