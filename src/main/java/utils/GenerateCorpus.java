package utils;

import hdp.HDPGibbsSampler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import utils.corpus.CLDACorpus;

public class GenerateCorpus {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String trainFile = "data/nips_train.txt";
		String testTrnFile = "data/nips_testTrn.txt";//测试部分的训练集
		String testCalcFile = "data/nips_testCalc.txt";//测试部分计算集
		HDPGibbsSampler hdp = new HDPGibbsSampler();
		
		String inFile =  "data/nips/nips.txt";
		String I2WPath = "data/nips/vocab.nips.txt";
		CLDACorpus corpus = new CLDACorpus(new FileInputStream(inFile), 
				9, 1);
		hdp.V = Readdata.getVocabSize(I2WPath);
		corpus.getDocuments(true, 0.1f);
		hdp.addInstances(corpus.getDocuments(), hdp.V);
		int[][] train = corpus.getDocuments();
		int[][] testTrnDocs = corpus.getTestTrnDocuments();
		int[][] testCalcDocs = corpus.getTestCalcDocuments();
		
		outData(trainFile, train);
		outData(testTrnFile, testTrnDocs);
		outData(testCalcFile, testCalcDocs);
		
	}

	public static void outData(String outfile, int[][] docs) throws IOException{
		BufferedWriter bw = IOUtils.getBufferedWriter(outfile);
		String line = null;
		int count = 0;
		int len = docs.length;
		for(int i = 0; i < len; i++){
			if(docs[i].length > 0){
				bw.write((i) + "\t");
				bw.write(" " + (docs[i][0]));
				for(int j = 1, len2= docs[i].length; j < len2; j++){
					bw.write(" " + (docs[i][j]));
				}
				bw.write("\r\n");
			}
		}
		System.out.println("总词数：" + count);
		bw.close();
	}
}
