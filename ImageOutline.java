import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Area;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Date;
import javax.swing.*;

/* Motorcycle image courtesy of ShutterStock
http://www.shutterstock.com/pic-13585165/stock-vector-travel-motorcycle-silhouette.html */
class ImageOutline {

    public static int[][] getOutline(BufferedImage image, boolean include, int tolerance) {
        int[][] outline = new int[image.getWidth()][image.getHeight()];
        for (int x=0; x<image.getWidth(); x++) {
            for (int y=0; y<image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x,y));
                Color left = null;
                Color top = null;
                if(x != 0){
                   left = new Color(image.getRGB(x - 1,y));
                }
                if(y != 0){
                    top = new Color(image.getRGB(x,y - 1));
                }

                if (!(top == null || isIncluded(top, pixel, tolerance))  && !(left == null || isIncluded(left, pixel, tolerance))) {
                    drawOutline(outline, x,y);
                }
            }
        }
        return outline;
    }

    public static boolean isIncluded(Color target, Color pixel, int tolerance) {
        int rT = target.getRed();
        int gT = target.getGreen();
        int bT = target.getBlue();
        int rP = pixel.getRed();
        int gP = pixel.getGreen();
        int bP = pixel.getBlue();
        return(
                (rP-tolerance<=rT) && (rT<=rP+tolerance) &&
                        (gP-tolerance<=gT) && (gT<=gP+tolerance) &&
                        (bP-tolerance<=bT) && (bT<=bP+tolerance) );
    }

    public static void drawOutline(int[][] outline, int x, int y) {
        outline[x][y] = 1;
        return;
    }

}