package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.SparseIntArray;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * One playable instrument. Everything is drawn for a wide play area; on a tall screen the area is
 * turned sideways so piano keys run from bottom (low) to top (high), and labels stay upright.
 *
 * The bottom bar has a song (or rhythm) picker, a play button that performs it while lighting up
 * the keys, and for melodic instruments a "learn" button: a star shows which key to press next.
 */
final class InstrumentView extends PlayView {
    private static final int TEMPO_MS = 420;

    private final int kind, layout;
    private final Instrument.Pad[] pads;
    private final Instrument.Rhythm[] rhythms;
    private final boolean melodic;
    private final SoundBank bank;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final RectF stage = new RectF();
    private final RectF chipRect = new RectF();
    private boolean rotated;
    private float W, H;

    private final long[] pressAt = new long[16];
    private final SparseIntArray pointerZone = new SparseIntArray();
    private final List<float[]> floaters = new ArrayList<>();
    private final List<float[]> ripples = new ArrayList<>();

    private int choice;          // song or rhythm index
    private boolean learning;
    private int learnPos;
    private boolean playing;
    private int step;
    private int loops;
    private String[] bol;
    private long bolAt;
    private long openedAt;

    private final Runnable stepper = this::playStep;

    InstrumentView(Context context, int kind) {
        super(context);
        this.kind = kind;
        layout = Instrument.layout(kind);
        pads = Instrument.pads(kind);
        rhythms = Instrument.rhythms(kind);
        melodic = Instrument.melodic(kind);
        bank = activity.sounds;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
    }

    void start() {
        openedAt = now();
        bank.load(kind);
        activity.speaker.say(Lang.t(Instrument.ABOUT[kind]));
    }

    void stop() {
        stopPlaying();
        bank.unloadAll();
    }

    /** The app went to the background: stop any song or rhythm that is playing. */
    void pause() {
        stopPlaying();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(stepper);
    }

    // ---- geometry ----

    private float headerH() {
        return dp(72);
    }

    private float barH() {
        return dp(78);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stage.set(dp(10), headerH(), w - dp(10), h - barH());
        rotated = stage.width() < stage.height() * 0.9f;
        W = rotated ? stage.height() : stage.width();
        H = rotated ? stage.width() : stage.height();
    }

    /** Screen point to play-area coordinates. */
    private float[] toLocal(float x, float y) {
        return rotated ? new float[]{stage.bottom - y, x - stage.left} : new float[]{x - stage.left, y - stage.top};
    }

    private float[] toScreen(float lx, float ly) {
        return rotated ? new float[]{stage.left + ly, stage.bottom - lx} : new float[]{stage.left + lx, stage.top + ly};
    }

    /** Text that stays upright even when the play area is turned. */
    private void upright(Canvas c, String s, float x, float y, float size, int color, float maxW) {
        if (rotated) {
            c.save();
            c.rotate(90, x, y);
            Art.text(c, s, x, y, size, color, maxW);
            c.restore();
        } else {
            Art.text(c, s, x, y, size, color, maxW);
        }
    }

    private String noteName(int degree) {
        return Instrument.SARGAM[Lang.current][degree];
    }

    // ---- zones: which sound is under a point ----

    private int zoneCount() {
        switch (layout) {
            case Instrument.KEYS: return 13;
            case Instrument.STRINGS: return 6;
            case Instrument.PADS: return Instrument.sounds(kind);
            default: return 8;
        }
    }

    private float keyTop() {
        return kind == Instrument.HARMONIUM ? H * 0.2f : H * 0.1f;
    }

    private float keyLeft() {
        return W * 0.03f;
    }

    private float whiteW() {
        return (W - 2 * keyLeft()) / 8f;
    }

    private static final int[] BLACK_AFTER = {0, 1, 3, 4, 5};
    private static final int[] BLACK_NOTE = {1, 3, 6, 8, 10};

    private float holeX(int i) {
        return kind == Instrument.FLUTE ? W * (0.2f + i * 0.105f) : W * (0.11f + i * 0.1f);
    }

    private float neckLeft() {
        return W * 0.03f;
    }

    private float neckZoneW() {
        return (W * 0.70f - neckLeft()) / 8f;
    }

    private float stringY(int i) {
        return H * (0.14f + i * 0.144f);
    }

    private int zoneAt(float lx, float ly) {
        if (lx < 0 || ly < 0 || lx > W || ly > H) return -1;
        switch (layout) {
            case Instrument.KEYS: {
                float ww = whiteW(), left = keyLeft(), top = keyTop(), blackBottom = top + (H - top) * 0.6f;
                if (ly < top) return -1;
                if (ly < blackBottom) {
                    for (int b = 0; b < 5; b++) {
                        float cx = left + (BLACK_AFTER[b] + 1) * ww;
                        if (Math.abs(lx - cx) < ww * 0.32f) return BLACK_NOTE[b];
                    }
                }
                int i = (int) Math.floor((lx - left) / ww);
                if (i < 0 || i > 7) return -1;
                return Instrument.SCALE[i];
            }
            case Instrument.BARS:
                return Math.max(0, Math.min(7, (int) (lx / (W / 8f))));
            case Instrument.HOLES: {
                float half = (holeX(1) - holeX(0)) / 2f;
                for (int i = 0; i < 8; i++) if (Math.abs(lx - holeX(i)) <= half) return i;
                return -1;
            }
            case Instrument.NECK: {
                int i = (int) Math.floor((lx - neckLeft()) / neckZoneW());
                return i >= 0 && i < 8 ? i : -1;
            }
            case Instrument.STRINGS: {
                float gap = stringY(1) - stringY(0);
                int i = Math.round((ly - stringY(0)) / gap);
                return i >= 0 && i < 6 && Math.abs(ly - stringY(i)) < gap * 0.5f ? i : -1;
            }
            default: {
                for (int p = pads.length - 1; p >= 0; p--) {
                    Instrument.Pad pad = pads[p];
                    float dx = (lx - pad.x * W) / (pad.rx * H), dy = (ly - pad.y * H) / (pad.ry * H);
                    float d = dx * dx + dy * dy;
                    if (d <= 1.1f) return pad.inner > 0 && d <= pad.inner * pad.inner ? pad.innerSound : pad.sound;
                }
                return -1;
            }
        }
    }

