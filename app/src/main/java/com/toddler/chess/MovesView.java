package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * "Catch the star": one piece on an empty board, green dots where it may move and a star on
 * one of those squares. Tapping a dot moves the piece there; landing on the star is a party.
 */
final class MovesView extends PlayView {
    private static final int[] ORDER = {Pieces.ROOK, Pieces.BISHOP, Pieces.QUEEN, Pieces.KING, Pieces.KNIGHT, Pieces.PAWN};
    private static final String[][] RULES = {
            {"The king takes one little step, any way.", "রাজা এক পা যায়, যেকোনো দিকে।", "राजा एक कदम चलता है, किसी भी तरफ़।"},
            {"The queen goes far, straight and slanty!", "মন্ত্রী অনেক দূর যায়, সোজা আর কোনাকুনি!", "रानी दूर तक जाती है, सीधी और तिरछी!"},
            {"The rook goes straight. Up and down, side to side.", "নৌকা সোজা যায়। উপর নিচ, ডানে বাঁয়ে।", "हाथी सीधा चलता है। ऊपर नीचे, दाएँ बाएँ।"},
            {"The bishop goes slanty, corner to corner.", "হাতি কোনাকুনি যায়।", "ऊँट तिरछा चलता है।"},
            {"The knight jumps like an L. Hop!", "ঘোড়া আড়াই ঘর লাফ দেয়!", "घोड़ा ढाई घर कूदता है!"},
            {"The pawn walks forward, one step.", "বোড়ে সামনে এক পা যায়।", "प्यादा आगे एक कदम चलता है।"},
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path star = new Path();
    private final Random random = new Random();
    private final List<Integer> targets = new ArrayList<>();

    private int lesson;
    private int type;
    private int pos;
    private int starSq;
    private int from = -1;
    private long moveStart;
    private long bounceStart;
    private int stars;

    MovesView(Context context) {
        super(context);
    }

    void start() {
        setLesson(0);
    }

    private void setLesson(int i) {
        lesson = (i + ORDER.length) % ORDER.length;
        type = ORDER[lesson];
        pos = type == Pieces.PAWN ? 6 * 8 + 3 : 4 * 8 + 3;
        from = -1;
        computeTargets();
        placeStar();
        bounceStart = now();
        activity.speaker.say(Pieces.name(type) + "! " + Lang.t(RULES[type]) + " "
                + Lang.t("Catch the star!", "তারাটা ধরো!", "तारा पकड़ो!"));
    }

    private void computeTargets() {
        targets.clear();
        int row = pos / 8, col = pos % 8;
        switch (type) {
            case Pieces.KING:
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++)
                        if (dr != 0 || dc != 0) add(row + dr, col + dc);
                break;
            case Pieces.KNIGHT:
                int[][] jumps = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
                for (int[] j : jumps) add(row + j[0], col + j[1]);
                break;
            case Pieces.PAWN:
                add(row - 1, col);
                if (row == 6) add(row - 2, col);
                break;
            default:
                boolean straight = type == Pieces.ROOK || type == Pieces.QUEEN;
                boolean slanty = type == Pieces.BISHOP || type == Pieces.QUEEN;
                int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                for (int d = 0; d < 8; d++) {
                    if (d < 4 ? !straight : !slanty) continue;
                    for (int k = 1; k < 8; k++) {
                        if (!add(row + dirs[d][0] * k, col + dirs[d][1] * k)) break;
                    }
                }
                break;
        }
    }

