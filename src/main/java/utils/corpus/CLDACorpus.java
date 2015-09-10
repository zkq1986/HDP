package utils.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class CLDACorpus {

	private int[][] trainDocs;//存放 文档-词 索引
	private int[][] testDocs;
	int[][] testTrnDocs;//测试部分的训练部分
	int[][] testCalcDocs;//测试部分的计算部分
	private int vocabularySize = 0;

	/**
	 * 
	 * @param is 输入流
	 * @param trainCount 训练与测试的比例，如9-1
	 * @param testCount 训练与测试的比例，如9-1
	 * @throws IOException
	 */
	public CLDACorpus(InputStream is, int trainCount, int testCount) throws IOException {
		int length, word, counts;
		List<List<Integer>> traindocList = new ArrayList<List<Integer>>();
		List<List<Integer>> testdocList = new ArrayList<List<Integer>>();
		List<Integer> traindoc;
		List<Integer> testdoc;
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String line = null;
					
		while ((line = br.readLine()) != null && !line.trim().equals("")) {
			try {
				traindoc = new ArrayList<Integer>();
				testdoc = new ArrayList<Integer>();
				String[] fields = line.split(" ");
				length = Integer.parseInt(fields[0]);
				int trnCount = 0, tstCount = 0, trn_i = 0, tst_j = 0;
				for (int n = 0; n < length; n++) {
					String[] wordCounts = fields[n + 1].split(":");
					word = Integer.parseInt(wordCounts[0]);
					counts = Integer.parseInt(wordCounts[1]);
					if (word >= vocabularySize)
						vocabularySize = word + 1;
					for (int i = 0; i < counts; i++)
						traindoc.add(word);
				}
				if(length > 0){
					traindocList.add(traindoc);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage() + "\n");
			}
		}
		trainDocs = new int[traindocList.size()][];
		List<Integer> doc;
		for (int j = 0; j < traindocList.size(); j++) {
			doc = traindocList.get(j);
			trainDocs[j] = new int[doc.size()];
			 for (int i = 0; i < doc.size(); i++) {
				 trainDocs[j][i] = doc.get(i);
			 }
		}
	}
	
	/**
	 * percent 随机获取的比例
	 */
	public List<int[][]> heldoutCorpus(int[][] trainDocs, int[][]testDcos, 
			float percent, int count){
		int docLen = trainDocs.length;
		int[][] originDocs = new int[docLen][];
		for(int i = 0; i < docLen; i++){
			int len = trainDocs[i].length + testDocs[i].length;
			originDocs[i] = new int[len];
			for(int j = 0; j < trainDocs[i].length; j++){
				originDocs[i][j] = trainDocs[i][j];
			}
			for(int j = 0; j < testDocs[i].length; j++){
				originDocs[i][j] = testDocs[i][j];
			}
		}
		
		List<int[][]> testDocsls = new ArrayList<int[][]>(count);
		Random rand = new Random();
		for(int i = 0; i < count; i++){//测试的held out个数
			int[][] testDoc = new int[docLen][];
			for(int j = 0; j < docLen; j++){//文档个数
				for(int k = 0, len = originDocs[j].length; k < len; k++){//文档内的元素
					int N = (int) (percent * len);//随机获取的个数
					testDoc[j] = new int[N];
					HashSet<Integer> hs = new HashSet();
					for(int m = 0; m < N; m++){
						int pos = -1;
						while(true){
							pos = rand.nextInt(len);//随机获取N个不重复的位置
							if(!hs.contains(pos)){
								hs.add(pos);
								break;
							}
						}
						testDoc[j][m] = originDocs[j][pos];
					}
				}
			}
			testDocsls.add(testDoc);
		}
		return testDocsls;
	}
	
	
	public int[][] getDocuments() {
		return trainDocs;
	}
	
	/**
	 * 获取训练文档， 测试文档中训练部分，测试文档中的计算部分
	 * @param bHeldout 是否有Heldout
	 * @param percent 采用多少比例Heldout，0.9表示90%train，10%test
	 */
	public void getDocuments2(boolean bHeldout, float percent) {
		int docLen = trainDocs.length;
		int[][] originDocs = trainDocs;
		
		int count = 1;
		Random rand = new Random();		
		int N = (int) (percent * docLen);//随机获取的个数
		int[][] testTrnDocs = new int[N][];//测试部分的训练部分
		int[][] testCalcDocs = new int[N][];//测试部分的计算部分
		trainDocs = new int[docLen - N][];
		HashSet<Integer> hs = new HashSet();
		for(int j = 0; j < N; j++){//文档个数
			int pos = -1;
			while(true){
				pos = rand.nextInt(docLen);//随机获取N个不重复的位置
				if(!hs.contains(pos)){
					hs.add(pos);
					break;
				}
			}
			int len = originDocs[pos].length;
			testTrnDocs[j] = new int[len/2];
			testCalcDocs[j] = new int[len - len/2];
			int cnt = 0;
			int idx1 = 0, idx2 = 0;
			int len2 = len/2;
			HashSet<Integer> hsInner = new HashSet();
			for(int m = 0; m < len2; m++){
				int pos2 = -1;
				while(true){
					pos2 = rand.nextInt(len);//随机获取N个不重复的位置
					if(!hsInner.contains(pos2)){
						hsInner.add(pos2);
						break;
					}
				}
				testTrnDocs[j][m] = originDocs[pos][pos2];//测试部分的训练部分
			}
			int idx = 0;
			for(int k = 0; k < len; k++){//文档内的元素
				if(!hsInner.contains(k)){
					testCalcDocs[j][idx++] = originDocs[pos][k];//测试部分的计算部分
				}
			}
		}	
		int idx = 0;
		for(int j = 0; j < docLen; j++){//文档个数			
			if(!hs.contains(j)){
				trainDocs[idx++] = originDocs[j];
			}
		}
		this.testTrnDocs = testTrnDocs;
		this.testCalcDocs = testCalcDocs;
		this.trainDocs = trainDocs;
	}
	
	/**
	 * 获取训练文档， 测试文档中训练部分，测试文档中的计算部分
	 * @param bHeldout 是否有Heldout
	 * @param percent 采用多少比例Heldout，0.9表示90%train，10%test
	 */
	public void getDocuments(boolean bHeldout, float percent) {
		int docLen = trainDocs.length;
		int[][] originDocs = trainDocs;
		
		Random rand = new Random();
//		int[][] testDoc = new int[docLen][];
		int[][] testTrnDocs = new int[docLen][];//测试部分的训练部分
		int[][] testCalcDocs = new int[docLen][];//测试部分的计算部分
		trainDocs = new int[docLen][];
		HashSet<Integer> hs = new HashSet();
		for(int j = 0; j < docLen; j++){//文档个数
			hs.clear();
			int N = (int) (percent * originDocs[j].length);//随机获取的个数
			int len = originDocs[j].length;
			testTrnDocs[j] = new int[N/2];
			testCalcDocs[j] = new int[N - N/2];
			int cnt = 0;
			int idx1 = 0, idx2 = 0;
			for(int m = 0; m < N; m++){
				int pos = -1;
				while(true){
					pos = rand.nextInt(len);//随机获取N个不重复的位置
					if(!hs.contains(pos)){
						hs.add(pos);
						break;
					}
				}
				if(cnt < N/2){//测试部分的训练部分
					testTrnDocs[j][idx1++] = originDocs[j][pos];
					cnt++;
				}
				else{//测试部分的计算部分
					testCalcDocs[j][idx2++] = originDocs[j][pos];
				}
			}
				
			//获取训练文档
			int idx = 0;
			trainDocs[j] = new int[len-N];
			for(int k = 0; k < len; k++){//文档内的元素
				if(!hs.contains(k)){
					trainDocs[j][idx++] = originDocs[j][k];
				}
			}
		}
		this.testTrnDocs = testTrnDocs;
		this.testCalcDocs = testCalcDocs;
	}
	
	
	public int[][] getTestTrnDocuments() {
		return testTrnDocs;
	}

	public int[][] getTestCalcDocuments() {
		return testCalcDocs;
	}
	
	public int[][] getTestDocuments() {
		return testDocs;
	}
	public int getVocabularySize() {
		return vocabularySize;
	}
}

