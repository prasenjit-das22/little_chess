package com.toddler.chess;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Chess piece silhouettes drawn as vector paths inside a 100x100 box. */
final class Pieces {
    static final int KING = 0, QUEEN = 1, ROOK = 2, BISHOP = 3, KNIGHT = 4, PAWN = 5;
    static final String[] NAMES = {"King", "Queen", "Rook", "Bishop", "Knight", "Pawn"};

    private static final Path[][] PARTS = new Path[6][];
    private static final Path[] DETAILS = new Path[6];
    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        STROKE.setStyle(Paint.Style.STROKE);
        STROKE.setStrokeWidth(3.5f);
        STROKE.setStrokeJoin(Paint.Join.ROUND);
        STROKE.setStrokeCap(Paint.Cap.ROUND);

        Path base = roundRect(18, 80, 82, 93, 5);
        Path band = roundRect(28, 70, 72, 81, 3);

        // King: cross on a two-lobed crown.
        Path cross = new Path();
        cross.moveTo(46, 4);
        cross.lineTo(54, 4);
        cross.lineTo(54, 10);
        cross.lineTo(60, 10);
        cross.lineTo(60, 17);
        cross.lineTo(54, 17);
        cross.lineTo(54, 26);
        cross.lineTo(46, 26);
        cross.lineTo(46, 17);
        cross.lineTo(40, 17);
        cross.lineTo(40, 10);
        cross.lineTo(46, 10);
        cross.close();
        Path dome = new Path();
        dome.moveTo(33, 72);
        dome.lineTo(25, 42);
        dome.quadTo(24, 28, 38, 28);
        dome.quadTo(50, 28, 50, 38);
        dome.quadTo(50, 28, 62, 28);
        dome.quadTo(76, 28, 75, 42);
        dome.lineTo(67, 72);
        dome.close();
        PARTS[KING] = new Path[]{cross, dome, band, base};
        Path kingDetail = new Path();
        kingDetail.moveTo(30, 54);
        kingDetail.quadTo(50, 48, 70, 54);
        DETAILS[KING] = kingDetail;

        // Queen: five-pointed crown with balls on the tips.
        float[][] tips = {{22, 28}, {36, 20}, {50, 14}, {64, 20}, {78, 28}};
        Path balls = new Path();
        for (float[] t : tips) balls.addCircle(t[0], t[1] - 2, 5, Path.Direction.CW);
        Path crown = new Path();
        crown.moveTo(32, 72);
        crown.lineTo(22, 28);
        crown.lineTo(31, 52);
        crown.lineTo(36, 20);
        crown.lineTo(43, 47);
        crown.lineTo(50, 14);
        crown.lineTo(57, 47);
        crown.lineTo(64, 20);
        crown.lineTo(69, 52);
        crown.lineTo(78, 28);
        crown.lineTo(68, 72);
        crown.close();
        PARTS[QUEEN] = new Path[]{balls, crown, band, base};
        Path queenDetail = new Path();
        queenDetail.moveTo(31, 62);
        queenDetail.quadTo(50, 57, 69, 62);
        DETAILS[QUEEN] = queenDetail;

        // Rook: a little castle tower.
        Path tower = new Path();
        tower.moveTo(24, 12);
        tower.lineTo(35, 12);
        tower.lineTo(35, 20);
        tower.lineTo(44, 20);
        tower.lineTo(44, 12);
        tower.lineTo(56, 12);
        tower.lineTo(56, 20);
        tower.lineTo(65, 20);
        tower.lineTo(65, 12);
        tower.lineTo(76, 12);
        tower.lineTo(76, 32);
        tower.lineTo(68, 37);
        tower.lineTo(66, 72);
        tower.lineTo(34, 72);
        tower.lineTo(32, 37);
        tower.lineTo(24, 32);
        tower.close();
        PARTS[ROOK] = new Path[]{tower, band, base};
        Path rookDetail = new Path();
        rookDetail.moveTo(32, 37);
        rookDetail.lineTo(68, 37);
        DETAILS[ROOK] = rookDetail;

