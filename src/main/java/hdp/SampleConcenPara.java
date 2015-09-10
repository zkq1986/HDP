package hdp;

import cern.jet.random.Beta;
import cern.jet.random.Gamma;
import cern.jet.random.Uniform;
import utils.corpus.DOCState;
import utils.maths.Bernoulli;
import utils.maths.BetaRandom;
import utils.maths.Gamma2;
import utils.maths.GammaDistribution;
import utils.maths.GammaDistrn;
import utils.maths.RandUtils;
import utils.maths.RandomNumberGenerator;
import utils.maths.StdRandom;

/**
 * 集中参数，重采样
 * @author Zhuo
 *
 */
public class SampleConcenPara {

	public void sampleGamma(GammaDistrn gamma, int total_num_tables, int iterMax,
			DOCState[] docStates){
	
	    /// (page 585 in (Escobar & West 1995))
		RandUtils rut = new RandUtils();
		rut.randconparam(gamma, total_num_tables, iterMax, docStates);			
	}
	
	public void sampleGamma(GammaDistrn gamma, int total_num_tables, int iterMax,
		DOCState[] docStates, int K){

	    /// (page 585 in (Escobar & West 1995))
	    double shape = gamma.getShape();
	    double scale = gamma.getScale();
	    int n = total_num_tables;
	    int k = K;
		double init_gamma = gamma.m_gamma;
		double sum_gamma = 0.0;
		
		for (int step = 0; step < iterMax; step++) {
			double eta = RandUtils.RandBeta(init_gamma + 1, n);
		    double pi = (shape + k - 1) / ( (shape + k - 1) + n * (scale - Math.log(eta)) );
		    
		    sum_gamma += pi * new GammaDistrn(shape + k, 1.0 / (scale - Math.log(eta))).m_gamma + 
		    	(1 - pi) * new GammaDistrn(shape + k - 1, 1.0 / (scale - Math.log(eta))).m_gamma;
		}
		
		gamma.m_gamma = sum_gamma / iterMax;
		System.out.println("resample gamma : " + gamma.m_gamma);
	}

	public void sampleAlpha(GammaDistrn gamma, int total_num_tables, int iterMax,
			DOCState[] docStates, int K){

	    /// (page 585 in (Escobar & West 1995))

	    double shape = gamma.getShape();
	    double scale = gamma.getScale();
	    int n = total_num_tables;
		double init_gamma = gamma.m_gamma;
		double sum_gamma = 0.0;
		double rate, sum_log_w, sum_s;

		for (int step = 0; step < iterMax; step++) {
			sum_log_w = 0.0;
			sum_s = 0.0;

			for (int d = 0; d < docStates.length; d++) {

				sum_log_w += Math.log(RandUtils.RandBeta(gamma.m_gamma + 1, docStates[d].docLen));
				double pi = (double)docStates[d].docLen
					/ (docStates[d].docLen + gamma.m_gamma);
				int cc = Bernoulli.binomial(pi);
				sum_s += cc;
			}
			rate = 1.0 / scale - sum_log_w;
			gamma.m_gamma = (new GammaDistrn(shape + n - sum_s, 1.0 / rate)).getValue();
		}
		
		System.out.println("resample alpha : " + gamma.m_gamma);
	}
	
	public void sampleAlpha(GammaDistrn alpha, int total_num_tables, int iterMax,
			DOCState[] docStates){	
		RandUtils rut = new RandUtils();
		rut.randconparam(alpha, total_num_tables, iterMax, docStates);
	}
	
	public void sampleGamma2(GammaDistrn gamma, int total_num_tables, int num_topics){
		
	    /// (page 585 in (Escobar & West 1995))
	    double shape = gamma.getShape();
	    double scale = gamma.getScale();
	    int n = total_num_tables;
	    int k = num_topics;

	    double eta = BetaRandom.generateBeta(new RandomNumberGenerator(System.currentTimeMillis()), 
	    		1, gamma.m_gamma + 1, n);
	    double pi = shape + k - 1;
	    double rate = 1.0 / scale - Math.log(eta);
	    pi = pi / (pi + rate * n);

	    boolean cc = StdRandom.bernoulli(pi);
	    if (cc == true)
	    	gamma.m_gamma = (new GammaDistrn(shape + k, 1.0 / rate)).getValue();
	    else
	    	gamma.m_gamma = (new GammaDistrn(shape + k - 1, 1.0 / rate)).getValue();
	}
	
	public void sampleAlpha2(GammaDistrn alpha, int total_num_tables, int iterMax,
			DOCState[] docStates){	
	    double  shape = alpha.getShape();
	    double  scale = alpha.getScale();

	    int n = total_num_tables;
	    double rate, sum_log_w, sum_s;

	    for (int step = 0; step < iterMax; step++){
	        sum_log_w = 0.0;
	        sum_s = 0.0;
	        for (int d = 0, len = docStates.length; d < len; d++){
	        	if(docStates[d].docLen > 0){
		            sum_log_w += Math.log(BetaRandom.generateBeta(new RandomNumberGenerator(System.currentTimeMillis()), 
		    	    		1, alpha.m_gamma + 1, docStates[d].docLen));
	        	
		            if(StdRandom.bernoulli((double)docStates[d].docLen / (docStates[d].docLen + alpha.m_gamma))){
		            	sum_s ++;
		            }
	            }
	        }
	        rate = 1.0 / scale - sum_log_w;
	        alpha.m_gamma = (new GammaDistrn(shape + n - sum_s, 1.0 / rate)).getValue();
	    }
	}
}
