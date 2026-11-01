package com.toddler.chess;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the sounds of the instrument on screen. They are synthesised once on a background thread,
 * cached as small WAV files and loaded into a SoundPool.
 */
final class SoundBank {
    private static final String VERSION = "v1";

    private final SoundPool pool;
    private final File dir;
    private final Set<Integer> loaded = new HashSet<>();
    private int[] ids = new int[0];
    private int generation;

    SoundBank(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build();
        pool.setOnLoadCompleteListener((p, id, status) -> {
            if (status == 0) {
                synchronized (loaded) {
                    loaded.add(id);
                }
            }
        });
        dir = context.getCacheDir();
    }

    /** Starts preparing the sounds for an instrument, replacing the previous one. */
    synchronized void load(final int kind) {
        unloadAll();
        final int gen = ++generation;
        final int n = Instrument.sounds(kind);
        ids = new int[n];
        Thread t = new Thread(() -> {
            for (int i = 0; i < n; i++) {
                if (!current(gen)) return;
                File f = new File(dir, "snd_" + VERSION + "_" + kind + "_" + i + ".wav");
                try {
                    if (!f.exists()) Synth.writeWav(f, Instrument.render(kind, i));
                } catch (IOException e) {
                    continue;
                }
                int id = pool.load(f.getAbsolutePath(), 1);
                synchronized (SoundBank.this) {
                    if (gen == generation) ids[i] = id;
                    else pool.unload(id);
                }
            }
        }, "sounds");
        t.setPriority(Thread.MIN_PRIORITY + 1);
        t.start();
    }

    private synchronized boolean current(int gen) {
        return gen == generation;
    }

    synchronized boolean ready(int i) {
        if (i < 0 || i >= ids.length || ids[i] == 0) return false;
        synchronized (loaded) {
            return loaded.contains(ids[i]);
        }
    }

    synchronized void play(int i, float volume) {
        if (!ready(i)) return;
        pool.play(ids[i], volume, volume, 1, 0, 1f);
    }

    synchronized void unloadAll() {
        generation++;
        for (int id : ids) {
            if (id != 0) {
                pool.unload(id);
                synchronized (loaded) {
                    loaded.remove(id);
                }
            }
        }
        ids = new int[0];
    }

    void release() {
        pool.release();
    }
}
