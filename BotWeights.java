import java.util.Random;

public class BotWeights {
    // הגדרת כל המשקולות כמשתנים במחלקה
    public double my_dist;
    public double opp_dist;
    public double distDiff;
    public double my_wallsLeft;
    public double opp_wallsLeft;
    public double wallImpact;

    // בנאי (Constructor) לאתחול אקראי
    public BotWeights() {
        Random r = new Random();
        this.my_dist = r.nextDouble();
        this.opp_dist = r.nextDouble();
        this.distDiff = r.nextDouble();
        this.my_wallsLeft = r.nextDouble();
        this.opp_wallsLeft = r.nextDouble();
        this.wallImpact = r.nextDouble();
        
    }
}