        // Bishop: pointy hat with a slit and a ball on top.
        Path ball = circle(50, 11, 6);
        Path mitre = new Path();
        mitre.moveTo(50, 16);
        mitre.cubicTo(26, 32, 30, 56, 39, 63);
        mitre.lineTo(61, 63);
        mitre.cubicTo(70, 56, 74, 32, 50, 16);
        mitre.close();
        Path collar = roundRect(34, 61, 66, 69, 3);
        Path skirt = new Path();
        skirt.moveTo(41, 68);
        skirt.quadTo(40, 76, 28, 81);
        skirt.lineTo(72, 81);
        skirt.quadTo(60, 76, 59, 68);
        skirt.close();
        PARTS[BISHOP] = new Path[]{skirt, ball, mitre, collar, base};
        Path bishopDetail = new Path();
        bishopDetail.moveTo(57, 29);
        bishopDetail.lineTo(47, 43);
        DETAILS[BISHOP] = bishopDetail;

        // Knight: a horse head looking left.
        Path horse = new Path();
        horse.moveTo(68, 81);
        horse.lineTo(30, 81);
        horse.cubicTo(30, 68, 38, 60, 48, 53);
        horse.cubicTo(40, 55, 32, 60, 25, 57);
        horse.cubicTo(17, 53, 18, 45, 25, 40);
        horse.cubicTo(33, 33, 37, 24, 45, 17);
        horse.lineTo(44, 7);
        horse.lineTo(53, 14);
        horse.cubicTo(71, 16, 80, 35, 77, 57);
        horse.cubicTo(76, 67, 72, 75, 68, 81);
        horse.close();
        PARTS[KNIGHT] = new Path[]{horse, base};
        Path knightDetail = new Path();
        knightDetail.addCircle(43, 29, 2.5f, Path.Direction.CW);
        knightDetail.addCircle(25, 49, 1.2f, Path.Direction.CW);
        knightDetail.moveTo(58, 19);
        knightDetail.quadTo(72, 32, 70, 60);
        DETAILS[KNIGHT] = knightDetail;

        // Pawn: round head, collar and a bell-shaped body.
        Path body = new Path();
        body.moveTo(39, 46);
        body.quadTo(37, 68, 27, 81);
        body.lineTo(73, 81);
        body.quadTo(63, 68, 61, 46);
        body.close();
        Path pawnCollar = new Path();
        pawnCollar.addOval(new RectF(33, 41, 67, 51), Path.Direction.CW);
        PARTS[PAWN] = new Path[]{body, pawnCollar, circle(50, 28, 13), base};
    }

    private static Path circle(float x, float y, float r) {
        Path p = new Path();
        p.addCircle(x, y, r, Path.Direction.CW);
        return p;
    }

    private static Path roundRect(float l, float t, float r, float b, float rad) {
        Path p = new Path();
        p.addRoundRect(new RectF(l, t, r, b), rad, rad, Path.Direction.CW);
        return p;
    }

    /** Draws a piece centred at (cx, cy) filling a square of the given size. */
    static void draw(Canvas c, int type, boolean white, float cx, float cy, float size) {
        int fill = white ? Color.rgb(255, 253, 244) : Color.rgb(52, 48, 56);
        int outline = white ? Color.rgb(58, 42, 30) : Color.rgb(15, 12, 18);
        int detail = white ? Color.rgb(58, 42, 30) : Color.rgb(205, 200, 210);
        c.save();
        c.translate(cx - size / 2f, cy - size / 2f);
        c.scale(size / 100f, size / 100f);
        FILL.setColor(fill);
        STROKE.setColor(outline);
        for (Path p : PARTS[type]) {
            c.drawPath(p, FILL);
            c.drawPath(p, STROKE);
        }
        if (DETAILS[type] != null) {
            STROKE.setColor(detail);
            c.drawPath(DETAILS[type], STROKE);
            if (type == KNIGHT) {
                FILL.setColor(detail);
                c.drawCircle(43, 29, 2.5f, FILL);
            }
        }
        c.restore();
    }
}
