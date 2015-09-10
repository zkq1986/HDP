package utils.maths;

public class Gamma2 {

    private static final double LOG_2PI = Math.log(2 * Math.PI); // log(2PI)
    private static final int N = 8;
    private static final double B0  = 1.0;           // 以下は Bernoulli 数 TODO
    private static final double B1  = (-1.0 / 2.0);    // TODO
    private static final double B2  = ( 1.0 / 6.0);
    private static final double B4  = (-1.0 / 30.0);
    private static final double B6  = ( 1.0 / 42.0);
    private static final double B8  = (-1.0 / 30.0);
    private static final double B10 = ( 5.0 / 66.0);
    private static final double B12 = (-691.0 / 2730.0);
    private static final double B14 = ( 7.0 / 6.0);
    private static final double B16 = (-3617.0 / 510.0);

    public static double loggamma(double x) { // ガンマ関数の対数
        double v = 1.0;
        while (x < N) { v *= x; x++; }
        double w = 1 / (x * x);
        double ret =    B16 / (16 * 15);
        ret = ret * w + B14 / (14 * 13);
        ret = ret * w + B12 / (12 * 11);
        ret = ret * w + B10 / (10 *  9);
        ret = ret * w + B8  / ( 8 *  7);
        ret = ret * w + B6  / ( 6 *  5);
        ret = ret * w + B4  / ( 4 *  3);
        ret = ret * w + B2  / ( 2 *  1);
        ret = ret / x + 0.5 * LOG_2PI - Math.log(v) - x + (x - 0.5) * Math.log(x);
        return ret;
    }

    public static double lgamma(double x) { // ガンマ関数の対数        
        return loggamma(x);
    }
    
    public static double gamma(double x) { // ガンマ関数
        if (x < 0.0)
            return  Math.PI / (Math.sin(Math.PI * x) * Math.exp(loggamma(1 - x)));
        else
            return Math.exp(loggamma(x));
    }

}