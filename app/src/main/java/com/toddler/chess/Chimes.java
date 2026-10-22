package com.toddler.chess;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/** Soft bell tones synthesised at startup, so the app ships without any audio files. */
final class Chimes {
    // C major pentatonic: always sounds pleasant, whatever order a toddler taps in.
    private static final double[] NOTES = {523.25, 587.33, 659.25, 783.99, 880.00, 1046.50};
    private static final int RATE = 22050;

    private final SoundPool pool;
    private final int[] ids = new int[NOTES.length];
    private final Random random = new Random();
    private int step;

    Chimes(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();
        for (int i = 0; i < NOTES.length; i++) {
            File f = new File(context.getCacheDir(), "chime" + i + ".wav");
            try {
                writeBell(f, NOTES[i]);
                ids[i] = pool.load(f.getAbsolutePath(), 1);
            } catch (IOException e) {
                ids[i] = 0;
            }
        }
    }

    /** A random happy note. */
    void ding() {
        play(random.nextInt(NOTES.length));
    }

    /** Notes that climb up the scale on each call. */
    void next() {
        play(step);
        step = (step + 1) % NOTES.length;
    }

    /** The lowest note, for gentle feedback. */
    void low() {
        play(0);
    }

    private void play(int i) {
        if (ids[i] != 0) pool.play(ids[i], 0.5f, 0.5f, 1, 0, 1f);
    }

    void release() {
        pool.release();
    }

    private static void writeBell(File f, double freq) throws IOException {
        int n = (int) (RATE * 0.7);
        byte[] data = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            double env = Math.min(1.0, t / 0.005) * Math.exp(-t * 6.0);
            double s = Math.sin(2 * Math.PI * freq * t)
                    + 0.4 * Math.sin(2 * Math.PI * 2 * freq * t)
                    + 0.15 * Math.sin(2 * Math.PI * 3 * freq * t);
            short v = (short) (env * s / 1.55 * 0.8 * Short.MAX_VALUE);
            data[2 * i] = (byte) v;
            data[2 * i + 1] = (byte) (v >> 8);
        }
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(wavHeader(data.length));
            out.write(data);
        } finally {
            out.close();
        }
    }

    private static byte[] wavHeader(int dataLen) {
        byte[] h = new byte[44];
        putStr(h, 0, "RIFF");
        putInt(h, 4, 36 + dataLen);
        putStr(h, 8, "WAVE");
        putStr(h, 12, "fmt ");
        putInt(h, 16, 16);
        putShort(h, 20, 1);          // PCM
        putShort(h, 22, 1);          // mono
        putInt(h, 24, RATE);
        putInt(h, 28, RATE * 2);     // byte rate
        putShort(h, 32, 2);          // block align
        putShort(h, 34, 16);         // bits per sample
        putStr(h, 36, "data");
        putInt(h, 40, dataLen);
        return h;
    }

    private static void putStr(byte[] b, int off, String s) {
        for (int i = 0; i < s.length(); i++) b[off + i] = (byte) s.charAt(i);
    }

    private static void putInt(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
        b[off + 2] = (byte) (v >> 16);
        b[off + 3] = (byte) (v >> 24);
    }

    private static void putShort(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
    }
}
