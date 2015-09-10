package utils.maths;

import java.util.Random;

import utils.corpus.DOCState;

public class RandUtils {
	

		public void randconparam(GammaDistrn alpha, int total_num_tables, int numIter,
				DOCState[] docStates) {
		  int iter, nd, zz;
		  double aa, bb, xx;
		  for ( iter = 0 ; iter < numIter ; iter++ ) {
		    aa = alpha.getShape();
		    bb = alpha.getScale();
		    for (int j = 0, len = docStates.length ; j < len ; j++ ) {
		      nd = docStates[j].docLen;
		      xx = RandBeta(alpha.getValue()+1.0, nd);
		      zz = Math.random() * (alpha.getValue() + nd) < nd ? 1 : 0;
		      aa += docStates[j].tablesNum - zz;
		      bb -= Math.log(xx);
		    }
		    alpha.m_gamma = RandGamma(aa) / bb;
		  }
		}
		
		 /**
	     * See paper written by Hisashi Tanizaki named
	     * A simple Gamma Random Number Generator for Arbitrary Shape Parameters
	     * @param aa \alpha
	     * @return double
	     */
		public static double RandGamma(double aa) {
			double n = 0.0;
			if (aa > 0.0 && aa <= 0.4) {
				n = 1 / aa;
			} else if (aa > 0.4 && aa <= 4) {
				n = 1 / aa + (1 / aa) * (aa - 0.4) / 3.6;
			} else if (aa > 4) {
				n = 1 / Math.sqrt(aa);
			} else {
				/* May be some error, return 0.0 and skip it */
				return 0.0;
			}
			double b1 = aa - 1 / n;
			double b2 = aa + 1 / n;
			double c1 = 0.0;
			if (aa > 0.0 && aa <= 0.4) {
				c1 = 0;
			} else if (aa > 0.4) {
				c1 = b1 * (Math.log(b1) - 1) / 2;
			}
			double c2 = b2 * (Math.log(b2) - 1) / 2;

			double y = 0.0;
			double w1 = 0.0;
			double w2 = 0.0;
			double x = 0.0;
			do {
				do {
					double v1 = Math.random();
					double v2 = Math.random();
					w1 = c1 + Math.log(v1);
					w2 = c2 + Math.log(v2);
					y = n * (b1 * w2 - b2 * w1);
				} while (y < 0);
				x = n * (w2 - w1);
			} while (y < x);
			return Math.exp(x);
		}

		public static double RandBeta(double aa, double bb) {
			aa = RandGamma(aa);
			bb = RandGamma(bb);
			return aa / (aa + bb);
		}

//		public static DoubleArrayList RandDir(DoubleArrayList alpha, DoubleArrayList beta) {
//			double sum = 0.0;
//			for (int i = 0; i < beta.size(); i++) {
//				beta.set(i, RandGamma(alpha.get(i)));
//				sum += beta.get(i);
//			}
//			for (int i = 0; i < beta.size(); i++) {
//				beta.set(i, beta.get(i) / sum);
//			}
//			return beta;
//		}

		public static int RandNumTables(double alpha, int numdata) {
			int retNumTable = 0;
			for (int i = 0; i < numdata; i++) {
				if (Math.random() < alpha / (i + alpha))
					retNumTable++;
			}
			return retNumTable;
		}


//		public static int RandMult(DoubleArrayList pi, int length) {
//			double sum = 0.0;
//			int cc = 0;
//			for (cc = 0; cc < length; cc++) {
//				sum += pi.getQuick(cc);
//			}
//			double rand = Math.random() * sum;
//			for (cc = 0; cc < length; cc++) {
//				rand -= pi.getQuick(cc);
//				if (rand < 0.0)
//					break;
//			}
//			return cc;
//		}

		public static int RandUniform(int numValue) {
			return (int) Math.floor(Math.random() * numValue);
		}
		
		
		public static void main(String[] args){ 
			System.out.println(RandUtils.RandBeta(1, 2));
		}
}