    /** Centre of a zone in play-area coordinates (for stars and floating notes). */
    private float[] zoneCenter(int zone) {
        switch (layout) {
            case Instrument.KEYS: {
                float ww = whiteW(), left = keyLeft(), top = keyTop();
                for (int b = 0; b < 5; b++) {
                    if (BLACK_NOTE[b] == zone) return new float[]{left + (BLACK_AFTER[b] + 1) * ww, top + (H - top) * 0.35f};
                }
                int i = degreeOf(zone);
                return new float[]{left + (i + 0.5f) * ww, H - ww * 0.62f};
            }
            case Instrument.BARS:
                return new float[]{(zone + 0.5f) * W / 8f, H * 0.5f};
            case Instrument.HOLES:
                return new float[]{holeX(zone), H * 0.42f};
            case Instrument.NECK:
                return new float[]{neckLeft() + (zone + 0.5f) * neckZoneW(), H * 0.5f};
            case Instrument.STRINGS:
                return new float[]{W * 0.3f, stringY(zone)};
            default:
                for (Instrument.Pad p : pads) {
                    if (p.sound == zone) return new float[]{p.x * W, p.y * H};
                    if (p.inner > 0 && p.innerSound == zone) return new float[]{p.x * W, p.y * H};
                }
                return new float[]{W / 2f, H / 2f};
        }
    }

    private static int degreeOf(int chromatic) {
        for (int d = 0; d < 8; d++) if (Instrument.SCALE[d] == chromatic) return d;
        return -1;
    }

    /** Zone for a degree of the scale (0..7). */
    private int zoneForDegree(int degree) {
        return layout == Instrument.KEYS ? Instrument.SCALE[degree] : degree;
    }

    private int colorOf(int zone) {
        if (layout == Instrument.KEYS) {
            int d = degreeOf(zone);
            return d < 0 ? Color.rgb(94, 53, 177) : Instrument.NOTE_COLORS[d];
        }
        if (layout == Instrument.PADS || layout == Instrument.STRINGS) {
            return Instrument.NOTE_COLORS[(zone * 3) % 8];
        }
        return Instrument.NOTE_COLORS[zone];
    }

    // ---- playing ----

    private void hit(int zone, float sx, float sy, boolean byUser) {
        if (zone < 0) return;
        bank.play(zone, 0.95f);
        pressAt[zone] = now();
        floaters.add(new float[]{sx, sy, colorOf(zone), now()});
        if (layout == Instrument.PADS) ripples.add(new float[]{sx, sy, now()});
        if (byUser && learning && melodic) {
            Instrument.Song song = Instrument.SONGS[choice];
            if (learnPos < song.notes.length && zone == zoneForDegree(song.notes[learnPos])) {
                learnPos++;
                if (learnPos == song.notes.length) celebrate();
            }
        }
    }

    private void celebrate() {
        for (int i = 0; i < 4; i++) {
            final int k = i;
            postDelayed(() -> {
                burst(getWidth() * (0.2f + 0.2f * k), getHeight() * 0.4f);
                activity.chimes.next();
            }, i * 180L);
        }
        Instrument.Song song = Instrument.SONGS[choice];
        activity.speaker.say(Lang.t("Well done! You played ", "দারুণ! তুমি বাজালে ", "शाबाश! तुमने बजाया ")
                + Lang.t(song.name) + "!");
        postDelayed(() -> learnPos = 0, 2500);
    }

    private void startPlaying() {
        playing = true;
        step = 0;
        loops = 0;
        removeCallbacks(stepper);
        post(stepper);
    }

    private void stopPlaying() {
        playing = false;
        bol = null;
        removeCallbacks(stepper);
    }

    private void playStep() {
        if (!playing) return;
        if (melodic) {
            Instrument.Song song = Instrument.SONGS[choice];
            if (step >= song.notes.length) {
                stopPlaying();
                return;
            }
            int zone = zoneForDegree(song.notes[step]);
            float[] p = zoneCenter(zone);
            float[] s = toScreen(p[0], p[1]);
            hit(zone, s[0], s[1], false);
            int beats = song.beats[step];
            step++;
            postDelayed(stepper, (long) TEMPO_MS * beats);
            return;
        }
        if (rhythms.length == 0) return;
        Instrument.Rhythm r = rhythms[choice];
        if (step >= r.steps.length) {
            step = 0;
            loops++;
            if (kind == Instrument.SHANKH || loops >= 4) {
                stopPlaying();
                return;
            }
        }
        int[] sounds = r.steps[step];
        for (int i = 0; i < sounds.length; i++) {
            final int zone = sounds[i];
            float[] p = zoneCenter(zone);
            final float[] s = toScreen(p[0], p[1]);
            if (layout == Instrument.STRINGS) {
                postDelayed(() -> hit(zone, s[0], s[1], false), i * 22L);
            } else {
                hit(zone, s[0], s[1], false);
            }
        }
        if (r.bols[step] != null) {
            bol = r.bols[step];
            bolAt = now();
        }
        step++;
        postDelayed(stepper, r.stepMs);
    }

    private int choices() {
        return melodic ? Instrument.SONGS.length : rhythms.length;
    }

    private String choiceName() {
        if (melodic) return Lang.t(Instrument.SONGS[choice].name);
        return rhythms.length > 0 ? Lang.t(rhythms[choice].name) : "";
    }

    // ---- touch ----

    private boolean inBar(float y) {
        return y > getHeight() - barH();
    }

    private void barRects(RectF chip, RectF play, RectF learn) {
        float bh = dp(56), cy = getHeight() - barH() / 2f;
        float chipW = Math.min(dp(240), getWidth() * 0.5f);
        float total = chipW + dp(12) + bh + (melodic ? dp(12) + bh : 0);
        float left = (getWidth() - total) / 2f;
        chip.set(left, cy - bh / 2f, left + chipW, cy + bh / 2f);
        play.set(chip.right + dp(12), cy - bh / 2f, chip.right + dp(12) + bh, cy + bh / 2f);
        learn.set(play.right + dp(12), cy - bh / 2f, play.right + dp(12) + bh, cy + bh / 2f);
    }

