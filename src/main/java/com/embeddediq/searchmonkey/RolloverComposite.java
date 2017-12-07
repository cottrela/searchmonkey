/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author cottr
 */
public class RolloverComposite  implements Composite {

    private static RolloverComposite instance = null;

    private RolloverComposite() {
        // Only static singleton instantiation;
    }

    public static RolloverComposite getInstance() {
        if(instance == null) {
            instance = new RolloverComposite();
        }
        return instance;
    }

    /**
     *
     * @param srcColorModel
     * @param dstColorModel
     * @param hints
     * @return
     */
    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel, RenderingHints hints) {

        return new RolloverCompositeContext();

    }

    private static class RolloverCompositeContext implements CompositeContext {

        @Override
        public void dispose() {
            // Do nothing

        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            for (int y = 0; y < dstOut.getHeight(); y++) {
                for (int x = 0; x < dstOut.getWidth(); x++) {
                    // Get the source pixels
                    int[] srcPixels = new int[4];
                    src.getPixel(x, y, srcPixels);
                    // Ignore transparent pixels
                    //if (srcPixels[3] != 0) {
                    // Lighten each color by 1/2, and increasing the
                    // blue
                    srcPixels[0] = srcPixels[0] / 2;
                    srcPixels[1] = srcPixels[1] / 2;
                    srcPixels[2] = srcPixels[2] / 2 + 68;
                    // srcPixels[3] = 128; // Force non-trans

                    dstOut.setPixel(x, y, srcPixels);
                    //}
                }
            }
        }

    }
}       

