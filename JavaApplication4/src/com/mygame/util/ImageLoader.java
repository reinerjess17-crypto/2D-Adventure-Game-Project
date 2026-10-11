package com.mygame.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
// A small helper that loads one image from the resources folder.
// "static" means we can call it without creating an object: ImageLoader.load(...)
public class ImageLoader {

    // Returns the image, or null if it could not be loaded
    public static BufferedImage load(String path) {
        try (InputStream stream = ImageLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Image not found: " + path);
            }
            return ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
