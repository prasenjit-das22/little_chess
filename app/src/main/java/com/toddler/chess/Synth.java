package com.toddler.chess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Instrument sounds made from maths, so the app needs no audio files. Every method returns mono
 * samples at {@link #RATE}, roughly in -1..1.
 */
final class Synth {
    static final int RATE = 22050;
    private static final double TAU = Math.PI * 2;

    private Synth() {}

    static double hz(double base, int semitones) {
        return base * Math.pow(2, semitones / 12.0);
    }

    private static float[] buf(double seconds) {
        return new float[(int) (RATE * seconds)];
    }

    /** Scales so the loudest sample is at peak. */
    static float[] normalize(float[] s, float peak) {
        float max = 1e-6f;
        for (float v : s) max = Math.max(max, Math.abs(v));
        float k = peak / max;
        for (int i = 0; i < s.length; i++) s[i] *= k;
        return s;
    }

    /** Short fade in and a smooth fade out, to avoid clicks. */
    private static void edges(float[] s, double attack, double release) {
        int a = Math.max(1, (int) (attack * RATE)), r = Math.max(1, (int) (release * RATE));
        for (int i = 0; i < a && i < s.length; i++) s[i] *= i / (float) a;
        for (int i = 0; i < r && i < s.length; i++) {
            double t = i / (double) r;
            s[s.length - 1 - i] *= (float) (0.5 - 0.5 * Math.cos(Math.PI * t));
        }
    }

    private static void mix(float[] into, float[] add, float gain, int offset) {
        for (int i = 0; i < add.length && i + offset < into.length; i++) into[i + offset] += add[i] * gain;
    }

    // ---- melodic ----

    static float[] piano(double f) {
        float[] s = buf(2.2);
        for (int h = 1; h <= 10; h++) {
            double fh = f * h * Math.sqrt(1 + 0.0004 * h * h);
            if (fh > RATE * 0.45) break;
            double amp = (h == 1 ? 1.0 : 0.8 / Math.pow(h, 1.3)) * (h == 2 ? 1.3 : 1);
            double decay = 1.2 + 0.7 * h + f / 500.0;
            double w = TAU * fh / RATE, env = amp, k = Math.exp(-decay / RATE);
            for (int i = 0; i < s.length; i++) {
                s[i] += (float) (env * Math.sin(w * i));
                env *= k;
            }
        }
        Random r = new Random(11);
        for (int i = 0; i < RATE * 0.012; i++) s[i] += (float) ((r.nextDouble() - 0.5) * 0.25 * (1 - i / (RATE * 0.012)));
        edges(s, 0.003, 0.25);
        return normalize(s, 0.8f);
    }

    static float[] harmonium(double f) {
        float[] s = buf(1.4);
        for (int reed = 0; reed < 2; reed++) {
            double fr = f * (reed == 0 ? 1.0 : 1.0035);
            for (int h = 1; h <= 14; h++) {
                if (fr * h > RATE * 0.45) break;
                double amp = (h % 2 == 1 ? 1.0 : 0.6) / Math.pow(h, 0.95);
                double w = TAU * fr * h / RATE;
                for (int i = 0; i < s.length; i++) s[i] += (float) (amp * Math.sin(w * i + reed));
            }
        }
        edges(s, 0.06, 0.3);
        return normalize(s, 0.7f);
    }

    static float[] flute(double f) {
        float[] s = buf(1.2);
        Random r = new Random(5);
        double phase = 0, noise = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double vib = 1 + 0.006 * Math.sin(TAU * 5 * t) * Math.min(1, t / 0.35);
            phase += TAU * f * vib / RATE;
            noise += 0.15 * ((r.nextDouble() * 2 - 1) - noise);
            double chiff = t < 0.05 ? (1 - t / 0.05) * 0.5 : 0;
            s[i] = (float) (Math.sin(phase) + 0.2 * Math.sin(2 * phase) + 0.07 * Math.sin(3 * phase)
                    + noise * (0.25 + chiff * 3));
        }
        edges(s, 0.08, 0.3);
        return normalize(s, 0.75f);
    }

    static float[] violin(double f) {
        float[] s = buf(1.3);
        Random r = new Random(9);
        double phase = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double vib = 1 + 0.007 * Math.sin(TAU * 5.5 * t) * Math.min(1, Math.max(0, t - 0.15) / 0.3);
            phase += TAU * f * vib / RATE;
            double v = 0;
            for (int h = 1; h <= 14; h++) {
                if (f * h > RATE * 0.45) break;
                double formant = 1 + 1.2 * Math.exp(-Math.pow((f * h - 2800) / 1200.0, 2));
                v += Math.sin(h * phase) / h * formant;
            }
            s[i] = (float) (v + (r.nextDouble() - 0.5) * 0.08);
        }
        edges(s, 0.12, 0.3);
        return normalize(s, 0.7f);
    }

    static float[] trumpet(double f) {
        float[] s = buf(1.0);
        double phase = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double bright = Math.min(1, t / 0.06);
            double fi = f * (1 - 0.03 * Math.exp(-t / 0.025)) * (1 + 0.004 * Math.sin(TAU * 5 * t) * Math.min(1, t / 0.4));
            phase += TAU * fi / RATE;
            double v = 0;
            for (int h = 1; h <= 12; h++) {
                if (f * h > RATE * 0.45) break;
                v += Math.sin(h * phase) * Math.pow(1.0 / h, 1.6 - 0.9 * bright);
            }
            s[i] = (float) v;
        }
        edges(s, 0.03, 0.2);
        return normalize(s, 0.7f);
    }

    static float[] xylophone(double f) {
        float[] s = buf(1.2);
        double[][] modes = {{1, 1, 6}, {3.93, 0.3, 18}, {9.2, 0.08, 40}};
        for (double[] m : modes) {
            if (f * m[0] > RATE * 0.45) continue;
            double w = TAU * f * m[0] / RATE, env = m[1], k = Math.exp(-m[2] / RATE);
            for (int i = 0; i < s.length; i++) {
                s[i] += (float) (env * Math.sin(w * i));
                env *= k;
            }
        }
        Random r = new Random(3);
        for (int i = 0; i < RATE * 0.004; i++) s[i] += (float) ((r.nextDouble() - 0.5) * 0.4);
        edges(s, 0.001, 0.1);
        return normalize(s, 0.8f);
    }

    /** A Risset-style bell: inharmonic partials that each fade at their own speed. */
    static float[] bell(double f) {
        float[] s = buf(2.6);
        double[] ratio = {0.56, 0.56, 0.92, 0.92, 1.19, 1.7, 2.0, 2.74, 3.0, 3.76, 4.07};
        double[] offset = {0, 1, 0, 1.7, 0, 0, 0, 0, 0, 0, 0};
        double[] amp = {1, 0.67, 1, 1.8, 2.67, 1.67, 1.46, 1.33, 1.33, 1, 1.33};
        double[] dur = {1, 0.9, 0.65, 0.55, 0.325, 0.35, 0.25, 0.2, 0.15, 0.1, 0.075};
        for (int p = 0; p < ratio.length; p++) {
            double fp = f * ratio[p] + offset[p];
            if (fp > RATE * 0.45) continue;
            double w = TAU * fp / RATE, env = amp[p], k = Math.exp(-4.0 / (dur[p] * 2.6) / RATE);
            for (int i = 0; i < s.length; i++) {
                s[i] += (float) (env * Math.sin(w * i));
                env *= k;
            }
        }
        edges(s, 0.002, 0.3);
        return normalize(s, 0.75f);
    }

    /** Karplus-Strong plucked string with fractional-delay tuning. */
    static float[] pluck(double f, double seconds, double brightness, double t60, long seed) {
        float[] out = buf(seconds);
        // The loop delays by n - 0.5 samples (read-ahead averaging) plus the all-pass fraction.
        double period = RATE / f + 0.5;
        int n = Math.max(2, (int) Math.floor(period));
        double frac = period - n;
        double c = (1 - frac) / (1 + frac);
        double loss = Math.pow(0.001, 1.0 / (t60 * f));
        float[] line = new float[n];
        Random r = new Random(seed);
        double prev = 0, mean = 0;
        for (int i = 0; i < n; i++) {
            prev += brightness * ((r.nextDouble() * 2 - 1) - prev);
            line[i] = (float) prev;
            mean += prev;
        }
        mean /= n;
        for (int i = 0; i < n; i++) line[i] -= (float) mean;
        int idx = 0;
        double apIn = 0, apOut = 0;
        for (int i = 0; i < out.length; i++) {
            double cur = line[idx], next = line[(idx + 1) % n];
            double avg = loss * 0.5 * (cur + next);
            double ap = c * avg + apIn - c * apOut;
            apIn = avg;
            apOut = ap;
            line[idx] = (float) ap;
            out[i] = (float) cur;
            idx = (idx + 1) % n;
        }
        return out;
    }

    static float[] guitar(double f) {
        float[] s = pluck(f, 2.6, 0.55, 3.2, (long) f);
        edges(s, 0.002, 0.3);
        return normalize(s, 0.8f);
    }

    static float[] sitar(double f) {
        float[] s = pluck(f, 2.8, 0.95, 3.5, (long) f);
        mix(s, pluck(f * 2, 2.8, 1.0, 2.5, (long) f + 1), 0.3f, 0);
        normalize(s, 1f);
        // The jawari bridge makes the string buzz: a soft clip brings out bright overtones.
        for (int i = 0; i < s.length; i++) {
            double x = s[i];
            s[i] = (float) (Math.tanh(2.2 * x) + 0.25 * x * Math.abs(x));
        }
        edges(s, 0.002, 0.35);
        return normalize(s, 0.75f);
    }

    /** One plucked string whose pitch springs up a little, like squeezing the ektara's neck. */
    static float[] ektara(double f) {
        float[] src = pluck(f, 2.0, 0.75, 2.0, (long) f);
        float[] s = buf(1.8);
        double pos = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            int k = (int) pos;
            if (k + 1 >= src.length) break;
            double fr = pos - k;
            s[i] = (float) (src[k] * (1 - fr) + src[k + 1] * fr);
            pos += 1 - 0.035 * Math.exp(-t / 0.12);
        }
        edges(s, 0.002, 0.3);
        return normalize(s, 0.8f);
    }

    // ---- drums ----

    private static float[] modes(double seconds, double[] freqs, double[] amps, double[] decays) {
        float[] s = buf(seconds);
        for (int m = 0; m < freqs.length; m++) {
            if (freqs[m] > RATE * 0.45) continue;
            double w = TAU * freqs[m] / RATE, env = amps[m], k = Math.exp(-decays[m] / RATE);
            for (int i = 0; i < s.length; i++) {
                s[i] += (float) (env * Math.sin(w * i));
                env *= k;
            }
        }
        return s;
    }

    private static void noiseBurst(float[] s, double seconds, double gain, double lowpass, long seed) {
        Random r = new Random(seed);
        int n = (int) (seconds * RATE);
        double y = 0;
        for (int i = 0; i < n && i < s.length; i++) {
            y += lowpass * ((r.nextDouble() * 2 - 1) - y);
            s[i] += (float) (y * gain * (1 - i / (double) n));
        }
    }

    /** A drum whose pitch slides from f0 towards f1. */
    private static float[] glide(double seconds, double f0, double f1, double glideTime, double decay, double second) {
        float[] s = buf(seconds);
        double phase = 0, env = 1, k = Math.exp(-decay / RATE);
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double f = f1 + (f0 - f1) * Math.exp(-t / glideTime);
            phase += TAU * f / RATE;
            s[i] = (float) (env * (Math.sin(phase) + second * Math.sin(2 * phase)));
            env *= k;
        }
        return s;
    }

    static float[] tablaNa() {
        double f = 330;
        float[] s = modes(0.8, new double[]{f, 2 * f, 3 * f, 4 * f, 5 * f},
                new double[]{1, 0.6, 0.45, 0.25, 0.15}, new double[]{8, 10, 13, 17, 22});
        noiseBurst(s, 0.005, 0.6, 0.9, 1);
        edges(s, 0.0005, 0.1);
        return normalize(s, 0.75f);
    }

    static float[] tablaTin() {
        double f = 330;
        float[] s = modes(1.1, new double[]{f, 2 * f, 3 * f}, new double[]{1, 0.25, 0.12}, new double[]{3.5, 6, 9});
        noiseBurst(s, 0.004, 0.3, 0.6, 2);
        edges(s, 0.001, 0.15);
        return normalize(s, 0.7f);
    }

    static float[] tablaGe() {
        float[] s = glide(1.1, 92, 132, 0.12, 3.2, 0.25);
        noiseBurst(s, 0.012, 0.35, 0.15, 3);
        edges(s, 0.002, 0.15);
        return normalize(s, 0.85f);
    }

    static float[] tablaKe() {
        float[] s = modes(0.25, new double[]{80, 160}, new double[]{1, 0.3}, new double[]{30, 40});
        noiseBurst(s, 0.03, 0.8, 0.3, 4);
        edges(s, 0.001, 0.05);
        return normalize(s, 0.7f);
    }

    static float[] dhakBass() {
        float[] s = glide(1.3, 78, 55, 0.08, 4.5, 0.2);
        mix(s, modes(1.3, new double[]{140}, new double[]{0.35}, new double[]{8}), 1, 0);
        noiseBurst(s, 0.015, 0.6, 0.4, 5);
        edges(s, 0.001, 0.2);
        return normalize(s, 0.9f);
    }

    /** "Kur": a quick roll of three light hits on the drum. */
    static float[] dhakKur() {
        float[] s = buf(0.45);
        for (int hit = 0; hit < 3; hit++) {
            float[] h = modes(0.3, new double[]{230, 380}, new double[]{1, 0.4}, new double[]{22, 30});
            noiseBurst(h, 0.02, 0.9, 0.5, 6 + hit);
            mix(s, h, (float) (1 - hit * 0.25), (int) (hit * 0.055 * RATE));
        }
        edges(s, 0.001, 0.08);
        return normalize(s, 0.75f);
    }

    /** The kansor, a small bronze gong played along with the dhak. */
    static float[] kansor() {
        double f = 520;
        float[] s = modes(1.8, new double[]{f, f * 1.52, f * 2.34, f * 3.17, f * 4.11, f * 5.03},
                new double[]{0.8, 1, 0.7, 0.5, 0.4, 0.3}, new double[]{2.5, 3.2, 4, 5.5, 7, 9});
        noiseBurst(s, 0.01, 0.8, 0.95, 9);
        edges(s, 0.0005, 0.3);
        return normalize(s, 0.6f);
    }

    static float[] kick() {
        float[] s = glide(0.6, 140, 45, 0.04, 7, 0);
        noiseBurst(s, 0.006, 0.4, 0.5, 10);
        edges(s, 0.0005, 0.1);
        return normalize(s, 0.95f);
    }

    private static void highNoise(float[] s, double gain, double decay, long seed) {
        Random r = new Random(seed);
        double prev = 0, env = gain, k = Math.exp(-decay / RATE);
        for (int i = 0; i < s.length; i++) {
            double x = r.nextDouble() * 2 - 1;
            s[i] += (float) ((x - prev) * env);
            prev = x;
            env *= k;
        }
    }

    static float[] snare() {
        float[] s = modes(0.4, new double[]{190, 330}, new double[]{0.7, 0.3}, new double[]{22, 28});
        highNoise(s, 0.6, 16, 11);
        edges(s, 0.0005, 0.08);
        return normalize(s, 0.85f);
    }

    static float[] hihat() {
        float[] s = buf(0.18);
        highNoise(s, 1, 40, 12);
        edges(s, 0.0005, 0.03);
        return normalize(s, 0.55f);
    }

    static float[] tom() {
        float[] s = glide(0.8, 150, 105, 0.06, 6, 0.1);
        noiseBurst(s, 0.008, 0.4, 0.5, 13);
        edges(s, 0.0005, 0.1);
        return normalize(s, 0.85f);
    }

    static float[] crash() {
        float[] s = buf(2.2);
        highNoise(s, 1, 2.4, 14);
        mix(s, modes(2.2, new double[]{470, 723, 1081, 1540, 2210}, new double[]{0.08, 0.07, 0.06, 0.05, 0.04},
                new double[]{2, 2.5, 3, 3.5, 4}), 1, 0);
        edges(s, 0.0005, 0.3);
        return normalize(s, 0.55f);
    }

    static float[] clap() {
        float[] s = buf(0.4);
        Random r = new Random(15);
        int[] starts = {0, (int) (0.011 * RATE), (int) (0.022 * RATE)};
        for (int b = 0; b < starts.length; b++) {
            double env = b == 2 ? 1 : 0.7, k = Math.exp(-(b == 2 ? 14 : 120.0) / RATE);
            double prev = 0;
            for (int i = starts[b]; i < s.length; i++) {
                double x = r.nextDouble() * 2 - 1;
                s[i] += (float) ((x - 0.6 * prev) * env);
                prev = x;
                env *= k;
            }
        }
        edges(s, 0.0005, 0.05);
        return normalize(s, 0.7f);
    }

    /** The shankh, a conch shell: a horn-like note that swells, wavers and fades. */
    static float[] shankh() {
        double f = 233;
        float[] s = buf(2.6);
        Random r = new Random(16);
        double phase = 0, noise = 0;
        double[] amps = {1, 0.85, 0.55, 0.42, 0.28, 0.2, 0.12, 0.08};
        for (int i = 0; i < s.length; i++) {
            double t = i / (double) RATE;
            double swell = Math.min(1, t / 0.35);
            double fi = f * (0.96 + 0.04 * swell) * (1 + 0.005 * Math.sin(TAU * 4.5 * t));
            phase += TAU * fi / RATE;
            double v = 0;
            for (int h = 0; h < amps.length; h++) v += amps[h] * Math.pow(swell, h * 0.4) * Math.sin((h + 1) * phase);
            noise += 0.1 * ((r.nextDouble() * 2 - 1) - noise);
            s[i] = (float) (v * swell + noise * 0.6);
        }
        edges(s, 0.05, 0.5);
        return normalize(s, 0.75f);
    }

    // ---- files ----

    static void writeWav(File f, float[] samples) throws IOException {
        byte[] data = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            float v = Math.max(-1f, Math.min(1f, samples[i]));
            short x = (short) (v * Short.MAX_VALUE);
            data[2 * i] = (byte) x;
            data[2 * i + 1] = (byte) (x >> 8);
        }
        File tmp = new File(f.getPath() + ".tmp");
        FileOutputStream out = new FileOutputStream(tmp);
        try {
            out.write(header(data.length));
            out.write(data);
        } finally {
            out.close();
        }
        if (!tmp.renameTo(f)) throw new IOException("rename failed");
    }

    private static byte[] header(int dataLen) {
        byte[] h = new byte[44];
        put(h, 0, "RIFF");
        putInt(h, 4, 36 + dataLen);
        put(h, 8, "WAVE");
        put(h, 12, "fmt ");
        putInt(h, 16, 16);
        h[20] = 1;               // PCM
        h[22] = 1;               // mono
        putInt(h, 24, RATE);
        putInt(h, 28, RATE * 2); // byte rate
        h[32] = 2;               // block align
        h[34] = 16;              // bits per sample
        put(h, 36, "data");
        putInt(h, 40, dataLen);
        return h;
    }

    private static void put(byte[] b, int off, String s) {
        for (int i = 0; i < s.length(); i++) b[off + i] = (byte) s.charAt(i);
    }

    private static void putInt(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
        b[off + 2] = (byte) (v >> 16);
        b[off + 3] = (byte) (v >> 24);
    }
}
