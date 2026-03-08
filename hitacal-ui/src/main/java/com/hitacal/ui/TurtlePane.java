package com.hitacal.ui;

import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Animated 2D turtle drawn purely with JavaFX Canvas API.
 * Supports 7 expressions with smooth Timeline-driven animations.
 */
public class TurtlePane extends Canvas {

    public enum Expression { IDLE, HAPPY, EXCITED, THINKING, SLEEPING, CHEERING, WORRIED }

    private Expression current = Expression.IDLE;
    private Timeline   animation;
    private double     animFrame   = 0;
    private double     bobOffset   = 0;
    private boolean    blinkOpen   = true;
    private int        blinkTick   = 0;
    private double     bounceY     = 0;
    private int        particleTick = 0;

    public TurtlePane(double w, double h) {
        super(w, h);
        startIdleAnimation();
    }

    public void setExpression(Expression expr) {
        current = expr;
        stopAnimation();
        animFrame = 0;
        bounceY   = 0;
        switch (expr) {
            case IDLE     -> startIdleAnimation();
            case HAPPY    -> startHappyAnimation();
            case EXCITED  -> startExcitedAnimation();
            case CHEERING -> startCheeringAnimation();
            case SLEEPING -> startSleepingAnimation();
            case THINKING -> startThinkingAnimation();
            case WORRIED  -> startWorriedAnimation();
        }
    }

    private void stopAnimation() {
        if (animation != null) animation.stop();
    }

