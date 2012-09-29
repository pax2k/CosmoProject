package no.pax.cosmo.Client;
/**
 * Copyright (c) 2006 - 2012 Smaxe Ltd (www.smaxe.com).
 * All rights reserved.
 */


import com.smaxe.uv.media.VideoFrameFactory;
import com.smaxe.uv.media.core.VideoFrame;
import com.smaxe.uv.media.swing.JVideoScreen;
import com.smaxe.uv.na.WebcamFactory;
import com.smaxe.uv.na.webcam.IWebcam;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class WebCam  {

    final AtomicReference<JFrame> frameRef = new AtomicReference<JFrame>();
    final AtomicReference<VideoFrame> lastFrameRef = new AtomicReference<VideoFrame>();

    public WebCam() {
        final JFrame frame = new JFrame();
        final IWebcam webcam = WebcamFactory.getWebcams(frame, "jitsi").get(0);
        final JVideoScreen videoScreen = new JVideoScreen(new Dimension(200, 150), false);

        new Thread(new Runnable() {
            public void run() {
                try {
                    webcam.setFrameFormat(new IWebcam.FrameFormat(160,120));
                    webcam.open(new IWebcam.FrameFormat(160, 120), new IWebcam.IListener() {
                        private VideoFrame lastFrame = new VideoFrame(0, 0, null);

                        public void onVideoFrame(final VideoFrame frame) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    videoScreen.setFrame(frame);

                                    if (lastFrame.width != frame.width || lastFrame.height != frame.height) {
                                        final JFrame frame = frameRef.get();
                                        if (frame != null) frame.pack();
                                    }

                                    lastFrame = frame;
                                    lastFrameRef.set(lastFrame);
                                }
                            });
                        }
                    });

                    webcam.startCapture();

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            frame.setSize(200, 150);
                            frameRef.set(frame);
                            frame.getContentPane().setLayout(new BorderLayout());
                            frame.getContentPane().add(videoScreen, BorderLayout.CENTER);
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.setResizable(false);
                            frame.setTitle(webcam.getName());
                            frame.pack();
                            frame.setVisible(true);
                        }
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());

                }
            }
        }).start();
    }

    public boolean saveSnapShot() {
        final File file = new File("ray.jpg");
        final VideoFrame clone = VideoFrameFactory.clone(lastFrameRef.get());

        try {
            final BufferedImage bufferedImage = VideoFrameFactory.toBufferedImage(clone);
            final BufferedImage scaledImage = resizeImage(bufferedImage, bufferedImage.getType());
            ImageIO.write(scaledImage, "jpg", file);

            return true;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type){
        BufferedImage resizedImage = new BufferedImage(200, 150, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 200, 150, null);
        g.dispose();

        return resizedImage;
    }
}