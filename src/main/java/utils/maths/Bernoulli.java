package utils.maths;

public class Bernoulli
{
	public static int binomial(double p) {
		int v = 0;		
		if (StdRandom.bernoulli(p)) v = 1;		
		return v;
    }
	
	public static void main(String[] args){
		Bernoulli b = new Bernoulli();
		System.out.println(b.binomial(0.5));
	}
}