package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXApp;
import org.opencv.core.Core;

public class App extends CVFXApp {
    
    @Override
    protected void onAppStarted() {}
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
    
}
