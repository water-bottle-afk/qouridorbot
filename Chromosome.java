import java.util.Random;
public class Chromosome {
    // הגדרת אינדקסים קבועים כדי שתוכל לכתוב קוד קריא
    public static final int MY_DIST = 0;
    public static final int OPP_DIST = 1;
    public static final int DIST_DIFF = 2;
    public static final int MY_WALLS = 3;
    public static final int OPP_WALLS = 4;
    public static final int WALL_IMPACT = 5;

    public double[] genes = new double[6]; // כל הגנים במקום אחד
    private double fitness_score = 0;

    public Chromosome(boolean initializeRandomly) {
        Random r = new Random();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = r.nextDouble();
        }
    }

    public Chromosome() {
        
    }

    public double getWeight(int index) {
        return genes[index];
    }
}