    private boolean add(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) return false;
        targets.add(row * 8 + col);
        return true;
    }

    private void placeStar() {
        if (type == Pieces.PAWN && pos / 8 == 0) {
            // The pawn reached the far side; start again at the bottom.
            pos = 6 * 8 + random.nextInt(8);
            computeTargets();
        }
        starSq = targets.get(random.nextInt(targets.size()));
    }

    private float topBar() {
        return dp(84);
    }

    private float boardSize() {
        return Math.min(getWidth(), getHeight() - topBar()) * 0.95f;
    }

    private float boardLeft() {
        return (getWidth() - boardSize()) / 2f;
    }

    private float boardTop() {
        return topBar() + (getHeight() - topBar() - boardSize()) / 2f;
    }

    private float cx(int sq) {
        return boardLeft() + (sq % 8 + 0.5f) * boardSize() / 8f;
    }

    private float cy(int sq) {
        return boardTop() + (sq / 8 + 0.5f) * boardSize() / 8f;
    }

    private int squareAt(float x, float y) {
        float sq = boardSize() / 8f;
        int col = (int) Math.floor((x - boardLeft()) / sq);
        int row = (int) Math.floor((y - boardTop()) / sq);
        if (col < 0 || col > 7 || row < 0 || row > 7) return -1;
        return row * 8 + col;
    }

    private float buttonRadius() {
        return dp(30);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_DOWN) return true;
        float x = e.getX(), y = e.getY();
        float m = dp(12) + buttonRadius();
        if (y < m * 2) {
            if (x < m * 2) activity.showHome();
            else if (x > getWidth() - m * 2) {
                activity.chimes.next();
                setLesson(lesson + 1);
            }
            return true;
        }
        if (from != -1) return true; // still moving
        int sq = squareAt(x, y);
        if (sq == -1) return true;
        if (sq == pos) {
            bounceStart = now();
            activity.chimes.ding();
            activity.speaker.say(Pieces.name(type) + "! " + Lang.t(RULES[type]));
        } else if (targets.contains(sq)) {
            from = pos;
            pos = sq;
            moveStart = now();
            activity.chimes.next();
            postDelayed(this::landed, 450);
        } else {
            activity.chimes.low();
            activity.speaker.say(Lang.t("Go to a green dot!", "সবুজ ফোঁটায় যাও!", "हरे बिंदु पर जाओ!"));
        }
        return true;
    }

    private void landed() {
        from = -1;
        bounceStart = now();
        computeTargets();
        if (pos == starSq) {
            stars++;
            burst(cx(pos), cy(pos));
            burst(cx(pos), cy(pos));
            activity.chimes.ding();
            activity.speaker.say(Lang.t("Yay! You caught the star!", "দারুণ! তারাটা ধরেছ!", "वाह! तारा पकड़ लिया!"));
            placeStar();
        } else if (!targets.contains(starSq)) {
            // Keep the star one move away so there is always a dot to tap on it.
            placeStar();
        }
    }

    @Override
    protected void paint(Canvas c) {
        backdrop(c, Color.rgb(232, 245, 233), Color.rgb(165, 214, 167));
        float w = getWidth();
        float size = boardSize(), sq = size / 8f, left = boardLeft(), top = boardTop();

        float r = buttonRadius();
        float m = dp(12) + r;
        Art.homeButton(c, m, m, r);
        Art.arrowButton(c, w - m, m, r, 1);
        String title = Pieces.name(type) + (stars > 0 ? "  ⭐" + stars : "");
        Art.label(c, title, w / 2f, topBar() / 2f + dp(4), Math.min(dp(40), (w - 4 * m) / 5f),
                Color.rgb(46, 125, 50));

        BoardView.drawBoard(c, paint, rect, left, top, size, null, dp(1));

        long t = now();
        boolean moving = from != -1;
        if (!moving) {
            float pulse = 1 + 0.15f * (float) Math.sin(t / 220.0);
            for (int target : targets) {
                if (target == starSq) continue;
                paint.setColor(Color.argb(200, 76, 175, 80));
                c.drawCircle(cx(target), cy(target), sq * 0.17f * pulse, paint);
                paint.setColor(Color.argb(120, 255, 255, 255));
                c.drawCircle(cx(target) - sq * 0.04f, cy(target) - sq * 0.04f, sq * 0.06f * pulse, paint);
            }
            star.reset();
            Art.star(star, 0, 0, sq * 0.42f, sq * 0.19f);
            c.save();
            c.translate(cx(starSq), cy(starSq));
            c.rotate((t / 15) % 360);
            c.scale(pulse, pulse);
            paint.setColor(Color.rgb(255, 202, 40));
            c.drawPath(star, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(sq * 0.04f);
            paint.setColor(Color.rgb(245, 124, 0));
            c.drawPath(star, paint);
            paint.setStyle(Paint.Style.FILL);
            c.restore();
        }

        float px = cx(pos), py = cy(pos);
        if (moving) {
            float k = Math.min(1f, (t - moveStart) / 450f);
            float ease = k * k * (3 - 2 * k);
            px = cx(from) + (cx(pos) - cx(from)) * ease;
            py = cy(from) + (cy(pos) - cy(from)) * ease;
            // Every piece hops; the knight jumps extra high because it leaps over things.
            py -= (float) Math.sin(k * Math.PI) * sq * (type == Pieces.KNIGHT ? 1.2f : 0.35f);
        }
        float scale = 1 + 0.25f * bounce(bounceStart, 600);
        Pieces.draw(c, type, true, px, py - sq * 0.02f, sq * 0.95f * scale);
    }
}
