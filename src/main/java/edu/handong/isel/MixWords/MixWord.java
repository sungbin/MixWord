package edu.handong.isel.MixWords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.openkoreantext.processor.KoreanPosJava;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.openkoreantext.processor.tokenizer.Sentence;
import org.openkoreantext.processor.util.KoreanPos;
import scala.collection.Iterator;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MixWord {

	public static void main(String[] args) {
		MixWord mw = new MixWord();
		mw.run(args);

	}

	private void run(String[] args) {

		// this.executeExample();

		System.out.print("start Program from... ");
		System.out.println(System.getProperty("user.dir"));

		File dirFile = new File("Data");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File[] fileList = dirFile.listFiles();
		ArrayList<File> datas = new ArrayList<File>();

		for (File tempFile : fileList) {
			if (tempFile.isFile()) {
				if (tempFile.getName().endsWith(".txt")) {
					datas.add(tempFile);
				}
			}
		}
		
		for (File data : datas) {
			Seq<KoreanToken> oldTokens = null;
			List<KoreanTokenJava> newTokensList = null;

			ArrayList<String> keywords = new ArrayList<String>();
			Scanner in = new Scanner(System.in);
			String line;
			System.out.println("parsing " + data.getAbsolutePath() + "...");

			try {
				String extractedLine = this.extractLineFromFile(data);
				oldTokens = this.tokenization(extractedLine);
				List<KoreanTokenJava> oldTokensList = OpenKoreanTextProcessorJava
						.tokensToJavaKoreanTokenList(oldTokens);

				// this.printPoses(oldTokens);

				while (true) {
					System.out.println(data.getName() + ": push keywords.(end: q), (paste key: shift+insert)");
					line = in.nextLine();
					if (line.equals("q")) {
						break;
					}

					List<KoreanTokenJava> newList = OpenKoreanTextProcessorJava
							.tokensToJavaKoreanTokenList(this.tokenization(line));
					if (newTokensList == null)
						newTokensList = newList;
					else
						newTokensList.addAll(newList);
				}
				
				List<KoreanTokenJava> editedTokenList = this.mixWord(oldTokensList, newTokensList);

				int i = 0;
				StringBuffer sb = new StringBuffer();
				for (KoreanTokenJava temp : editedTokenList) {
					sb.append(temp.getText().trim());
				}
				// sb = new StringBuffer(sb.toString().trim());

//				CharSequence completedSentence = OpenKoreanTextProcessorJava.normalize(sb.toString());
				
				// System.out.println(completedSentence);
				this.makeOut(sb.toString(), data);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {
			System.out.println("All files saved in result");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File makeOut(String line, File file) throws IOException {
		File curDir = new File("result");

		if (!curDir.exists()) {
			curDir.mkdir();
		}

		File newFile = new File(curDir.getAbsolutePath() + File.separator + file.getName());
		if (newFile.exists()) {
			if (newFile.delete()) {
				System.out.println("successful to delete " + newFile.getName());
			} else {
				System.out.println("fail to delecte " + newFile.getName());
			}
		} else {
			System.out.println("start making " + newFile.getName() + "..");
		}

		FileOutputStream fileOutputStream = new FileOutputStream(newFile);
		OutputStreamWriter OutputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		BufferedWriter bf = new BufferedWriter(OutputStreamWriter);

		bf.write(line);
		bf.flush();

		bf.close();
		System.out.println(newFile.getName() + "was made");

		return newFile;

	}

	private List<KoreanTokenJava> mixWord(List<KoreanTokenJava> oldTokensList, List<KoreanTokenJava> newTokensList) {

		ArrayList<KoreanTokenJava> newNounList = new ArrayList<KoreanTokenJava>();
		ArrayList<KoreanTokenJava> oldNounList = new ArrayList<KoreanTokenJava>();
		ArrayList<Integer> randomN = new ArrayList<Integer>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		HashMap<Integer, Integer> datas = new HashMap<Integer, Integer>();
		int i;
		i = 0;
		for (KoreanTokenJava word : newTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
//				 System.out.println("text: " + word.getText()+ ", i:" + i);
				newNounList.add(word);
			}
		}
		int j = 0;
		for (KoreanTokenJava word : oldTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
//				 System.out.println("oldNoun: " + word.getText());
				datas.put(i, j);
				oldNounList.add(word);
				index.add(i);
				i++;
			}
			j++;
		}

		Random rand = new Random();
		for (i = 0; i < index.size(); i++) {
			while (true) {
				int n = rand.nextInt(index.size());
//				 System.out.println("n: "+ n);
				if (!randomN.contains(index.get(n))) {
					randomN.add(index.get(n));
					break;
				}
			}
		}
		i = 0;
		for (KoreanTokenJava keyword : newTokensList) {
//			 System.out.println("i: " + i + ", randomN.get(i): " + randomN.get(i));
			int ranN = randomN.get(i);
//			 System.out.println("old: " + oldTokensList.get(ranN).getText() + ", new: " +
//			 System.out.println("old: " + oldNounList.get(ranN).getText() + ", new: " +
//			 keyword.getText());
//			 System.out.println("저장될: "+oldTokensList.get(datas.get(ranN)).getText());
			 oldTokensList.set(datas.get(ranN), keyword);
//			oldTokensList.set(ranN, keyword);
			i++;
		}

		return oldTokensList;
	}

	private String extractLineFromFile(File data) throws IOException {
		String extractedLine = "";
		
		FileInputStream fileInputStream = new FileInputStream(data);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		BufferedReader reader = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ((line = reader.readLine()) != null) {
			extractedLine += (line + " ");
		}

		return extractedLine;
	}

	private Seq<KoreanTokenizer.KoreanToken> tokenization(String line) {
		CharSequence normalized = OpenKoreanTextProcessorJava.normalize(line);
		return OpenKoreanTextProcessorJava.tokenize(normalized);
		// Seq<KoreanTokenizer.KoreanToken> tokens =
		// OpenKoreanTextProcessorJava.tokenize(normalized);
		// return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens);

	}
}
