package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * "Find it!": three pictures and a question, "Where is the cow?". The right one gets sparkles and
 * a star; a wrong one wiggles and the question is asked again. Topics change every round.
 */
final class QuizView extends PlayView {
    private static final int[] TOPICS = {Decks.ANIMALS, Decks.BIRDS, Decks.FOOD, Decks.COLORS, Decks.SHAPES,
            Decks.THINGS, Decks.VEHICLES, Decks.BODY, Decks.NUMBERS, Decks.LETTERS, Decks.ABC};
    private static final String[] WHERE = {"Where is the %s?", "%s কোথায়?", "%s कहाँ है?"};
    private static final String[] WHERE_PLAIN = {"Where is %s?", "%s কোথায়?", "%s कहाँ है?"};
    private static final String[] WHICH = {"Which one is %s?", "কোনটা %s?", "कौन सा %s है?"};
    private static final String[] THAT_IS = {"That is %s.", "এটা %s।", "यह %s है।"};
    private static final String[][] PRAISE = {
            {"Yes!", "Well done!", "Super!", "Hooray!"},
            {"হ্যাঁ!", "দারুণ!", "খুব ভালো!", "শাবাশ!"},
            {"हाँ!", "शाबाश!", "बहुत बढ़िया!", "वाह!"},
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Random random = new Random();
    private final Item[] choices = new Item[3];
    private final long[] shakeAt = new long[3];
    private int topic = -1;
    private int answer;
    private boolean solved;
    private long solvedAt;
    private long roundAt;
    private int stars;

    QuizView(Context context) {
        super(context);
    }

    void start() {
        newRound();
    }

    private int promptLang() {
        if (topic == Decks.LETTERS) return Lang.BN;
        if (topic == Decks.ABC) return Lang.EN;
        return Lang.current;
    }

    private String ask(Item item) {
        String a = item.ask();
        if (promptLang() == Lang.EN && topic != Decks.ABC) a = a.toLowerCase(Locale.US);
        return a;
    }

    private String prompt() {
        int l = promptLang();
        String[] t = topic == Decks.COLORS ? WHICH
                : topic == Decks.NUMBERS || topic == Decks.ABC || topic == Decks.LETTERS ? WHERE_PLAIN : WHERE;
        return String.format(t[l], ask(choices[answer]));
    }

    private void newRound() {
        int next;
        do {
            next = TOPICS[random.nextInt(TOPICS.length)];
        } while (next == topic);
        topic = next;
        List<Item> deck = Decks.deck(topic);
        List<Integer> picks = new ArrayList<>();
        while (picks.size() < 3 && picks.size() < deck.size()) {
            int i = random.nextInt(deck.size());
            if (picks.contains(i)) continue;
            boolean same = false;
            for (int p : picks) if (ask(deck.get(p)).equals(ask(deck.get(i)))) same = true;
            if (!same) picks.add(i);
        }
        for (int i = 0; i < 3; i++) choices[i] = deck.get(picks.get(i % picks.size()));
        answer = random.nextInt(3);
        solved = false;
        roundAt = now();
        for (int i = 0; i < 3; i++) shakeAt[i] = 0;
        activity.speaker.say(prompt(), promptLang());
    }

    private float headerH() {
        return dp(124);
    }

    private void cardRect(int i, RectF out) {
        float w = getWidth(), h = getHeight(), top = headerH(), m = dp(14);
        if (w > h) {
            float cw = (w - m * 4) / 3f;
            out.set(m + i * (cw + m), top, m + i * (cw + m) + cw, h - m);
        } else {
            float ch = (h - top - m * 4) / 3f;
            out.set(m, top + m + i * (ch + m), w - m, top + m + i * (ch + m) + ch);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_DOWN) return true;
        float x = e.getX(), y = e.getY();
        float r = dp(28), m = dp(14) + r;
        if (Math.hypot(x - m, y - m) < r * 1.4f) {
            activity.showHome();
            return true;
        }
        if (y < headerH()) {
            activity.speaker.say(prompt(), promptLang());
            return true;
        }
        if (solved) return true;
        for (int i = 0; i < 3; i++) {
            cardRect(i, rect);
            if (!rect.contains(x, y)) continue;
            if (i == answer) {
                solved = true;
                solvedAt = now();
                stars++;
                burst(rect.centerX(), rect.centerY());
                activity.chimes.next();
                Item item = choices[i];
                int l = item.speechLang >= 0 ? item.speechLang : Lang.current;
                String[] praise = PRAISE[l];
                activity.speaker.say(praise[random.nextInt(praise.length)] + " " + item.speech, l);
                if (stars % 5 == 0) {
                    for (int k = 0; k < 5; k++) {
                        final int kk = k;
                        postDelayed(() -> {
                            burst(getWidth() * (0.1f + 0.2f * kk), getHeight() * 0.3f);
                            activity.chimes.next();
                        }, 300 + k * 160L);
                    }
                }
                postDelayed(this::newRound, 3200);
            } else {
                shakeAt[i] = now();
                activity.chimes.low();
                activity.speaker.say(String.format(THAT_IS[promptLang()], ask(choices[i])) + " " + prompt(), promptLang());
            }
            return true;
        }
        return true;
    }

    @Override
    protected void paint(Canvas c) {
        backdrop(c, Color.rgb(255, 249, 196), Color.rgb(255, 204, 128));
        float w = getWidth();
        float r = dp(28), m = dp(14) + r;
        Art.homeButton(c, m, m, r);

        // Stars collected so far.
        String count = Lang.digit(stars);
        Art.emoji(c, "⭐", w - dp(70), m, dp(40));
        Art.label(c, count, w - dp(30), m, dp(28), Color.rgb(230, 120, 0));

        float wave = (float) Math.sin(now() / 500.0) * dp(2);
        Art.label(c, Lang.t("Find it!", "খুঁজে দেখো!", "ढूँढो!"), w / 2f, m + wave, dp(30), Color.rgb(216, 27, 96));
        Art.label(c, prompt(), w / 2f, dp(96), dp(26), Art.INK);

        for (int i = 0; i < 3; i++) {
            cardRect(i, rect);
            Item item = choices[i];
            if (item == null) continue;
            float shake = 0;
            long st = now() - shakeAt[i];
            if (shakeAt[i] > 0 && st < 500) shake = (float) Math.sin(st / 30.0) * dp(10) * (1 - st / 500f);
            float scale = 1;
            boolean right = solved && i == answer;
            if (right) scale += 0.1f * bounce(solvedAt, 800) + 0.04f;
            float appear = Math.min(1, (now() - roundAt - i * 120) / 250f);
            if (appear <= 0) continue;
            scale *= 0.6f + 0.4f * appear;
            c.save();
            c.translate(shake, 0);
            c.scale(scale, scale, rect.centerX(), rect.centerY());
            float rad = dp(26);
            paint.setColor(Color.argb(50, 90, 50, 0));
            rect.offset(0, dp(5));
            c.drawRoundRect(rect, rad, rad, paint);
            rect.offset(0, -dp(5));
            paint.setColor(right ? Color.rgb(129, 199, 132) : Color.WHITE);
            c.drawRoundRect(rect, rad, rad, paint);
            rect.inset(dp(8), dp(8));
            paint.setColor(solved && !right ? Color.rgb(240, 236, 230) : Art.tint(item.background, 0.45f));
            c.drawRoundRect(rect, rad - dp(8), rad - dp(8), paint);
            float s = Math.min(rect.width(), rect.height()) * (right ? 0.72f : 0.85f);
            float cy = right ? rect.top + rect.height() * 0.42f : rect.centerY();
            item.painter.draw(c, rect.centerX(), cy, s);
            if (right) {
                Art.label(c, item.label, rect.centerX(), rect.bottom - rect.height() * 0.1f,
                        Math.min(rect.height() * 0.16f, dp(34)), item.labelColor);
            }
            c.restore();
        }
    }
}
