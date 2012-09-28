package no.pax.cosmo.Client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.File;

/**
 * Created: rak
 * Date: 27.09.12
 */
public class BarkDetection {
    final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
    final static float MAX_8_BITS_UNSIGNED = 0xff;
    final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
    final static float MAX_16_BITS_UNSIGNED = 0xffff;

    BarkListener listener;

    private float readLevel;
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private Clip clip;
    private int barkCounter = 0;

    public BarkDetection(BarkListener listener) {
        this.listener = listener;

        initPlayer();
        captureAudio();
    }

    private void playMusic() {
        clip.setFramePosition(0);
        clip.start();
    }

    private void initPlayer() {
        try {
            final File absoluteFile = new File("song2.wav").getAbsoluteFile();
            System.out.println(absoluteFile.getAbsolutePath());
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(absoluteFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    private void captureAudio() {
        try {
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo =
                    new DataLine.Info(
                            TargetDataLine.class,
                            audioFormat);

            targetDataLine = (TargetDataLine)
                    AudioSystem.getLine(dataLineInfo);

            new CaptureThread().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }
//=============================================//

    class CaptureThread extends Thread {
        public void run() {
            try {
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                int bufferSize = (int) audioFormat.getSampleRate()
                        * audioFormat.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                boolean readMic = true;

                while (readMic) {
                    int count =
                            targetDataLine.read(buffer, 0, buffer.length);
                    calculateLevel(buffer, 0, 0);

                    if (readLevel > 0.8f) {
                        barkCounter ++;
                        listener.newNumberOfBarks();

                        if (clip != null && !clip.isRunning()) {
                            System.out.println("Bark detected and no player started. Play music");
                            playMusic();
                        } else {
                            System.out.println("Bark detected while music is playing, ignore this. ");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateLevel(byte[] buffer,
                                int readPoint,
                                int leftOver) {
        int max = 0;
        boolean use16Bit = (audioFormat.getSampleSizeInBits() == 16);
        boolean signed = (audioFormat.getEncoding() ==
                AudioFormat.Encoding.PCM_SIGNED);
        boolean bigEndian = (audioFormat.isBigEndian());
        if (use16Bit) {
            for (int i = readPoint; i < buffer.length - leftOver; i += 2) {
                int value = 0;
                // deal with endianness
                int hiByte = (bigEndian ? buffer[i] : buffer[i + 1]);
                int loByte = (bigEndian ? buffer[i + 1] : buffer[i]);
                if (signed) {
                    short shortVal = (short) hiByte;
                    shortVal = (short) ((shortVal << 8) | (byte) loByte);
                    value = shortVal;
                } else {
                    value = (hiByte << 8) | loByte;
                }
                max = Math.max(max, value);
            } // for
        } else {
            // 8 bit - no endianness issues, just sign
            for (int i = readPoint; i < buffer.length - leftOver; i++) {
                int value = 0;
                if (signed) {
                    value = buffer[i];
                } else {
                    short shortVal = 0;
                    shortVal = (short) (shortVal | buffer[i]);
                    value = shortVal;
                }
                max = Math.max(max, value);
            } // for
        } // 8 bit
        // express max as float of 0.0 to 1.0 of max value
        // of 8 or 16 bits (signed or unsigned)
        if (signed) {
            if (use16Bit) {
                readLevel = (float) max / MAX_16_BITS_SIGNED;
            } else {
                readLevel = (float) max / MAX_8_BITS_SIGNED;
            }
        } else {
            if (use16Bit) {
                readLevel = (float) max / MAX_16_BITS_UNSIGNED;
            } else {
                readLevel = (float) max / MAX_8_BITS_UNSIGNED;
            }
        }
    } // calculateLevel

    public int getBarkCounter() {
        return barkCounter;
    }
}