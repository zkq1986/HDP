package utils.maths;

/**
 *  Description : ${CLASS_DESCRIPTION}
 */
public class BetaRandom {
    public static double generateBeta(RandomNumberGenerator random, double maxX, double alpha, double beta) {
        double b = random.nextBeta(alpha, beta);
        return maxX * b;
    }
}
