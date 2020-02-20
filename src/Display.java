import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display extends JPanel {

    private int SCALE;
    private int GRID_SIZE;

    Display(int GRID_SIZE, int SCALE) {
        //setup screen stuff
        super();

        this.SCALE = SCALE;
        this.GRID_SIZE = GRID_SIZE;

        JFrame frame = new JFrame("Game");
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);

        Color bgCol = new Color(22, true);
        frame.getContentPane().setBackground(bgCol);
        frame.setVisible(true);
        frame.pack();

        frame.setSize(new Dimension(GRID_SIZE * SCALE, GRID_SIZE * SCALE + frame.getInsets().top));
        frame.setPreferredSize(new Dimension(GRID_SIZE * SCALE, GRID_SIZE * SCALE+ frame.getInsets().top));

        //frame.setResizable(false);

    }

    //draw pixel components
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Have to iterate through normal array not array list because of concurrent modification of array list order through shuffle can cause weird graphical problems.
        for(int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                Pixel p = Main.pixels[x][y];
                g.setColor(p.colour);
                g.fillRect(p.xPos * SCALE, p.yPos * SCALE, SCALE, SCALE);
            }
        }
    }

    void update() {
        repaint();
    }

    //record screenshot
    BufferedImage screenShot() {
        Container content = this;
        BufferedImage screen = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = screen.createGraphics();

        content.printAll(g2d);

        g2d.dispose();
        return screen;
    }
}
