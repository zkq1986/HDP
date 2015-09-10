/**
 * 参照Yee Whye the 的《hierarchical dirichlet process》(2006) 实现
 * 采用CRF gibbs抽样
 * 代码中的参数含义与论文完全一致
 * @author Zhuo KQ
 * @since 2015.3
 */

package hdp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import utils.corpus.DOCState;
import utils.corpus.WordState;
import utils.maths.GammaDistrn;

public class Inference {

	//----------------------
	//为Dirichlet process的参数，这三者的值 始终固定不变，为经验值
	public double eta  = 0.01; // 主题-词 phi 的先验参数
//	public double gamma = (new GammaDistribution(10,1)).getValue();//G0~DP(r(读作gamma),H)
//	public double gamma = 3;//G0~DP(r(读作gamma),H)
//	public double alpha = (new GammaDistrn(2,1)).getValue();//Gj ~ DP(a(读作alpha), G0)
	
	GammaDistrn gamma = new GammaDistrn(5,1);//G0~DP(r(读作gamma),H)
	GammaDistrn alpha = new GammaDistrn(1,1);//Gj ~ DP(a(读作alpha), G0)
	//----------------------//
	
	private Random random = new Random();
	private double[] p;//概率
	private double[] f;//概率密度 函数
	
	public DOCState[] docStates;//文档状态
	public int[] tablesNumByTopic;//一个主题拥有的桌子数
	public int[] wordNumByTopic;//一个主题中的词个数	
	public int[][] phi;//主题-词矩阵, 主题中词的个数,
	public int totalTablesNum;//总的桌子数
	
	public int[] trn_tablesNumByTopic;//一个主题拥有的桌子数
	public int[] trn_wordNumByTopic;//一个主题中的词个数
	public int[][] trn_phi;//训练后的主题-词矩阵, 主题中词的个数,
	public int trn_totalTablesNum;//总的桌子数
	
	public int V;//单词数
	public int totalWordsNum;//总的词数，与单词数的区别在于n个相同的单词，单词数只计1，而总的词数计n
	public int K = 5;//主题数，初始主题只为1
	
	
	//---------测试--------------//
	public DOCState[] docTestStates;//测试语料文档状态
	int totalTestWordsNum;//测试语料总的词数
	int trainCount = 8;//采用8-2交叉验证
	int testCount = 2;
	private int[][] testDocs;
	private List<int[][]> testDocsls;
	
	public Inference(int[] trn_tablesNumByTopic, int[] trn_wordNumByTopic, 
			int[][] trn_phi, int trn_totalTablesNum, int K){
		this.trn_tablesNumByTopic = trn_tablesNumByTopic;
		this.trn_wordNumByTopic = trn_wordNumByTopic;
		this.trn_phi = trn_phi;
		this.trn_totalTablesNum = trn_totalTablesNum;
		this.K = K;
	}
	
	//inference new model ~ getting data from a specified dataset
	public void inference(int nIters){

		System.out.println("Sampling " + nIters + " iteration for inference!");		
		for (int iter = 0; iter < nIters; iter++){
			nextGibbsSweep();
		}		
//		System.out.println("Gibbs sampling for inference completed!");
//		System.out.println("Topic Number for inference: " + K);
	}

	/**
	 * 初始化，将词随机赋予桌子与主题
	 * @param corpus  格式与Blei 的 cLDA一致
	 */
	public void addInstances(int[][] documentsInput, int V) {
		this.V = V;
		totalWordsNum = 0;
		docStates = new DOCState[documentsInput.length];
		for (int d = 0; d < documentsInput.length; d++) {
			docStates[d] = new DOCState(documentsInput[d], d);
			totalWordsNum += documentsInput[d].length;
		}

		int k, i, j;
		DOCState docState;
		p = new double[20];
		f = new double[20];
//		tablesNumByTopic = new int[K+1];
//		wordNumByTopic = new  int[K+1];
		phi = trn_phi;
		tablesNumByTopic = trn_wordNumByTopic.clone();
		wordNumByTopic = trn_wordNumByTopic.clone();
//		phi = new int[K+1][];
//		for (k = 0; k <= K; k++) 	// var initialization done
//			phi[k] = new int[V];
		for (k = 0; k < K; k++) {
			docState = docStates[k];
			for (i = 0; i < docState.docLen; i++)
				addWord(docState.docID, i, 0, k);
		} // all topics have now one document
		for (j = K; j < docStates.length; j++) {
			docState = docStates[j]; 
			k = random.nextInt(K);
			for (i = 0; i < docState.docLen; i++) 
				addWord(docState.docID, i, 0, k);
		} // all words have now one topic
	}
	
	/**
	 * 一步一步向前执行Gibbs Sampling
	 */
	public void nextGibbsSweep() {
		int table;
		for (int d = 0, len = docStates.length; d < len; d++) {
			for (int i = 0; i < docStates[d].docLen; i++) {
				removeWord(d, i); // remove the word i from the state
				table = sampleTable(d, i);
				if (table == docStates[d].tablesNum) // new Table
					addWord(d, i, table, sampleTopic()); // sampling its Topic
				else
					addWord(d, i, table, docStates[d].tableToTopic[table]); // existing Table
			}
		}
		defragment();
	}

	
	/**
	 * 
	 * 桌子的主题抽样，决定将桌子赋予哪个主题
	 * 语料层的抽样
	 * @return the index of the topic
	 */
	private int sampleTopic() {
		double r, pSum = 0.0;
		int k;
		p = ensureCapacity(p, K);
		for (k = 0; k < K; k++) {
			pSum += tablesNumByTopic[k] * f[k];
			p[k] = pSum;
		}
		pSum += gamma.getValue() / V;
		p[K] = pSum;
		r = random.nextDouble() * pSum;
		for (k = 0; k <= K; k++)
			if (r < p[k])
				break;
		return k;
	}
	

