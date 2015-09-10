/*
 * Copyright 2015 Zhuo KQ
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package hdp;

import java.util.List;
import utils.corpus.DOCState;
import utils.maths.GammaDistrn;

public class Perplexity {

	int K;//主题数
	int V;//词个数
	double eta;//phi~Dir(eta)
	int[] word_counts_by_z;//每个topic含有的词个数
	int[][] word_counts_by_zw;//[each topic, each word]词个数
	GammaDistrn alpha;//Gj|G0 ~ DP(a;G0);与 Teh et al. (2005) 论文一致
	GammaDistrn gamma;
	double beta; //beta~Dir(gamma)
	int totalTablesNum;//总的桌子数
	public int[] tablesNumByTopic;//一个主题拥有的桌子数
	DOCState[] docStates;//文档状态
	boolean sample_hyperparameter;	//是否抽样超参数
	int[][] phi;//主题-词矩阵, 主题中词的个数
	int trianWordsNum;//语料中 总词数，注意与字典词V的不同
	private double[] p;//概率
	private double[] f;//概率密度 函数
	int[] wordNumByTopic;
	int testTotalWordsNum;	
	DOCState[] docTestStates;
	List<int[][]> testDocsls;
	int[][] testTrnDocs;//测试部分的训练部分
	int[][] testCalcDocs;//测试部分的计算部分
	
	public int[] trn_tablesNumByTopic;//一个主题拥有的桌子数
	public int[] trn_wordNumByTopic;//一个主题中的词个数
	public int[][] trn_phi;//训练后的主题-词矩阵, 主题中词的个数,
	public int trn_totalTablesNum;//总的桌子数
	
	/**
	 * 计算每个词的perplexity
	 * @param K
	 * @param V
	 * @param eta
	 * @param word_counts_by_z
	 * @param word_counts_by_zw
	 * @param alpha
	 * @param gamma
	 * @param totalTablesNum
	 * @param tablesNumByTopic
	 * @param docStates
	 * @param sample_hyperparameter
	 * @return perplexity double
	 */
	public double getPerplexity(int K, int V, float eta, int[] word_counts_by_z, 
			int[][] word_counts_by_zw, GammaDistrn alpha, 
			GammaDistrn gamma, int totalTablesNum, int[] tablesNumByTopic,
			DOCState[] docStates, boolean sample_hyperparameter, int[][] phi,
			double beta, int totalWordsNum, int[] wordNumByTopic, int trainTotalWordsNum,
			int[][] testDocs, DOCState[] docTestStates, List<int[][]> testDocsls,
			int[] trn_tablesNumByTopic,int[] trn_wordNumByTopic, int[][] trn_phi,
			int trn_totalTablesNum, int[][] testTrnDocs, int[][] testCalcDocs){
		this.K = K;
		this.V = V;
		this.eta = eta;
		this.word_counts_by_z = word_counts_by_z;
		this.word_counts_by_zw = word_counts_by_zw;		
		this.alpha = alpha;
		this.gamma = gamma;
		this.totalTablesNum = totalTablesNum;
		this.tablesNumByTopic = tablesNumByTopic;
		this.docStates = docStates;
		this.sample_hyperparameter = sample_hyperparameter;
		this.phi = trn_phi.clone();
		this.trianWordsNum = totalWordsNum;
		this.beta = beta;
		this.testTrnDocs = testTrnDocs;
		this.testCalcDocs = testCalcDocs;
		this.docTestStates = docTestStates;
		this.trn_tablesNumByTopic = trn_tablesNumByTopic;
		this.trn_wordNumByTopic = trn_wordNumByTopic;
		this.trn_phi = trn_phi;
		this.trn_totalTablesNum = trn_totalTablesNum;
		
		return perplexity();
	}
	
	public double perplexity() {

//		trn_phi = phi;
//		Inference inf = new Inference(trn_tablesNumByTopic, trn_wordNumByTopic, trn_phi, 
//				trn_totalTablesNum, K);
//		inf.addInstances(testTrnDocs, V);
//		inf.addInstances(testCalcDocs, V);
//		testCalcDocs = testTrnDocs;
		
//		inf.inference(50);//开始推断
		
//		K = inf.K;
//		phi = inf.phi;
//		wordNumByTopic = inf.wordNumByTopic;
//		totalTablesNum = inf.totalTablesNum;
//		tablesNumByTopic = inf.tablesNumByTopic;		
//		docStates = inf.docStates;//获得推断后的相应值
		
		wordNumByTopic = trn_wordNumByTopic;
		totalTablesNum = trn_totalTablesNum;
		tablesNumByTopic = trn_tablesNumByTopic;
			
		double[][] theta = estimateTheta();
		double[][] phi = estimatePhi();
		
		int total_length = 0;
		double perplexity = 0.0;
		
		for (int d = 0, len = testCalcDocs.length; d < len; d++) {
			total_length += testCalcDocs[d].length;
			for (int w = 0, len2 = testCalcDocs[d].length; w < len2; w++) {
				double prob = 0.0;
				for (int k = 0; k < K; k++) {
					prob += theta[d][k] * phi[k][testCalcDocs[d][w]];
				}
				perplexity += Math.log(prob);
			}
		}
		perplexity = Math.exp(-1 * perplexity / total_length);
		return perplexity;
	}
	
	public double[][] estimateTheta() {
		double[][] theta = new double[docStates.length][K];
		
		for (int d = 0; d < docStates.length; d++) {
	    	for(int t=0 ; t < docStates[d].tablesNum; t++){
	    		if(docStates[d].wordCountByTable[t] > 0){
	    			int k = docStates[d].tableToTopic[t];
	    			theta[d][k] += docStates[d].wordCountByTable[t];
	    		}
	    	}
	    	for(int k = 0; k < K; k++) {
	    		if(word_counts_by_z[k] > 0){
	    		theta[d][k] += alpha.getValue() * tablesNumByTopic[k] / 
	    						(gamma.getValue() + totalTablesNum);
	    		theta[d][k] /= docStates[d].docLen + alpha.getValue() ;
	    		}
	    	}
		}
		return theta;
	}
	
	public double[][] estimatePhi() {
		double[][] phi = new double[K][V];
		double v_eta = V * eta;
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++) {
				phi[k][w] = (trn_phi[k][w] + eta) /
					(word_counts_by_z[k] + v_eta);
			}
		}
		return phi;
	}	
}
