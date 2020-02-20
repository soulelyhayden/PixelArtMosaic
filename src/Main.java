import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    private static Functions fct = new Functions();

    private static Display display;
    private boolean pressed = false;
    private int screenshot = 0;


    private long startTime;
    private long elapsedTime;
    //refresh's per second
    private int tick = 30;

    static final int GRID_SIZE = 100;
    private static final int SCALE = 5;

    private static final int SEED = (int)(fct.random() * 1000);
    static Pixel[][] pixels =  new Pixel[GRID_SIZE][GRID_SIZE];
    private static ArrayList<Pixel> pixelList = new ArrayList<>();

    private Color colValPoor, colValRich;
    private static final Color poorColour = new Color(80, 165, 200);
    private static final Color richColour = new Color(240, 225, 190);

    private void setup() {
        colValPoor = fct.randomCol(poorColour);
        colValRich = fct.randomCol(richColour);

        randomizeGrid();
    }

    //Main program loop
    private void update() {

        //pauses when space is pressed
        if (!keyListener.isSpace()) {
            pressed = false;

            //using an arraylist and shuffling helps avoid things moving in one direction based on iterating through it the same way each time
            Collections.shuffle(pixelList);

            for (Pixel p : pixelList) {
                p.update();
                //p.updateCol();
            }

        //take screenshot when space is pressed - make sure it only takes one
        } else if (!pressed){
            pressed = true;
            screenshot += 1;
            BufferedImage screen = display.screenShot();
            try {
                ImageIO.write(screen, "png", new File("Screenshot_" + screenshot + ".png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        display.update();
    }

    /*Sets up the initial pixel cluster, two main noise values are used here both based on Perlin noise function for a
    "natural" setup. Each pixel is assigned a wealth and influence value based on this noise.
     */
    private void randomizeGrid() {
        for(int y = 0; y < GRID_SIZE; y++) {
            for(int x = 0; x < GRID_SIZE; x++) {
                double dx = (double) x / GRID_SIZE;
                double dy = (double) y / GRID_SIZE;
                int frequency = GRID_SIZE / 10;
                //influence distribution - adjust this carefully to determine initial influence of pixels
                double influence = (fct.PerlinNoise((dx * frequency) + SEED, (dy * frequency) + SEED, 0.8, 2) - 0.9) * 2;
                //adjust this to adjust initial wealth distribution. The last number affects the total wealth available.
                double wealth = fct.PerlinNoise((dx * frequency) + SEED, (dy * frequency) + SEED, 1, 3) - 0.8;

                wealth = fct.constrain(fct.map(wealth, -1, 1, 0, 1), 0, 1);
                influence = fct.constrain((wealth / 2 + fct.constrain(influence,0,1)), 0, 1);

                //create new pixel with defined values
                pixels[x][y] = new Pixel(x, y, wealth, influence, colValRich, colValPoor);
                pixelList.add(pixels[x][y]);

            }
        }
    }

    //start program main loop and keep timing accurate
    private void startLoop() {
        setup();
        display = new Display(GRID_SIZE, SCALE);

        SwingWorker<Object, Object> sw = new SwingWorker<>() {
            @Override
            protected Object doInBackground() throws Exception {
                while (true) {
                    startTime = System.nanoTime();
                    update();
                    elapsedTime = (System.nanoTime() - startTime) / 1000000;
                    //System.out.println(elapsedTime);
                    if ((1000 / tick) - elapsedTime > 0) {
                        Thread.sleep((1000 / tick) - elapsedTime);
                    }
                }
            }
        };

        sw.execute();
    }

    //Main application launch
    static public void main(String[] passedArgs) {
        //run the key listener
        keyListener.main(passedArgs);

        SwingUtilities.invokeLater(() -> {
            Main updateThread = new Main();
            updateThread.startLoop();
        });
    }
}
