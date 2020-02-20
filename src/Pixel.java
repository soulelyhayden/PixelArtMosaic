import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

class Pixel {
    private Functions fct = new Functions();

    private double[] colValTemp = new double[]{0, 0, 0};

    private Color colValPoor;
    private Color colValRich;

    Color colour;

    private double wealth, influence, surroundingWealth, surroundingInfluence, survival, surroundingSurvival;
    int xPos, yPos;

    private double radius;

    private ArrayList<Pixel> neighbours = new ArrayList<>();

    //iniate and define things
    Pixel(int x, int y, double wealth, double influence, Color colValRich, Color colValPoor) {
        this.xPos = x;
        this.yPos = y;
        this.wealth = wealth;
        this.influence = influence;

        this.colValRich = colValRich;
        this.colValPoor = colValPoor;
    }

    void update() {
        //determines how easy it is to steal wealth from and it's drive to steal wealth basically
        survival = fct.constrain((fct.map(influence, 0, 1, 1, 0) + fct.map(wealth,0,1,1,0)) / 2, 0, 1);

        //effects larger areas as influence and wealth grow - influence is more important than wealth
        radius = ((influence + fct.constrain(wealth, 0.1, 0.5)) + 1);

        //reset values on each update for new neighbour set
        neighbours.clear();
        surroundingWealth = 0;
        surroundingInfluence = 0;

        //iterate through neighbours
        for (int y = -(int)radius; y <= radius; y++) {
            for (int x = -(int)radius; x <= radius; x++) {
                if (!(x == 0 && y == 0) && xPos + x >= 0 && yPos + y >= 0 && xPos + x < Main.GRID_SIZE && yPos + y < Main.GRID_SIZE) {
                    Pixel neighbour = Main.pixels[x + xPos][y + yPos];
                    neighbours.add(neighbour);

                    surroundingWealth += neighbour.wealth;
                    surroundingInfluence += neighbour.influence;
                    surroundingSurvival += neighbour.survival;
                }
            }
        }

        changeInfluence();

        //same deal as main update method - avoids iterating through same way each time
        Collections.shuffle(neighbours);
        for (Pixel n : neighbours) {
            distributeWealth(n);
        }

        updateCol();
    }

    //update pixel influence - affects pixel's ability to steal wealth
     private void changeInfluence() {

        //n is the number of neighbours determined by the radius
        int n = (int)(radius * ((radius / 2) + 0.5)) * 8;

        //if you're super wealth the people closest to you will receive some of that wealth
        if (wealth > n && surroundingWealth / n < 0.5) {
            for (Pixel p : neighbours) {
                if (p.wealth < 0.1 && fct.random() > 0) {
                    wealth -= 0.2;
                    p.wealth += 0.2;
                }
            }
        }

        //if the average wealth around you is super high you will become more influential
        if (surroundingWealth / n > 0.9) {
            influence += 0.1;
        }

        //if your surrounded by influential people there is a chance you become influential as well by association
        if (surroundingInfluence / n > 0.9) {
            if (fct.random() > 0) {
                influence += 0.3;
            }

        }

        //if there's lots of poverty around you you will be less influential
        if (surroundingWealth / n < 0.3) {
            influence -= 0.1;
        }

        //if you have more desire to survive you will become more influential
        if (survival > surroundingSurvival / n) {
            influence += 0.1;
        }

        //equivalent to the lottery- happens very infrequently but provides you and some of your neighbours with wealth and influence
        if ((int)fct.random(0,Math.pow(Main.GRID_SIZE, 5)) == 1) {
            influence = 1;
            for (Pixel p : neighbours) {
                if (fct.random() > 0) {
                    p.influence = 1;
                }
            }
            System.out.println("Winner!");
        }

        //influence stays within 0-1 for easy manipulation
        influence = fct.constrain(influence, 0, 1);
    }

    //distribute pixel wealth - main economic factor
    private void distributeWealth(Pixel neighbour) {
        double distribute;

        //if you are more influential this process allows you to steal more wealth
        if (influence > neighbour.influence) {
            //wealth stolen is based on the percent difference of each neighbours influence
            double infDiff = fct.percentDifference(influence, neighbour.influence);

            if (neighbour.wealth > 0) {
                if (infDiff > 0.5) {
                    distribute = infDiff / 3;

                    //make sure you aren't taking more than you can - but influential pixels will always take something if its available
                    double steal = Math.min(distribute, neighbour.wealth);

                    wealth += steal;
                    Main.pixels[neighbour.xPos][neighbour.yPos].wealth -= steal;
                }
            }
        //keeps the economy moving - don't have to have influence to exchange wealth
        } else {
            //maximum amount that can change
            double maxChange = (Math.max(wealth, neighbour.wealth) - Math.min(wealth, neighbour.wealth));

            //amount exhanged between non-influential pixels is determined by their survival value
            double survivalDiff = fct.constrain(fct.percentDifference(survival, neighbour.survival), 0, 1);
            distribute = fct.random(survival / 2,maxChange) / ((survivalDiff * 2) + 2);

            //make sure you're not taking more than you can - no guarantee that an exchange will happen, unlike more influential pixels
            if (neighbour.wealth - distribute >= 0) {
                wealth += distribute;
                Main.pixels[neighbour.xPos][neighbour.yPos].wealth -= distribute;
            }


        }
    }

    //update pixel colour based on wealth value
      public void updateCol() {
        //standard deviation of random gaussian colour based on wealth - very wealthy pixels will flash more extreme differences from their base colour
        int sDCol = (int)fct.map(wealth, 0, 5, 5, 25);

        //interpolation between rich and poor colours
        double interpCol = fct.constrain(1 - (wealth - 0.3), 0, 1);
        double invInterpCol = 1 - interpCol;

        colValTemp[0] = colValPoor.getRed()   * interpCol   +   colValRich.getRed()   * invInterpCol;
        colValTemp[1] = colValPoor.getGreen() * interpCol   +   colValRich.getGreen() * invInterpCol;
        colValTemp[2] = colValPoor.getBlue()  * interpCol   +   colValRich.getBlue()  * invInterpCol;

        //random gaussian distribution of the interpolated colour
        for (int j = 0; j < 3; j++) {
            double rnd = fct.randomGaussian(sDCol, 0);
            colValTemp[j] += rnd;
            colValTemp[j] = fct.constrain(colValTemp[j], 0, 255);
        }

        //alpha is also affected by wealth
        double alpha = fct.constrain(wealth * 255, 0, 255);

        //apply colour
        colour = new Color((int)colValTemp[0], (int)colValTemp[1], (int)colValTemp[2], (int)alpha);
    }

}
