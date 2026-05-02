package view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Center animation surface. Draws the static supermarket layout
 * (entrance → shopping area → checkout decision → regular/self queues → exit)
 * and animates customer flow as colored dots.
 *
 * Implements IVisualisation so the existing Controller can drive it
 * via newCustomer(), customerToCheckout() and customerLeft().
 */
public class SimulatorVisualisation extends Canvas implements IVisualisation {

    private final GraphicsContext gc;

    private int customerCount = 0;
    private int regularQueueCount = 0;
    private int selfQueueCount = 0;
    private final List<double[]> shoppingDots = new ArrayList<>();

    public SimulatorVisualisation(int w, int h) {
        super(w, h);
        gc = getGraphicsContext2D();
        clearDisplay();
    }

    /* ===================== IVisualisation ===================== */

    @Override
    public void clearDisplay() {
        customerCount = 0;
        regularQueueCount = 0;
        selfQueueCount = 0;
        shoppingDots.clear();
        redraw();
    }

    @Override
    public void newCustomer() {
        customerCount++;
        // place a dot somewhere in the shopping area
        double x = 220 + Math.random() * 220;
        double y = 70  + Math.random() * 30;
        shoppingDots.add(new double[]{x, y});
        // keep latest 8 dots visible to avoid clutter
        if (shoppingDots.size() > 8) shoppingDots.remove(0);
        redraw();
    }

    @Override
    public void customerToCheckout(boolean regular) {
        if (regular) regularQueueCount++; else selfQueueCount++;
        if (!shoppingDots.isEmpty()) shoppingDots.remove(0);
        redraw();
    }

    @Override
    public void customerLeft(boolean regular) {
        if (regular && regularQueueCount > 0) regularQueueCount--;
        if (!regular && selfQueueCount > 0)   selfQueueCount--;
        redraw();
    }

    /* ===================== Drawing ===================== */

    private void redraw() {
        double W = getWidth();
        double H = getHeight();

        // background
        gc.setFill(Color.web("#f7f3ea"));
        gc.fillRect(0, 0, W, H);

        // ---- Top row: Entrance + Shopping area ----
        drawBox(50,  40, 120, 60, "#cfe1f8", "#1e3a8a", "Entrance");
        drawBox(200, 40, 320, 60, "#bfead0", "#0f5132", "Shopping area");
        // dots in shopping area
        gc.setFill(Color.web("#3730a3"));
        for (double[] d : shoppingDots) {
            gc.fillOval(d[0], d[1], 8, 8);
        }

        // ---- Decision label ----
        gc.setFill(Color.web("#374151"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 13));
        gc.fillText("Checkout decision point", 230, 130);

        // ---- Two checkouts ----
        drawBox(80,  170, 200, 80, "#fed7aa", "#7c2d12",
                "Regular checkout   Q: " + regularQueueCount);
        drawBox(310, 170, 200, 80, "#fbcfe8", "#831843",
                "Self-checkout       Q: " + selfQueueCount);
        // dots in queues
        gc.setFill(Color.web("#7c2d12"));
        for (int i = 0; i < Math.min(regularQueueCount, 8); i++) {
            gc.fillOval(95 + i * 18, 225, 8, 8);
        }
        gc.setFill(Color.web("#831843"));
        for (int i = 0; i < Math.min(selfQueueCount, 8); i++) {
            gc.fillOval(325 + i * 18, 225, 8, 8);
        }

        // arrows from decision to checkouts
        drawArrow(290, 130, 180, 175);
        drawArrow(310, 130, 410, 175);

        // ---- Exit ----
        drawBox(220, 290, 160, 50, "#d1d5db", "#374151", "Exit");
        drawArrow(180, 250, 250, 290);
        drawArrow(410, 250, 350, 290);

        // ---- Counter overlay ----
        gc.setFill(Color.web("#4b5563"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.fillText("Customers entered: " + customerCount, 12, H - 10);
    }

    private void drawBox(double x, double y, double w, double h,
                         String fill, String stroke, String text) {
        gc.setFill(Color.web(fill));
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setStroke(Color.web(stroke));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, w, h, 12, 12);
        gc.setFill(Color.web(stroke));
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        gc.fillText(text, x + 12, y + 22);
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(1.5);
        gc.strokeLine(x1, y1, x2, y2);
        // simple arrow head
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double headLen = 8;
        gc.strokeLine(x2, y2,
                x2 - headLen * Math.cos(angle - Math.PI / 7),
                y2 - headLen * Math.sin(angle - Math.PI / 7));
        gc.strokeLine(x2, y2,
                x2 - headLen * Math.cos(angle + Math.PI / 7),
                y2 - headLen * Math.sin(angle + Math.PI / 7));
    }

    /* ===================== Getters for status bar ===================== */

    public int getCustomerCount()      { return customerCount; }
    public int getRegularQueueCount()  { return regularQueueCount; }
    public int getSelfQueueCount()     { return selfQueueCount; }
}
