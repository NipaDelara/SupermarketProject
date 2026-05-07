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
 * <p>
 * Implements IVisualisation so the existing Controller can drive it
 * via newCustomer(), customerToCheckout() and customerLeft().
 */
public class SimulatorVisualisation extends Canvas implements IVisualisation {

    private final GraphicsContext gc;
    private int entranceCount = 0;
    private int shoppingCount = 0;
    private int customerCount = 0;
    private int decisionCount = 0;
    private int regularQueueCount = 0;
    private int selfQueueCount = 0;
    private int exitCount = 0;

    private final List<double[]> shoppingDots = new ArrayList<>();

    public SimulatorVisualisation(int w, int h) {
        super(w, h);
        gc = getGraphicsContext2D();

        widthProperty().addListener(e -> redraw());
        heightProperty().addListener(e -> redraw());

        clearDisplay();
    }

    /* ===================== IVisualisation ===================== */

    @Override
    public void clearDisplay() {
        customerCount = 0;
        entranceCount = 0;
        shoppingCount = 0;
        decisionCount = 0;
        regularQueueCount = 0;
        selfQueueCount = 0;
        exitCount = 0;

        shoppingDots.clear();
        redraw();
    }

    @Override
    public void newCustomer() {
        customerCount++;
        entranceCount++;

        // place a dot somewhere in the shopping area
        double x = 220 + Math.random() * 220;
        double y = 70  + Math.random() * 30;

        shoppingDots.add(new double[]{x, y});

        // keep latest 8 dots visible to avoid clutter
        if (shoppingDots.size() > 8) {
            shoppingDots.remove(0);
        }
        redraw();
    }

    @Override
    public void customerToCheckout(boolean regular) {
        if (regular){
            regularQueueCount++;
        } else{
            selfQueueCount++;
        }
        if (!shoppingDots.isEmpty()) {
            shoppingDots.remove(0);
        }
        redraw();
    }

    @Override
    public void customerLeft(boolean regular) {
        if (regular && regularQueueCount > 0){
            regularQueueCount--;
        }
        if (!regular && selfQueueCount > 0){
            selfQueueCount--;
        }
        exitCount++;
        redraw();
    }
    //show customer stage
    @Override
    public void showCustomerStages(
            int entrance,
            int shopping,
            int decision,
            int regularCheckout,
            int selfCheckout,
            int exit
    ){
        this.entranceCount = Math.max(0, entrance);
        this.shoppingCount = Math.max(0, shopping);
        this.decisionCount = Math.max(0, decision);
        this.regularQueueCount = Math.max(0, regularCheckout);
        this.selfQueueCount = Math.max(0, selfCheckout);
        this.exitCount = Math.max(0, exit);

        this.customerCount =
                this.entranceCount +
                        this.shoppingCount +
                        this.decisionCount +
                        this.regularQueueCount +
                        this.selfQueueCount +
                        this.exitCount;

        rebuildShoppingDots();
        redraw();
    }
    // UPDATED: keeps shopping dots synced with shoppingCount
    private void rebuildShoppingDots() {
        shoppingDots.clear();

        int visible = Math.min(shoppingCount, 12);

        for (int i = 0; i < visible; i++) {
            double x = 220 + Math.random() * 280;
            double y = 65 + Math.random() * 25;
            shoppingDots.add(new double[]{x, y});
        }
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

        // UPDATED: entrance dots
        drawDots(65, 78, entranceCount, "#2563eb", 5, 10);

        // dots in shopping area
        gc.setFill(Color.web("#3730a3"));
        for (double[] d : shoppingDots) {
            gc.fillOval(d[0], d[1], 8, 8);
        }

        // ---- Decision label ----
        gc.setFill(Color.web("#374151"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 13));
        gc.fillText("Checkout decision point", 230, 130);

        // UPDATED: decision dots
        drawDots(285, 145, decisionCount, "#4f46e5", 5, 10);

        // ---- Two checkouts ----
        drawBox(80,  170, 200, 80, "#fed7aa", "#7c2d12",
                "Regular checkout   Q: " + regularQueueCount);
        drawBox(310, 170, 200, 80, "#fbcfe8", "#831843",
                "Self-checkout       Q: " + selfQueueCount);

        // UPDATED: checkout queue dots
        drawDots(95, 225, regularQueueCount, "#7c2d12", 8, 18);
        drawDots(325, 225, selfQueueCount, "#831843", 8, 18);

        drawArrow(290, 130, 180, 175);
        drawArrow(310, 130, 410, 175);

        drawBox(220, 290, 160, 50, "#d1d5db", "#374151", "Exit");

        drawArrow(180, 250, 250, 290);
        drawArrow(410, 250, 350, 290);

        // UPDATED: exit dots
        drawDots(235, 515, exitCount, "#111827", 10, 15);

        // dots in queues
        gc.setFill(Color.web("#111827"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Exited: " + exitCount, 235, 375);

        // arrows from decision to checkouts
        drawArrow(290, 130, 180, 175);
        drawArrow(310, 130, 410, 175);

        // ---- Exit ----
        drawBox(220, 290, 160, 50, "#d1d5db", "#374151", "Exit");
        drawArrow(180, 250, 250, 290);
        drawArrow(410, 250, 350, 290);

        gc.setFill(Color.web("#111827"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Exited: " + exitCount, 235, 325);

        drawDots(235, 340, exitCount, "#111827", 8, 15);

        // ---- Counter overlay ----
        gc.setFill(Color.web("#4b5563"));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.fillText("Customers entered: " + customerCount, 12, H - 10);
    }
    // UPDATED: reusable dot drawing method
    private void drawDots(
            double startX,
            double startY,
            int count,
            String color,
            int maxVisible,
            int spacing
    ) {
        gc.setFill(Color.web(color));

        int visible = Math.min(count, maxVisible);

        for (int i = 0; i < visible; i++) {
            gc.fillOval(startX + i * spacing, startY, 8, 8);
        }

        if (count > maxVisible) {
            gc.setFill(Color.web("#111827"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 11));
            gc.fillText("+" + (count - maxVisible),
                    startX + maxVisible * spacing + 4,
                    startY + 8);
        }
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

    public int getCustomerCount(){
        return customerCount;
    }
    public int getRegularQueueCount(){
        return regularQueueCount;
    }
    public int getSelfQueueCount(){
        return selfQueueCount;
    }
}