    private void onBarTap(float x, float y) {
        RectF play = new RectF(), learn = new RectF();
        barRects(chipRect, play, learn);
        float slop = dp(6);
        if (x > chipRect.left - slop && x < chipRect.right + slop && choices() > 1) {
            stopPlaying();
            choice = (choice + 1) % choices();
            learnPos = 0;
            activity.chimes.next();
            activity.speaker.say(choiceName());
        } else if (x > play.left - slop && x < play.right + slop) {
            if (playing) {
                stopPlaying();
            } else {
                learnPos = 0;
                startPlaying();
            }
        } else if (melodic && x > learn.left - slop && x < learn.right + slop) {
            stopPlaying();
            learning = !learning;
            learnPos = 0;
            activity.chimes.ding();
            if (learning) {
                activity.speaker.say(Lang.t("Follow the star!", "তারাটা দেখে বাজাও!", "तारे को देखकर बजाओ!")
                        + " " + choiceName());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int i = e.getActionIndex();
                float x = e.getX(i), y = e.getY(i);
                if (y < headerH()) {
                    float r = dp(28), m = dp(14) + r;
                    if (action == MotionEvent.ACTION_DOWN && Math.hypot(x - m, y - m) < r * 1.4f) activity.openMusic();
                    return true;
                }
                if (inBar(y)) {
                    if (action == MotionEvent.ACTION_DOWN) onBarTap(x, y);
                    return true;
                }
                float[] l = toLocal(x, y);
                int zone = zoneAt(l[0], l[1]);
                if (zone >= 0) {
                    if (playing && melodic) stopPlaying();
                    hit(zone, x, y, true);
                }
                pointerZone.put(e.getPointerId(i), zone);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (layout == Instrument.PADS) return true;
                for (int i = 0; i < e.getPointerCount(); i++) {
                    int id = e.getPointerId(i);
                    if (pointerZone.indexOfKey(id) < 0) continue;
                    float[] l = toLocal(e.getX(i), e.getY(i));
                    int zone = zoneAt(l[0], l[1]);
                    int last = pointerZone.get(id);
                    if (zone >= 0 && zone != last) {
                        if (layout == Instrument.STRINGS && last >= 0) {
                            int dir = zone > last ? 1 : -1;
                            for (int z = last + dir; z != zone; z += dir) {
                                float[] p = toScreen(l[0], stringY(z));
                                hit(z, p[0], p[1], true);
                            }
                        }
                        hit(zone, e.getX(i), e.getY(i), true);
                        pointerZone.put(id, zone);
                    } else if (zone < 0 && layout == Instrument.STRINGS) {
                        // Between strings: remember nothing so the next string counts.
                    } else if (zone < 0) {
                        pointerZone.put(id, -1);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:
                pointerZone.delete(e.getPointerId(e.getActionIndex()));
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pointerZone.clear();
                return true;
            default:
                return true;
        }
    }

    // ---- drawing ----

    private float pressed(int zone) {
        long t = now() - pressAt[zone];
        return t < 0 || t > 350 ? 0 : 1 - t / 350f;
    }

    @Override
    protected void paint(Canvas c) {
        int theme = Instrument.COLORS[kind];
        backdrop(c, Art.tint(theme, 0.75f), Art.tint(theme, 0.35f));
        float w = getWidth();

        c.save();
        if (rotated) {
            c.translate(stage.left, stage.bottom);
            c.rotate(-90);
        } else {
            c.translate(stage.left, stage.top);
        }
        switch (layout) {
            case Instrument.KEYS: drawKeys(c); break;
            case Instrument.BARS: if (kind == Instrument.BELLS) drawBells(c); else drawXylophone(c); break;
            case Instrument.HOLES: drawHoles(c); break;
            case Instrument.NECK: drawNeck(c); break;
            case Instrument.STRINGS: drawGuitar(c); break;
            default: drawPads(c); break;
        }
        if (learning && melodic && !playing) {
            Instrument.Song song = Instrument.SONGS[choice];
            if (learnPos < song.notes.length) {
                float[] p = zoneCenter(zoneForDegree(song.notes[learnPos]));
                float pulse = 1 + 0.15f * (float) Math.sin(now() / 120.0);
                float size = Math.min(W / 8f, H * 0.3f) * 0.7f * pulse;
                paint.setColor(Color.argb(110, 255, 255, 255));
                c.drawCircle(p[0], p[1], size * 0.75f, paint);
                Art.emoji(c, "⭐", p[0], p[1], size);
            }
        }
        c.restore();

        drawRipples(c);
        drawFloaters(c);

        // Header: home, the instrument's name and its name in the other language.
        float r = dp(28), m = dp(14) + r;
        Art.homeButton(c, m, m, r);
        String name = Lang.t(Instrument.NAMES[kind]);
        String other = Lang.current == Lang.BN ? Instrument.NAMES[kind][Lang.EN] : Instrument.NAMES[kind][Lang.BN];
        float wave = (float) Math.sin(now() / 600.0) * dp(2);
        Art.label(c, name, w / 2f, dp(30) + wave, dp(32), Color.rgb(60, 36, 90));
        Art.text(c, other, w / 2f, dp(58), dp(15), Color.rgb(110, 80, 130), w * 0.5f);
        InstrumentArt.icon(c, kind, w - m, m, r * 1.7f);

        if (bol != null && now() - bolAt < 600) {
            float t = (now() - bolAt) / 600f;
            float size = dp(54) * (1.2f - 0.2f * t);
            float[] p = rotated ? new float[]{stage.centerX(), stage.top + dp(40)} : new float[]{stage.centerX(), stage.top + dp(36)};
            Art.label(c, Lang.t(bol), p[0], p[1], size, Color.rgb(216, 27, 96));
        }

        if (!bank.ready(0) && now() - openedAt > 300) {
            Art.text(c, Lang.t("Tuning up…", "সুর বাঁধছি…", "सुर मिला रहे हैं…"), stage.centerX(), stage.centerY(),
                    dp(18), Color.rgb(110, 80, 130));
        }
        drawBar(c);
    }

    private void drawBar(Canvas c) {
        if (choices() == 0) return;
        RectF play = new RectF(), learn = new RectF();
        barRects(chipRect, play, learn);
        paint.setColor(Color.argb(50, 0, 0, 0));
        chipRect.offset(0, dp(3));
        c.drawRoundRect(chipRect, chipRect.height() / 2, chipRect.height() / 2, paint);
        chipRect.offset(0, -dp(3));
        paint.setColor(Color.WHITE);
        c.drawRoundRect(chipRect, chipRect.height() / 2, chipRect.height() / 2, paint);
        Art.emoji(c, melodic ? "🎵" : "🥁", chipRect.left + chipRect.height() * 0.5f, chipRect.centerY(), chipRect.height() * 0.5f);
        Art.text(c, choiceName(), chipRect.centerX() + chipRect.height() * 0.2f, chipRect.centerY(), dp(18), Art.INK,
                chipRect.width() - chipRect.height() * 1.3f);
        if (choices() > 1) {
            path.reset();
            float ax = chipRect.right - dp(18), ay = chipRect.centerY();
            path.moveTo(ax - dp(5), ay - dp(7));
            path.lineTo(ax + dp(4), ay);
            path.lineTo(ax - dp(5), ay + dp(7));
            path.close();
            paint.setColor(Color.rgb(120, 90, 200));
            c.drawPath(path, paint);
        }

        float pr = play.width() / 2f;
        paint.setColor(Color.argb(50, 0, 0, 0));
        c.drawCircle(play.centerX(), play.centerY() + dp(3), pr, paint);
        paint.setColor(playing ? Color.rgb(255, 112, 67) : Color.rgb(102, 187, 106));
        c.drawCircle(play.centerX(), play.centerY(), pr, paint);
        paint.setColor(Color.WHITE);
        if (playing) {
            c.drawRect(play.centerX() - pr * 0.32f, play.centerY() - pr * 0.32f, play.centerX() + pr * 0.32f,
                    play.centerY() + pr * 0.32f, paint);
        } else {
            path.reset();
            path.moveTo(play.centerX() - pr * 0.28f, play.centerY() - pr * 0.42f);
            path.lineTo(play.centerX() + pr * 0.45f, play.centerY());
            path.lineTo(play.centerX() - pr * 0.28f, play.centerY() + pr * 0.42f);
            path.close();
            c.drawPath(path, paint);
        }

        if (melodic) {
            paint.setColor(Color.argb(50, 0, 0, 0));
            c.drawCircle(learn.centerX(), learn.centerY() + dp(3), pr, paint);
            paint.setColor(learning ? Color.rgb(255, 202, 40) : Color.WHITE);
            c.drawCircle(learn.centerX(), learn.centerY(), pr, paint);
            Art.emoji(c, learning ? "⭐" : "👆", learn.centerX(), learn.centerY(), pr * 1.15f);
        }
    }

    private void drawFloaters(Canvas c) {
        long t = now();
        for (int i = floaters.size() - 1; i >= 0; i--) {
            float[] f = floaters.get(i);
            float age = (t - f[3]) / 1100f;
            if (age > 1) {
                floaters.remove(i);
                continue;
            }
            float x = f[0] + (float) Math.sin(age * 9 + f[3]) * dp(10);
            float y = f[1] - dp(110) * age;
            int col = (int) f[2];
            drawNote(c, x, y, dp(26) * (0.7f + 0.5f * age), Color.argb((int) (255 * (1 - age)),
                    Color.red(col), Color.green(col), Color.blue(col)));
        }
    }

    private void drawNote(Canvas c, float x, float y, float s, int color) {
        paint.setColor(color);
        c.save();
        c.rotate(-20, x, y);
        rect.set(x - s * 0.32f, y - s * 0.2f, x + s * 0.32f, y + s * 0.2f);
        c.drawOval(rect, paint);
        c.restore();
        line.setColor(color);
        line.setStrokeWidth(s * 0.1f);
        c.drawLine(x + s * 0.27f, y - s * 0.05f, x + s * 0.27f, y - s * 0.95f, line);
        c.drawLine(x + s * 0.27f, y - s * 0.95f, x + s * 0.6f, y - s * 0.65f, line);
    }

    private void drawRipples(Canvas c) {
        long t = now();
        line.setStrokeWidth(dp(4));
        for (int i = ripples.size() - 1; i >= 0; i--) {
            float[] r = ripples.get(i);
            float age = (t - r[2]) / 500f;
            if (age > 1) {
                ripples.remove(i);
                continue;
            }
            line.setColor(Color.argb((int) (200 * (1 - age)), 255, 255, 255));
            c.drawCircle(r[0], r[1], dp(20) + dp(70) * age, line);
        }
    }

    // ---- the instruments ----

    private void drawKeys(Canvas c) {
        boolean harmonium = kind == Instrument.HARMONIUM;
        int body = harmonium ? Color.rgb(141, 90, 55) : Color.rgb(45, 30, 40);
        rect.set(0, 0, W, H);
        paint.setShader(new LinearGradient(0, 0, 0, H, Art.tint(body, 0.15f), body, Shader.TileMode.CLAMP));
        c.drawRoundRect(rect, dp(18), dp(18), paint);
        paint.setShader(null);
        float top = keyTop(), left = keyLeft(), ww = whiteW();
        if (harmonium) {
            // Bellows pleats and stops along the top.
            line.setStrokeWidth(dp(2));
            line.setColor(Color.rgb(90, 55, 35));
            for (int i = 1; i < 5; i++) c.drawLine(W * 0.04f, top * i / 5f, W * 0.96f, top * i / 5f, line);
            float air = (float) Math.sin(now() / 400.0) * 0.5f + 0.5f;
            paint.setColor(Color.argb((int) (40 + 40 * air), 255, 230, 200));
            c.drawRect(W * 0.04f, top * 0.1f, W * 0.96f, top * 0.9f, paint);
        } else {
            paint.setColor(Color.rgb(198, 40, 40));
            c.drawRect(left, top - dp(5), W - left, top, paint);
        }
        float bottom = H - dp(6);
        for (int i = 0; i < 8; i++) {
            int zone = Instrument.SCALE[i];
            float p = pressed(zone);
            rect.set(left + i * ww + dp(1.5f), top, left + (i + 1) * ww - dp(1.5f), bottom + p * dp(3));
            paint.setColor(p > 0 ? Art.tint(Instrument.NOTE_COLORS[i], 0.55f + 0.3f * (1 - p)) : Color.WHITE);
            c.drawRoundRect(rect, dp(8), dp(8), paint);
            paint.setColor(Color.argb(30, 0, 0, 0));
            c.drawRect(rect.left, rect.bottom - dp(8), rect.right, rect.bottom - dp(4), paint);
            float lr = Math.min(ww * 0.34f, (bottom - top) * 0.12f);
            float ly = bottom - ww * 0.62f;
            paint.setColor(Instrument.NOTE_COLORS[i]);
            c.drawCircle(rect.centerX(), ly, lr, paint);
            upright(c, noteName(i), rect.centerX(), ly, lr * 0.95f, Color.WHITE, lr * 1.8f);
        }
        float blackBottom = top + (H - top) * 0.6f;
        for (int b = 0; b < 5; b++) {
            float cx = left + (BLACK_AFTER[b] + 1) * ww;
            float p = pressed(BLACK_NOTE[b]);
            rect.set(cx - ww * 0.3f, top, cx + ww * 0.3f, blackBottom + p * dp(2));
            paint.setColor(p > 0 ? Color.rgb(94, 53, 177) : Color.rgb(30, 24, 30));
            c.drawRoundRect(rect, dp(5), dp(5), paint);
            paint.setColor(Color.argb(60, 255, 255, 255));
            c.drawRoundRect(rect.left + dp(3), rect.bottom - dp(14), rect.right - dp(3), rect.bottom - dp(5), dp(3), dp(3), paint);
        }
    }

    private void drawXylophone(Canvas c) {
        float colW = W / 8f;
        line.setColor(Color.rgb(93, 58, 36));
        line.setStrokeWidth(Math.max(dp(6), H * 0.04f));
        c.drawLine(colW * 0.2f, H * 0.2f, W - colW * 0.2f, H * 0.33f, line);
        c.drawLine(colW * 0.2f, H * 0.8f, W - colW * 0.2f, H * 0.67f, line);
        for (int i = 0; i < 8; i++) {
            float p = pressed(i);
            float h = H * (0.9f - i * 0.045f);
            float cx = (i + 0.5f) * colW, cy = H / 2f + p * dp(5);
            rect.set(cx - colW * 0.4f, cy - h / 2f, cx + colW * 0.4f, cy + h / 2f);
            int col = Instrument.NOTE_COLORS[i];
            if (p > 0) col = Art.tint(col, 0.3f * p);
            paint.setShader(new LinearGradient(rect.left, 0, rect.right, 0, Art.tint(col, 0.3f), col, Shader.TileMode.CLAMP));
            c.drawRoundRect(rect, dp(10), dp(10), paint);
            paint.setShader(null);
            paint.setColor(Color.argb(170, 255, 255, 255));
            c.drawCircle(cx, rect.top + colW * 0.25f, colW * 0.08f, paint);
            c.drawCircle(cx, rect.bottom - colW * 0.25f, colW * 0.08f, paint);
            upright(c, noteName(i), cx, rect.bottom - h * 0.22f, Math.min(colW * 0.42f, dp(30)), Color.WHITE, colW * 0.75f);
        }
    }

    private void drawBells(Canvas c) {
        float colW = W / 8f;
        float beamY = H * 0.08f;
        paint.setColor(Color.rgb(121, 85, 61));
        c.drawRoundRect(0, beamY - dp(8), W, beamY + dp(8), dp(8), dp(8), paint);
        for (int i = 0; i < 8; i++) {
            float cx = (i + 0.5f) * colW;
            float size = Math.min(colW * (0.95f - i * 0.04f), H * 0.55f);
            float top = beamY + H * 0.1f;
            long t = now() - pressAt[i];
            float swing = t < 1500 ? (float) (Math.sin(t / 90.0) * 18 * Math.exp(-t / 500.0)) : 0;
            c.save();
            c.rotate(swing, cx, beamY);
            line.setColor(Color.rgb(121, 85, 61));
            line.setStrokeWidth(dp(2));
            c.drawLine(cx, beamY, cx, top, line);
            paint.setColor(Instrument.NOTE_COLORS[i]);
            c.drawCircle(cx, top, size * 0.1f, paint);
            path.reset();
            float bw = size * 0.5f, bh = size * 1.05f;
            path.moveTo(cx - bw * 0.45f, top + bh * 0.12f);
            path.cubicTo(cx - bw * 0.45f, top - bh * 0.02f, cx + bw * 0.45f, top - bh * 0.02f, cx + bw * 0.45f, top + bh * 0.12f);
            path.cubicTo(cx + bw * 0.55f, top + bh * 0.6f, cx + bw * 0.7f, top + bh * 0.75f, cx + bw, top + bh * 0.85f);
            path.lineTo(cx - bw, top + bh * 0.85f);
            path.cubicTo(cx - bw * 0.7f, top + bh * 0.75f, cx - bw * 0.55f, top + bh * 0.6f, cx - bw * 0.45f, top + bh * 0.12f);
            path.close();
            int gold = pressed(i) > 0 ? Color.rgb(255, 236, 130) : Color.rgb(255, 202, 40);
            paint.setShader(new LinearGradient(cx - bw, 0, cx + bw, 0, Art.tint(gold, 0.5f), Color.rgb(230, 150, 20), Shader.TileMode.CLAMP));
            c.drawPath(path, paint);
            paint.setShader(null);
            paint.setColor(Color.rgb(200, 120, 20));
            c.drawCircle(cx, top + bh * 0.92f, size * 0.09f, paint);
            c.restore();
            upright(c, noteName(i), cx, H * 0.88f, Math.min(colW * 0.4f, dp(28)), Instrument.NOTE_COLORS[i], colW * 0.8f);
        }
    }

    private void drawHoles(Canvas c) {
        boolean flute = kind == Instrument.FLUTE;
        float ty = H * 0.42f, th = H * 0.2f;
        if (flute) {
            rect.set(W * 0.01f, ty - th / 2f, W * 0.99f, ty + th / 2f);
            paint.setShader(new LinearGradient(0, rect.top, 0, rect.bottom, Color.rgb(240, 214, 150), Color.rgb(196, 150, 80), Shader.TileMode.CLAMP));
            c.drawRoundRect(rect, th / 2f, th / 2f, paint);
            paint.setShader(null);
            paint.setColor(Color.rgb(198, 40, 40));
            for (float x : new float[]{W * 0.04f, W * 0.13f, W * 0.97f}) c.drawRect(x - dp(4), rect.top, x + dp(4), rect.bottom, paint);
            paint.setColor(Color.rgb(60, 40, 30));
            c.drawCircle(W * 0.085f, ty, th * 0.22f, paint);
        } else {
            // Trumpet: mouthpiece, tube, a loop, and the bell.
            int brass = Color.rgb(240, 190, 60);
            line.setColor(brass);
            line.setStrokeWidth(th * 0.45f);
            c.drawLine(W * 0.03f, ty + th * 0.6f, W * 0.82f, ty + th * 0.6f, line);
            rect.set(W * 0.2f, ty + th * 0.6f, W * 0.75f, H * 0.92f);
            line.setStrokeWidth(th * 0.3f);
            c.drawRoundRect(rect, th, th, line);
            path.reset();
            path.moveTo(W * 0.78f, ty + th * 0.45f);
            path.quadTo(W * 0.9f, ty + th * 0.4f, W * 0.99f, ty - th * 1.2f);
            path.lineTo(W * 0.99f, ty + th * 2.4f);
            path.quadTo(W * 0.9f, ty + th * 0.8f, W * 0.78f, ty + th * 0.75f);
            path.close();
            paint.setShader(new LinearGradient(W * 0.78f, 0, W, 0, brass, Color.rgb(255, 230, 140), Shader.TileMode.CLAMP));
            c.drawPath(path, paint);
            paint.setShader(null);
            paint.setColor(Color.rgb(200, 150, 40));
            c.drawCircle(W * 0.025f, ty + th * 0.6f, th * 0.3f, paint);
        }
        float hr = Math.min(H * 0.075f, W * 0.035f);
        for (int i = 0; i < 8; i++) {
            float x = holeX(i), p = pressed(i);
            if (flute) {
                paint.setColor(Color.rgb(50, 32, 24));
                c.drawCircle(x, ty, hr, paint);
                if (p > 0) {
                    paint.setColor(Instrument.NOTE_COLORS[i]);
                    c.drawCircle(x, ty, hr * (0.7f + 0.5f * p), paint);
                }
            } else {
                line.setColor(Color.rgb(200, 150, 40));
                line.setStrokeWidth(hr * 0.5f);
                c.drawLine(x, ty - th * 0.1f, x, ty + th * 0.6f, line);
                float by = ty - th * 0.2f + p * dp(5);
                paint.setColor(p > 0 ? Instrument.NOTE_COLORS[i] : Color.rgb(250, 240, 210));
                c.drawCircle(x, by, hr * 1.25f, paint);
                line.setColor(Color.rgb(200, 150, 40));
                line.setStrokeWidth(dp(2));
                c.drawCircle(x, by, hr * 1.25f, line);
            }
            float lr = Math.min(W * 0.042f, H * 0.11f);
            float ly = flute ? H * 0.72f : H * 0.12f;
            paint.setColor(Instrument.NOTE_COLORS[i]);
            c.drawCircle(x, ly, lr, paint);
            upright(c, noteName(i), x, ly, lr * 0.95f, Color.WHITE, lr * 1.8f);
        }
    }

    private void drawStringLine(Canvas c, float x1, float x2, float y, float width, int color, float amp) {
        line.setColor(color);
        line.setStrokeWidth(width);
        if (amp < 0.3f) {
            c.drawLine(x1, y, x2, y, line);
            return;
        }
        path.reset();
        path.moveTo(x1, y);
        int n = 24;
        double phase = now() / 18.0;
        for (int k = 1; k <= n; k++) {
            float x = x1 + (x2 - x1) * k / n;
            float env = (float) Math.sin(Math.PI * k / n);
            path.lineTo(x, y + (float) (Math.sin(phase + k * 0.9) * amp * env));
        }
        c.drawPath(path, line);
    }

    private float vibration() {
        long latest = 0;
        for (long t : pressAt) latest = Math.max(latest, t);
        long age = now() - latest;
        return age > 900 ? 0 : dp(5) * (float) Math.exp(-age / 250.0);
    }

    private void drawNeck(Canvas c) {
        float zw = neckZoneW(), x0 = neckLeft(), x1 = x0 + 8 * zw;
        float cy = H * 0.5f;
        int strings;
        float nh;
        float amp = vibration();
        switch (kind) {
            case Instrument.SITAR: {
                strings = 3;
                nh = H * 0.26f;
                paint.setColor(Color.rgb(196, 120, 60));
                c.drawCircle(x0 + zw * 0.4f, cy - nh * 0.85f, H * 0.14f, paint);
                paint.setShader(new RadialGradient(W * 0.82f, cy - H * 0.1f, H * 0.6f, Color.rgb(230, 160, 90),
                        Color.rgb(160, 90, 40), Shader.TileMode.CLAMP));
                c.drawCircle(W * 0.85f, cy, Math.min(H * 0.46f, W * 0.15f), paint);
                paint.setShader(null);
                line.setColor(Color.rgb(250, 240, 220));
                line.setStrokeWidth(dp(3));
                c.drawCircle(W * 0.85f, cy, Math.min(H * 0.34f, W * 0.11f), line);
                rect.set(0, cy - nh / 2f, W * 0.8f, cy + nh / 2f);
                paint.setColor(Color.rgb(93, 58, 36));
                c.drawRoundRect(rect, dp(8), dp(8), paint);
                paint.setColor(Color.rgb(250, 240, 220));
                c.drawRect(0, rect.top, W * 0.8f, rect.top + dp(3), paint);
                c.drawRect(0, rect.bottom - dp(3), W * 0.8f, rect.bottom, paint);
                line.setColor(Color.rgb(215, 215, 222));
                line.setStrokeWidth(dp(3));
                for (int i = 0; i <= 8; i++) {
                    float x = x0 + i * zw;
                    path.reset();
                    path.moveTo(x, rect.top + dp(4));
                    path.quadTo(x - dp(6), cy, x, rect.bottom - dp(4));
                    c.drawPath(path, line);
                }
                break;
            }
            case Instrument.EKTARA: {
                strings = 1;
                nh = H * 0.34f;
                float gx = W * 0.84f, gr = Math.min(H * 0.4f, W * 0.15f);
                line.setColor(Color.rgb(222, 184, 110));
                line.setStrokeWidth(dp(10));
                c.drawLine(gx - gr * 0.3f, cy - gr * 0.85f, 0, cy - dp(2), line);
                c.drawLine(gx - gr * 0.3f, cy + gr * 0.85f, 0, cy + dp(2), line);
                paint.setShader(new RadialGradient(gx - gr * 0.3f, cy - gr * 0.3f, gr * 1.4f, Color.rgb(240, 170, 90),
                        Color.rgb(180, 95, 40), Shader.TileMode.CLAMP));
                c.drawCircle(gx, cy, gr, paint);
                paint.setShader(null);
                paint.setColor(Color.rgb(255, 213, 79));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    c.drawCircle(gx + (float) Math.cos(a) * gr * 0.65f, cy + (float) Math.sin(a) * gr * 0.65f, gr * 0.07f, paint);
                }
                break;
            }
            default: {
                strings = 4;
                nh = H * 0.24f;
                float bx = W * 0.86f;
                paint.setShader(new RadialGradient(bx, cy - H * 0.15f, H * 0.7f, Color.rgb(200, 110, 60),
                        Color.rgb(130, 50, 25), Shader.TileMode.CLAMP));
                rect.set(W * 0.74f, cy - H * 0.36f, W * 0.86f, cy + H * 0.36f);
                c.drawOval(rect, paint);
                rect.set(W * 0.8f, cy - H * 0.46f, W * 0.995f, cy + H * 0.46f);
                c.drawOval(rect, paint);
                paint.setShader(null);
                line.setColor(Color.rgb(60, 25, 15));
                line.setStrokeWidth(dp(3));
                c.drawLine(W * 0.86f, cy - H * 0.3f, W * 0.89f, cy - H * 0.2f, line);
                c.drawLine(W * 0.86f, cy + H * 0.3f, W * 0.89f, cy + H * 0.2f, line);
                rect.set(W * 0.02f, cy - nh / 2f, W * 0.82f, cy + nh / 2f);
                paint.setColor(Color.rgb(30, 24, 26));
                c.drawRoundRect(rect, dp(6), dp(6), paint);
                paint.setColor(Color.rgb(130, 50, 25));
                c.drawCircle(W * 0.02f, cy, nh * 0.42f, paint);
                line.setColor(Color.rgb(90, 35, 20));
                c.drawCircle(W * 0.02f, cy, nh * 0.22f, line);
                paint.setColor(Color.argb(90, 255, 255, 255));
                for (int i = 1; i < 8; i++) c.drawCircle(x0 + i * zw, cy, dp(3), paint);
                break;
            }
        }
        for (int i = 0; i < 8; i++) {
            float p = pressed(i);
            if (p > 0) {
                paint.setColor(Color.argb((int) (150 * p), Color.red(Instrument.NOTE_COLORS[i]),
                        Color.green(Instrument.NOTE_COLORS[i]), Color.blue(Instrument.NOTE_COLORS[i])));
                rect.set(x0 + i * zw, cy - nh / 2f - dp(6), x0 + (i + 1) * zw, cy + nh / 2f + dp(6));
                c.drawRoundRect(rect, dp(10), dp(10), paint);
            }
        }
        float end = kind == Instrument.EKTARA ? W * 0.84f : W * 0.9f;
        for (int s = 0; s < strings; s++) {
            float y = strings == 1 ? cy : cy - nh * 0.3f + s * nh * 0.6f / (strings - 1);
            drawStringLine(c, 0, end, y, dp(strings == 1 ? 3 : 2), Color.rgb(235, 235, 240), amp);
        }
        if (kind == Instrument.EKTARA) {
            for (int i = 0; i < 8; i++) {
                paint.setColor(Instrument.NOTE_COLORS[i]);
                c.drawCircle(x0 + (i + 0.5f) * zw, cy, dp(7), paint);
            }
        }
        float lr = Math.min(zw * 0.36f, H * 0.11f);
        for (int i = 0; i < 8; i++) {
            float x = x0 + (i + 0.5f) * zw, ly = Math.min(H - lr - dp(4), cy + nh / 2f + lr + dp(14));
            paint.setColor(Instrument.NOTE_COLORS[i]);
            c.drawCircle(x, ly, lr, paint);
            upright(c, noteName(i), x, ly, lr * 0.95f, Color.WHITE, lr * 1.8f);
        }
    }

    private void drawGuitar(Canvas c) {
        float cy = H / 2f;
        int wood = Color.rgb(214, 150, 80);
        paint.setShader(new RadialGradient(W * 0.72f, cy, W * 0.4f, Color.rgb(240, 190, 120), wood, Shader.TileMode.CLAMP));
        c.drawCircle(W * 0.62f, cy, H * 0.52f, paint);
        c.drawCircle(W * 0.84f, cy, H * 0.62f, paint);
        paint.setShader(null);
        rect.set(0, stringY(0) - H * 0.09f, W * 0.5f, stringY(5) + H * 0.09f);
        paint.setColor(Color.rgb(70, 45, 35));
        c.drawRect(rect, paint);
        line.setColor(Color.rgb(200, 200, 205));
        line.setStrokeWidth(dp(3));
        for (int i = 1; i <= 4; i++) c.drawLine(W * 0.1f * i, rect.top, W * 0.1f * i, rect.bottom, line);
        paint.setColor(Color.rgb(250, 245, 235));
        for (int i : new int[]{2, 4}) c.drawCircle(W * 0.1f * i - W * 0.05f, cy, dp(6), paint);
        paint.setColor(Color.rgb(50, 30, 20));
        c.drawCircle(W * 0.64f, cy, H * 0.28f, paint);
        line.setColor(Color.rgb(250, 230, 190));
        line.setStrokeWidth(dp(4));
        c.drawCircle(W * 0.64f, cy, H * 0.32f, line);
        paint.setColor(Color.rgb(70, 45, 35));
        c.drawRoundRect(W * 0.86f, stringY(0) - dp(10), W * 0.9f, stringY(5) + dp(10), dp(4), dp(4), paint);
        for (int s = 0; s < 6; s++) {
            float p = pressed(s);
            float amp = p > 0 ? dp(6) * p : 0;
            int col = s < 3 ? Color.rgb(205, 160, 90) : Color.rgb(225, 225, 232);
            drawStringLine(c, 0, W * 0.88f, stringY(s), dp(5.2f) - s * dp(0.6f), col, amp);
            float lr = Math.min(H * 0.06f, dp(20));
            paint.setColor(colorOf(s));
            c.drawCircle(W * 0.03f + lr, stringY(s), lr, paint);
            upright(c, Instrument.GUITAR_NAMES[s], W * 0.03f + lr, stringY(s), lr * 1.1f, Color.WHITE, lr * 1.8f);
        }
    }

    private void drawPads(Canvas c) {
        if (kind == Instrument.DHAK) {
            Instrument.Pad a = pads[0], b = pads[1];
            float top = a.y * H - a.ry * H * 0.95f, bottom = a.y * H + a.ry * H * 0.95f;
            for (int i = 0; i < 9; i++) {
                double ang = Math.toRadians(-160 + i * 17.5);
                float bx = (a.x + b.x) / 2f * W, by = top;
                float ex = bx + (float) Math.cos(ang) * H * 0.4f, ey = by + (float) Math.sin(ang) * H * 0.3f;
                line.setColor(Color.rgb(200, 190, 170));
                line.setStrokeWidth(dp(2));
                c.drawLine(bx, by, ex, ey, line);
                paint.setColor(Color.rgb(252, 250, 244));
                c.drawCircle(ex, ey, H * 0.045f, paint);
            }
            rect.set(a.x * W, top, b.x * W, bottom);
            paint.setShader(new LinearGradient(0, top, 0, bottom, Color.rgb(229, 80, 70), Color.rgb(160, 30, 30), Shader.TileMode.CLAMP));
            c.drawRect(rect, paint);
            paint.setShader(null);
            line.setColor(Color.rgb(255, 202, 40));
            line.setStrokeWidth(dp(4));
            c.drawLine(rect.left, top + (bottom - top) * 0.25f, rect.right, top + (bottom - top) * 0.25f, line);
            c.drawLine(rect.left, top + (bottom - top) * 0.75f, rect.right, top + (bottom - top) * 0.75f, line);
            line.setStrokeWidth(dp(2));
            for (int i = 0; i <= 8; i++) {
                float x = rect.left + rect.width() * i / 8f;
                c.drawLine(x, top + (bottom - top) * 0.25f, x + rect.width() / 16f, top + (bottom - top) * 0.75f, line);
            }
            Instrument.Pad g = pads[2];
            line.setColor(Color.rgb(121, 85, 61));
            line.setStrokeWidth(dp(2));
            c.drawLine(g.x * W, 0, g.x * W, g.y * H - g.ry * H, line);
        }
        for (Instrument.Pad pad : pads) drawPad(c, pad);
    }

    private void drawPad(Canvas c, Instrument.Pad pad) {
        float x = pad.x * W, y = pad.y * H, rx = pad.rx * H, ry = pad.ry * H;
        float p = Math.max(pressed(pad.sound), pad.inner > 0 ? pressed(pad.innerSound) : 0);
        float sc = 1 - 0.05f * p;
        c.save();
        c.scale(sc, sc, x, y);
        paint.setColor(Color.argb(50, 0, 0, 0));
        rect.set(x - rx, y - ry + dp(6), x + rx, y + ry + dp(6));
        c.drawOval(rect, paint);
        rect.set(x - rx, y - ry, x + rx, y + ry);
        float labelSize = Math.min(rx, ry) * 0.24f;
        switch (pad.style) {
            case Instrument.BAYAN:
            case Instrument.DAYAN: {
                boolean bayan = pad.style == Instrument.BAYAN;
                int rim = bayan ? Color.rgb(176, 120, 80) : Color.rgb(121, 78, 48);
                paint.setShader(new RadialGradient(x - rx * 0.3f, y - ry * 0.3f, rx * 1.4f, Art.tint(rim, 0.3f), rim, Shader.TileMode.CLAMP));
                c.drawOval(rect, paint);
                paint.setShader(null);
                float skin = 0.84f;
                paint.setColor(p > 0 && pressed(pad.sound) > 0 ? Color.rgb(255, 248, 225) : Color.rgb(246, 232, 200));
                c.drawCircle(x, y, rx * skin, paint);
                line.setColor(Color.rgb(90, 55, 35));
                line.setStrokeWidth(dp(2));
                for (int i = 0; i < 16; i++) {
                    double a = i * Math.PI / 8;
                    c.drawLine(x + (float) Math.cos(a) * rx * skin, y + (float) Math.sin(a) * rx * skin,
                            x + (float) Math.cos(a) * rx * 0.99f, y + (float) Math.sin(a) * rx * 0.99f, line);
                }
                line.setColor(Color.rgb(200, 170, 120));
                line.setStrokeWidth(dp(3));
                c.drawCircle(x, y, rx * (skin - 0.06f), line);
                paint.setColor(pressed(pad.innerSound) > 0 ? Color.rgb(70, 60, 80) : Color.rgb(28, 24, 28));
                c.drawCircle(x, y, rx * pad.inner, paint);
                upright(c, Lang.t(pad.innerLabel), x, y, rx * 0.2f, Color.WHITE, rx * pad.inner * 1.7f);
                float ly = y + rx * (pad.inner + skin) / 2f;
                upright(c, Lang.t(pad.label), x, ly, rx * 0.17f, Color.rgb(90, 55, 35), rx * 0.8f);
                break;
            }
            case Instrument.CYMBAL:
            case Instrument.GONG: {
                paint.setShader(new RadialGradient(x - rx * 0.2f, y - ry * 0.2f, rx * 1.2f, Color.rgb(255, 236, 150),
                        Color.rgb(214, 160, 40), Shader.TileMode.CLAMP));
                c.drawOval(rect, paint);
                paint.setShader(null);
                line.setColor(Color.argb(90, 150, 100, 20));
                line.setStrokeWidth(dp(1.5f));
                for (int i = 1; i < 5; i++) c.drawCircle(x, y, rx * i / 5f, line);
                paint.setColor(Color.rgb(230, 180, 60));
                c.drawCircle(x, y, rx * (pad.style == Instrument.GONG ? 0.3f : 0.14f), paint);
                upright(c, Lang.t(pad.label), x, y + ry * 0.58f, labelSize, Color.rgb(120, 70, 10), rx * 1.4f);
                break;
            }
            case Instrument.CLAP: {
                paint.setColor(Color.WHITE);
                c.drawOval(rect, paint);
                Art.emoji(c, "👏", x, y - ry * 0.12f, rx * 1.1f);
                upright(c, Lang.t(pad.label), x, y + ry * 0.62f, labelSize, Art.INK, rx * 1.4f);
                break;
            }
            case Instrument.SHELL: {
                float glow = 0.5f + 0.5f * (float) Math.sin(now() / 500.0);
                paint.setColor(Color.argb((int) (60 + 60 * glow + 100 * p), 255, 255, 255));
                c.drawOval(rect, paint);
                Art.emoji(c, "🐚", x, y - ry * 0.08f, rx * 1.35f);
                upright(c, Lang.t(pad.label), x, y + ry * 0.78f, labelSize * 0.9f, Color.rgb(216, 27, 96), rx * 1.4f);
                break;
            }
            default: {
                boolean kick = pad.style == Instrument.KICK;
                int shell = kind == Instrument.DHAK ? Color.rgb(160, 30, 30) : kick ? Color.rgb(30, 136, 229) : Color.rgb(229, 57, 53);
                paint.setColor(shell);
                c.drawOval(rect, paint);
                rect.inset(rx * 0.1f, ry * 0.1f);
                paint.setShader(new RadialGradient(x - rx * 0.25f, y - ry * 0.25f, rx * 1.2f, Color.WHITE,
                        p > 0 ? Color.rgb(255, 240, 200) : Color.rgb(230, 228, 220), Shader.TileMode.CLAMP));
                c.drawOval(rect, paint);
                paint.setShader(null);
                if (kick) {
                    paint.setColor(Color.argb(60, 30, 136, 229));
                    c.drawCircle(x, y, rx * 0.35f, paint);
                }
                if (kind != Instrument.DHAK) {
                    paint.setColor(Color.rgb(200, 200, 205));
                    for (int i = 0; i < 8; i++) {
                        double a = i * Math.PI / 4;
                        c.drawCircle(x + (float) Math.cos(a) * rx * 0.95f, y + (float) Math.sin(a) * ry * 0.95f, rx * 0.05f, paint);
                    }
                }
                upright(c, Lang.t(pad.label), x, y, labelSize, Art.INK, Math.min(rx, ry) * 1.5f);
                break;
            }
        }
        c.restore();
    }
}
