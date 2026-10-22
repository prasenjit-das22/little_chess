package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.Arrays;

/**
 * A real chess board in the starting position. Touching a piece says its colour and name
 * ("White knight!"), and pieces can be dragged anywhere just for fun.
 */
final class BoardView extends PlayView {
    private static final int EMPTY = -1;
    private static final int[] BACK_RANK = {
            Pieces.ROOK, Pieces.KNIGHT, Pieces.BISHOP, Pieces.QUEEN,
            Pieces.KING, Pieces.BISHOP, Pieces.KNIGHT, Pieces.ROOK,
    };
    private static final int LIGHT = Color.rgb(255, 236, 200);
    private static final int DARK = Color.rgb(196, 134, 88);

    // Squares 0..63, row 0 at the top. Values: piece type, +6 for black pieces.
    private final int[] board = new int[64];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int dragFrom = EMPTY;
    private float dragX, dragY, downX, downY;
    private boolean dragMoved;
    private int bounceSquare = EMPTY;
    private long bounceStart;
    private int flashSquare = EMPTY;
    private long flashStart;

    BoardView(Context context) {
        super(context);
        reset();
    }

    void start() {
        activity.speaker.say("This is a chess board. Touch a piece!");
    }

    private void reset() {
        Arrays.fill(board, EMPTY);
        for (int col = 0; col < 8; col++) {
            board[col] = BACK_RANK[col] + 6;
            board[8 + col] = Pieces.PAWN + 6;
            board[48 + col] = Pieces.PAWN;
            board[56 + col] = BACK_RANK[col];
        }
        dragFrom = EMPTY;
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

    private int squareAt(float x, float y) {
        float sq = boardSize() / 8f;
        int col = (int) Math.floor((x - boardLeft()) / sq);
        int row = (int) Math.floor((y - boardTop()) / sq);
        if (col < 0 || col > 7 || row < 0 || row > 7) return EMPTY;
        return row * 8 + col;
    }

    private static String nameOf(int piece) {
        return (piece >= 6 ? "Black " : "White ") + Pieces.NAMES[piece % 6].toLowerCase() + "!";
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                dragMoved = false;
                if (hitButton(x, y)) return true;
                int sq = squareAt(x, y);
                if (sq == EMPTY) return true;
                if (board[sq] != EMPTY) {
                    dragFrom = sq;
                    dragX = x;
                    dragY = y;
                    activity.chimes.ding();
                    activity.speaker.say(nameOf(board[sq]));
                } else {
                    flashSquare = sq;
                    flashStart = now();
                    activity.chimes.low();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragFrom != EMPTY) {
                    dragX = x;
                    dragY = y;
                    if (Math.hypot(x - downX, y - downY) > dp(12)) dragMoved = true;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (dragFrom == EMPTY) {
                    if (hitButton(downX, downY) && hitButton(x, y)) pressButton(x, y);
                    return true;
                }
                int to = dragMoved ? squareAt(x, y) : dragFrom;
                if (to != EMPTY && to != dragFrom) {
                    int tmp = board[to];
                    board[to] = board[dragFrom];
                    board[dragFrom] = tmp;
                    activity.chimes.next();
                } else {
                    to = dragFrom;
                }
                bounceSquare = to;
                bounceStart = now();
                burst(squareCenterX(to), squareCenterY(to));
                dragFrom = EMPTY;
                return true;
            case MotionEvent.ACTION_CANCEL:
                dragFrom = EMPTY;
                return true;
            default:
                return true;
        }
    }

    private float buttonRadius() {
        return dp(30);
    }

    private boolean hitButton(float x, float y) {
        float m = dp(12) + buttonRadius();
        return y < m * 2 && (x < m * 2 || x > getWidth() - m * 2);
    }

    private void pressButton(float x, float y) {
        if (x < getWidth() / 2f) {
            activity.showHome();
        } else {
            reset();
            activity.chimes.next();
            activity.speaker.say("All the pieces go back home!");
        }
    }

    private float squareCenterX(int sq) {
        float s = boardSize() / 8f;
        return boardLeft() + (sq % 8 + 0.5f) * s;
    }

    private float squareCenterY(int sq) {
        float s = boardSize() / 8f;
        return boardTop() + (sq / 8 + 0.5f) * s;
    }

    @Override
    protected void paint(Canvas c) {
        c.drawColor(Color.rgb(200, 230, 255));
        float w = getWidth();
        float size = boardSize(), sq = size / 8f, left = boardLeft(), top = boardTop();

        float r = buttonRadius();
        float m = dp(12) + r;
        Art.homeButton(c, m, m, r);
        Art.resetButton(c, w - m, m, r);
        Art.label(c, "Chess board", w / 2f, topBar() / 2f + dp(4),
                Math.min(dp(40), (w - 4 * m) / 6f), Color.rgb(93, 64, 55));

        paint.setColor(Color.rgb(93, 64, 55));
        rect.set(left - dp(8), top - dp(8), left + size + dp(8), top + size + dp(8));
        c.drawRoundRect(rect, dp(10), dp(10), paint);

        for (int i = 0; i < 64; i++) {
            int row = i / 8, col = i % 8;
            boolean light = (row + col) % 2 == 0;
            paint.setColor(light ? LIGHT : DARK);
            if (i == dragFrom) paint.setColor(Color.rgb(255, 241, 118));
            if (i == flashSquare && now() - flashStart < 400) {
                paint.setColor(light ? Color.rgb(200, 255, 200) : Color.rgb(130, 200, 120));
            }
            rect.set(left + col * sq, top + row * sq, left + (col + 1) * sq, top + (row + 1) * sq);
            c.drawRect(rect, paint);
        }

        for (int i = 0; i < 64; i++) {
            if (board[i] == EMPTY || i == dragFrom) continue;
            float cx = squareCenterX(i), cy = squareCenterY(i);
            float scale = 1;
            if (i == bounceSquare) scale += 0.3f * bounce(bounceStart, 600);
            Pieces.draw(c, board[i] % 6, board[i] < 6, cx, cy - sq * 0.02f, sq * 0.92f * scale);
        }

        if (dragFrom != EMPTY) {
            int p = board[dragFrom];
            float lift = dragMoved ? sq * 0.6f : 0;
            Pieces.draw(c, p % 6, p < 6, dragX, dragY - lift, sq * 1.3f);
        }
    }
}