    // ── IDLE: gentle bob every 2s, blink every 4s ──────────────────────────
    private void startIdleAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            animFrame += 0.1;
            bobOffset = Math.sin(animFrame) * 3;
            blinkTick++;
            blinkOpen = (blinkTick % 80 > 4); // quick blink
            redraw();
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    // ── HAPPY: smiley bounce ────────────────────────────────────────────────
    private void startHappyAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            animFrame += 0.15;
            bobOffset = Math.abs(Math.sin(animFrame)) * -6;
            redraw();
        }));
        animation.setCycleCount(80); // ~3 seconds
        animation.setOnFinished(ev -> setExpression(Expression.IDLE));
        animation.play();
    }

    // ── EXCITED: fast bounce + particles ────────────────────────────────────
    private void startExcitedAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(30), e -> {
            animFrame += 0.3;
            bounceY = Math.abs(Math.sin(animFrame)) * -12;
            particleTick++;
            redraw();
        }));
        animation.setCycleCount(120);
        animation.setOnFinished(ev -> setExpression(Expression.IDLE));
        animation.play();
    }

    // ── CHEERING: jump + confetti ────────────────────────────────────────────
    private void startCheeringAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(35), e -> {
            animFrame += 0.25;
            bounceY = Math.abs(Math.sin(animFrame)) * -14;
            particleTick++;
            redraw();
        }));
        animation.setCycleCount(150);
        animation.setOnFinished(ev -> setExpression(Expression.IDLE));
        animation.play();
    }

    // ── SLEEPING: slow rise-fall ─────────────────────────────────────────────
    private void startSleepingAnimation() {
        blinkOpen = false;
        animation = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            animFrame += 0.05;
            bobOffset = Math.sin(animFrame) * 2;
            redraw();
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    // ── THINKING: slow eye dart ──────────────────────────────────────────────
    private void startThinkingAnimation() {
        blinkOpen = true;
        animation = new Timeline(new KeyFrame(Duration.millis(60), e -> {
            animFrame += 0.08;
            bobOffset = Math.sin(animFrame * 0.3) * 2;
            redraw();
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    // ── WORRIED: small shake ─────────────────────────────────────────────────
    private void startWorriedAnimation() {
        animation = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            animFrame += 0.3;
            bobOffset = Math.sin(animFrame * 4) * 3; // shake
            redraw();
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }

    // ── DRAW ─────────────────────────────────────────────────────────────────
    private void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth(), h = getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2 + (current == Expression.WORRIED ? bobOffset : 0);
        double cy = h / 2 + bobOffset + bounceY;

        drawTurtle(gc, cx, cy);

        if (current == Expression.SLEEPING) drawZZZ(gc, cx + 30, cy - 45);
        if (current == Expression.THINKING) drawThinkBubble(gc, cx + 32, cy - 42);
        if (current == Expression.EXCITED || current == Expression.CHEERING)
            drawParticles(gc, cx, cy);
    }

    private void drawTurtle(GraphicsContext gc, double cx, double cy) {
        // Shell
        Color shellColor = switch (current) {
            case WORRIED  -> Color.web("#5A7050");
            case SLEEPING -> Color.web("#4A6845");
            default       -> Color.web("#4A7C59");
        };
        gc.setFill(shellColor);
        gc.fillOval(cx - 38, cy - 30, 76, 58);

        // Shell pattern
        gc.setFill(Color.web("#3A6045", 0.5));
        gc.fillOval(cx - 22, cy - 20, 44, 32);
        gc.setFill(Color.web("#2E5035", 0.35));
        gc.fillOval(cx - 10, cy - 10, 20, 16);

        // Shell outline
        gc.setStroke(Color.web("#2C4A30"));
        gc.setLineWidth(2);
        gc.strokeOval(cx - 38, cy - 30, 76, 58);

        // Head
        Color headColor = Color.web("#5B8C6A");
        gc.setFill(headColor);
        gc.fillOval(cx - 14, cy - 52, 28, 26);
        gc.setStroke(Color.web("#3A6045"));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - 14, cy - 52, 28, 26);

        // Eyes
        drawEyes(gc, cx, cy);

        // Mouth
        drawMouth(gc, cx, cy);

        // Flippers
        gc.setFill(Color.web("#4A7C59"));
        if (current == Expression.CHEERING || current == Expression.EXCITED) {
            // arms up
            gc.fillOval(cx - 58, cy - 38, 22, 14);
            gc.fillOval(cx + 36, cy - 38, 22, 14);
        } else {
            gc.fillOval(cx - 54, cy - 12, 18, 12);
            gc.fillOval(cx + 36, cy - 12, 18, 12);
        }
        gc.fillOval(cx - 28, cy + 20, 16, 12);
        gc.fillOval(cx + 12,  cy + 20, 16, 12);
    }

    private void drawEyes(GraphicsContext gc, double cx, double cy) {
        // Eye whites
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 10, cy - 48, 9, 8);
        gc.fillOval(cx + 1,  cy - 48, 9, 8);

        if (blinkOpen && current != Expression.SLEEPING) {
            // Pupils
            Color pupilColor = (current == Expression.WORRIED) ? Color.web("#8B1A1A") : Color.web("#2C1A0E");
            gc.setFill(pupilColor);
            double eyeDart = (current == Expression.THINKING) ? Math.sin(animFrame) * 2 : 0;
            gc.fillOval(cx - 8 + eyeDart, cy - 46, 5, 5);
            gc.fillOval(cx + 3 + eyeDart, cy - 46, 5, 5);
            // Highlight
            gc.setFill(Color.WHITE);
            gc.fillOval(cx - 7 + eyeDart, cy - 46, 2, 2);
            gc.fillOval(cx + 4 + eyeDart, cy - 46, 2, 2);
        } else {
            // Closed eyes (arcs)
            gc.setStroke(Color.web("#2C1A0E"));
            gc.setLineWidth(1.5);
            gc.strokeArc(cx - 10, cy - 47, 9, 6, 0, 180, javafx.scene.shape.ArcType.OPEN);
            gc.strokeArc(cx + 1,  cy - 47, 9, 6, 0, 180, javafx.scene.shape.ArcType.OPEN);
        }
    }

    private void drawMouth(GraphicsContext gc, double cx, double cy) {
        gc.setStroke(Color.web("#2C1A0E"));
        gc.setLineWidth(1.8);
        switch (current) {
            case HAPPY, CHEERING, EXCITED -> {
                // Big smile
                gc.strokeArc(cx - 10, cy - 36, 20, 14, 200, 140, javafx.scene.shape.ArcType.OPEN);
            }
            case WORRIED -> {
                // Frown
                gc.strokeArc(cx - 10, cy - 28, 20, 14, 20, 140, javafx.scene.shape.ArcType.OPEN);
            }
            case SLEEPING -> {
                // Tiny 'O'
                gc.strokeOval(cx - 4, cy - 32, 8, 6);
            }
            default -> {
                // Neutral slight smile
                gc.strokeArc(cx - 8, cy - 35, 16, 10, 210, 120, javafx.scene.shape.ArcType.OPEN);
            }
        }
    }

    private void drawZZZ(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#6B3A2A", 0.7));
        gc.setFont(javafx.scene.text.Font.font("Arial", 14 + Math.sin(animFrame) * 2));
        gc.fillText("z", x, y);
        gc.setFont(javafx.scene.text.Font.font("Arial", 10));
        gc.fillText("z", x + 12, y - 8);
    }

    private void drawThinkBubble(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#FDF6EC", 0.9));
        gc.setStroke(Color.web("#C49A6C"));
        gc.setLineWidth(1);
        gc.fillOval(x, y, 6, 6);
        gc.fillOval(x + 8, y - 6, 8, 8);
        gc.fillOval(x + 16, y - 14, 24, 20);
        gc.strokeOval(x + 16, y - 14, 24, 20);
        gc.setFill(Color.web("#6B3A2A"));
        gc.setFont(javafx.scene.text.Font.font("Arial", 11));
        gc.fillText("?", x + 22, y - 1);
    }

    private void drawParticles(GraphicsContext gc, double cx, double cy) {
        Color[] colors = { Color.web("#C49A6C"), Color.web("#4A7C59"),
                           Color.web("#8B4513"), Color.web("#F5ECD7"), Color.GOLD };
        for (int i = 0; i < 8; i++) {
            double angle  = (i * 45.0 + particleTick * 5) * Math.PI / 180;
            double radius = 50 + Math.sin(animFrame + i) * 10;
            double px = cx + Math.cos(angle) * radius;
            double py = cy + Math.sin(angle) * radius * 0.5;
            gc.setFill(colors[i % colors.length]);
            gc.fillOval(px - 3, py - 3, 6, 6);
        }
    }
}
