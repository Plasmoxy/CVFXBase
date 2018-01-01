package com.plasmoxy.cvfxbase;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Useful methods for OpenCV.
 * Some methods from Luigi De Russis and his tutorial.
 *
 * @author <a target="_blank" href="http://github.com/Plasmoxy">Plasmoxy</a>
 * @version 1.2
 */

public final class CVUtility {
    
    /**
     * Converts and OpenCV Mat to JavaFX Image
     *
     * Dependent on matToBufferedImage method
     * @param frame the opencv Mat
     * @return javafx image
     */
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generic method which sets a JavaFX property on FX Thread through Platform.runLater
     *
     * @param property Property which is set.
     * @param value Value of the property.
     * @param <T> Type of value.
     */
    public static <T> void setProperty(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }
    
    /**
     * Effective converter from OpenCV mat to BufferedImage
     * @param original OpenCV Mat
     * @return converted BufferedImage
     */
    private static BufferedImage matToBufferedImage(Mat original) {
        // initController
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        image = new BufferedImage(
                width,
                height,
                original.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY
        );

        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}