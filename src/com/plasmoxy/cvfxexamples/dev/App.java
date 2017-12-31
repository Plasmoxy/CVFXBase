package com.plasmoxy.cvfxexamples.dev;

import com.plasmoxy.cvfxbase.CVFXApp;
import org.opencv.core.Core;

public class App extends CVFXApp {
    
    public App() {
    }
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
    
}
