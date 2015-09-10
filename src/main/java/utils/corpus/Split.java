package utils.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import utils.IOUtils;

public class Split {
	public static void main(String[] args) {
	  	try {
//	  		String srcFile = "D:/dev/hdp/sgrld/data/newsVocab.txt";
	  		String srcFile = "D:/360 cloud disk/学习/专业/topic detection realtime/" +
	  				"dataset/NIPS/nips_1-17.mat";
	  		readData(srcFile);

	  	} catch (IOException e) {
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
//	  	System.out.println("读取结束");
	  }
	
	public static void readData(String file) throws IOException{
		BufferedReader br = IOUtils.getBufferedReader("data/temp/news.txt");
		String line = null;
		int count = 0;
		int count2 = 0;
		BufferedWriter bw = IOUtils.getBufferedWriter("data/temp/news_" + count2++ +".txt");
		while((line = br.readLine()) != null){
//			System.out.println(line);
			count++;
			if(count % 1200 == 0){
				bw.close();
				bw = IOUtils.getBufferedWriter("data/temp/news_" + count2++ +".txt");
			}
			bw.write(line + "\r\n");
		}
		bw.close();
		System.out.println("总词数：" + count);
		br.close();
	}
}