	/**	 
	 * 词抽样，决定将词赋予哪个桌子（主题）
	 * @param docID the index of the document of the current word
	 * @param i the index of the current word
	 * @return the index of the table
	 */
	int sampleTable(int docID, int i) {
		int k, j;
		double pSum = 0.0, vb = V * eta, fNew, u;
		DOCState docState = docStates[docID];
		f = ensureCapacity(f, K);
		p = ensureCapacity(p, docState.tablesNum);
		fNew = gamma.getValue() / V;
		for (k = 0; k < K; k++) {
			f[k] = (phi[k][docState.words[i].termIndex] + eta) / 
					(wordNumByTopic[k] + vb);
			fNew += tablesNumByTopic[k] * f[k];
		}
		for (j = 0; j < docState.tablesNum; j++) {
			if (docState.wordCountByTable[j] > 0) 
				pSum += docState.wordCountByTable[j] * f[docState.tableToTopic[j]];
			p[j] = pSum;
		}
		pSum += alpha.getValue() * fNew / (totalTablesNum + gamma.getValue()); // Probability for t = tNew
		p[docState.tablesNum] = pSum;
		u = random.nextDouble() * pSum;
		for (j = 0; j <= docState.tablesNum; j++)
			if (u < p[j])
				break;	// decided which table the word i is assigned to
		return j;
	}

	/**
	 * Removes a word from the bookkeeping
	 * 
	 * @param docID 文档ID
	 * @param i 词在文档中的序号（不是词的全局索引号）
	 */
	public void removeWord(int docID, int i){
		DOCState docState = docStates[docID];
		int table = docState.words[i].tableAssignment;
		int k = docState.tableToTopic[table];
		docState.wordCountByTable[table]--; 
		wordNumByTopic[k]--; 		
		phi[k][docState.words[i].termIndex] --;
		if (docState.wordCountByTable[table] == 0) { // table is removed
			totalTablesNum--; 
			tablesNumByTopic[k]--; 
			docState.tableToTopic[table] --; 
		}
	}
	
	
	
	/**
	 * Add a word to the bookkeeping
	 * 增加phi, wordCountByTable等所拥有的词数
	 * @param docID	docID the id of the document the word belongs to 
	 * @param i the index of the word
	 * @param table the table to which the word is assigned to
	 * @param k the topic to which the word is assigned to
	 */
	public void addWord(int docID, int i, int table, int k) {
		DOCState docState = docStates[docID];
		docState.words[i].tableAssignment = table; 
		docState.wordCountByTable[table]++; 
		wordNumByTopic[k]++; 
		phi[k][docState.words[i].termIndex] ++;
		if (docState.wordCountByTable[table] == 1) { // a new table is created
			docState.tablesNum++;
			docState.tableToTopic[table] = k;
			totalTablesNum++;
			tablesNumByTopic[k]++;
			docState.tableToTopic = ensureCapacity(docState.tableToTopic, docState.tablesNum);
			docState.wordCountByTable = ensureCapacity(docState.wordCountByTable, docState.tablesNum);
			if (k == K) { // a new topic is created
				K++; 
				tablesNumByTopic = ensureCapacity(tablesNumByTopic, K); 
				wordNumByTopic = ensureCapacity(wordNumByTopic, K);
				phi = add(phi, new int[V], K);
			}
		}
	}

	
	/**
	 * 将没有一个词的 主题移走
	 */
	public void defragment() {
		int[] kOldToKNew = new int[K];
		int k, newK = 0;
		for (k = 0; k < K; k++) {
			if (wordNumByTopic[k] > 0) {
				kOldToKNew[k] = newK;
				swap(wordNumByTopic, newK, k);
				swap(tablesNumByTopic, newK, k);
				swap(phi, newK, k);
				newK++;
			} 
		}
		K = newK;
		for (int j = 0; j < docStates.length; j++) 
			docStates[j].defragment(kOldToKNew);
	}
	
	
	/**
	 * Permute the ordering of documents and words in the bookkeeping
	 */
	public void doShuffle(){
		List<DOCState> h = Arrays.asList(docStates);
		Collections.shuffle(h);
		docStates = h.toArray(new DOCState[h.size()]);
		for (int j = 0; j < docStates.length; j ++){
			List<WordState> h2 = Arrays.asList(docStates[j].words);
			Collections.shuffle(h2);
			docStates[j].words = h2.toArray(new WordState[h2.size()]);
		}
	}
	
	
	
	public static void swap(int[] arr, int arg1, int arg2){
		   int t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static void swap(int[][] arr, int arg1, int arg2) {
		   int[] t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	//将数组放大，确保不越界
	public static double[] ensureCapacity(double[] arr, int min){
		int length = arr.length;
		if (min < length)
			return arr;
		double[] arr2 = new double[min*2];
		System.arraycopy(arr, 0, arr2, 0, length);
//		for (int i = 0; i < length; i++) 
//			arr2[i] = arr[i];
		return arr2;
	}

	//将数组放大，确保不越界
	public static int[] ensureCapacity(int[] arr, int min) {
		int length = arr.length;
		if (min < length)
			return arr;
		int[] arr2 = new int[min*2];
		System.arraycopy(arr, 0, arr2, 0, length);
//		for (int i = 0; i < length; i++) 
//			arr2[i] = arr[i];
		return arr2;
	}

	public static int[][] add(int[][] arr, int[] newElement, int index) {
		int length = arr.length;
		if (length <= index){
			int[][] arr2 = new int[index*2][];
			for (int i = 0; i < length; i++) 
				arr2[i] = arr[i];
			arr = arr2;
		}
		arr[index] = newElement;
		return arr;
	}
}