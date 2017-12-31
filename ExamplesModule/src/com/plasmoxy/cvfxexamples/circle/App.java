package com.plasmoxy.cvfxexamples.circle;

import com.plasmoxy.cvfxbase.AppTitle;
import com.plasmoxy.cvfxbase.CVFXApp;
import com.plasmoxy.cvfxbase.ControllerClass;
import org.opencv.core.Core;

public class App extends CVFXApp {
    
    // set the controller class
    @ControllerClass(Controller.class)
    @AppTitle("CVFXBase Examples - Circle")
    public App() {}
    
    @Override
    protected void onAppStarted() {}
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
    
}
