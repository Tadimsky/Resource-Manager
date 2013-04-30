package resourcemanager.loaders;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import resourcemanager.ResourceLoader;


public class ImageLoader extends ResourceLoader {

    public ImageLoader () {
        super();
        Set<String> myUnique = new HashSet<String>(Arrays.asList(ImageIO.getReaderFormatNames()));
        for (String ext : myUnique) {
            this.registerExtension(ext);
        }
    }

    @Override
    public Class<?> getFileType () {
        return BufferedImage.class;
    }

    @Override
    public Object loadFile (URL path) {
        try {
            BufferedImage image = ImageIO.read(path);
            return toCompatibleImage(image);
        }
        catch (IOException e) {
            System.out.println("This is not an image.");
            return null;
        }
    }

    // From: http://stackoverflow.com/questions/196890/java2d-performance-issues
    private BufferedImage toCompatibleImage (BufferedImage image) {
        // obtain the current system graphical settings
        GraphicsConfiguration gfxconfig =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfxconfig.getColorModel())) {
            return image;
        }
        // image is not optimized, so create a new image that is
        BufferedImage newimage =
                gfxconfig.createCompatibleImage(image.getWidth(), image.getHeight(),
                                                 image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) newimage.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return newimage;
    }

}
