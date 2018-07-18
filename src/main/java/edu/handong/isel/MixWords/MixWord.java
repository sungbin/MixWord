package edu.handong.isel.MixWords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
		Seq<KoreanToken> oldTokens = null;
		List<KoreanTokenJava> newTokensList = null;

		ArrayList<String> keywords = new ArrayList<String>();
		Scanner in = new Scanner(System.in);
		String line;
		for (File data : datas) {
			System.out.println(data.getAbsolutePath() + "을 분석중..");

			try {
				String extractedLine = this.extractLineFromFile(data);
				oldTokens = this.tokenization(extractedLine);
				List<KoreanTokenJava> oldTokensList = OpenKoreanTextProcessorJava
						.tokensToJavaKoreanTokenList(oldTokens);

				// this.printPoses(oldTokens);

				while (true) {
					System.out.println(data.getName() + "에 들어갈 키워드들을 입력해주세요.(exit: q), (붙혀넣기: shift+insert)");
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
				//sb = new StringBuffer(sb.toString().trim());
				
				CharSequence normalized = OpenKoreanTextProcessorJava.normalize(sb.toString());
				System.out.println(normalized);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private List<KoreanTokenJava> mixWord(List<KoreanTokenJava> oldTokensList, List<KoreanTokenJava> newTokensList) {

		ArrayList<KoreanTokenJava> newNounList = new ArrayList<KoreanTokenJava>();
		ArrayList<KoreanTokenJava> oldNounList = new ArrayList<KoreanTokenJava>();
		ArrayList<Integer> randomN = new ArrayList<Integer>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		int i;
		i = 0;
		for (KoreanTokenJava word : newTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
				//System.out.println("text: " + word.getText()+ ", i:" + i);
				newNounList.add(word);
			}
		}
		for (KoreanTokenJava word : oldTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
				//System.out.println("oldNoun: " + word.getText());
				oldNounList.add(word);
				index.add(i);
				i++;
			}
		}

		Random rand = new Random();
		for (i = 0; i < index.size(); i++) {
			while (true) {
				int n = rand.nextInt(index.size());
				System.out.println("n: "+ n);
				if (!randomN.contains(index.get(n))) {
					randomN.add(index.get(n));
					break;
				}
			}
		}
		i = 0;
		for (KoreanTokenJava keyword : newTokensList) {
			//System.out.println("i: " + i + ", randomN.get(i): " + randomN.get(i));
			int ranN = randomN.get(i);
			//System.out.println("old: " + oldTokensList.get(ranN).getText() + ", new: " + keyword.getText());
			oldTokensList.set(ranN, keyword);
			i++;
		}

		return oldTokensList;
	}

	private void printPoses(Seq<KoreanToken> tokens) {
		List<KoreanTokenJava> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);
		for (KoreanTokenJava word : words) {
			if (word.getPos() == KoreanPosJava.Noun)
				System.out.println(word.getText());
		}

	}

	private String extractLineFromFile(File data) throws IOException {
		String extractedLine = "";

		BufferedReader reader = new BufferedReader(new FileReader(data));
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

	private void executeExample() {
		String text = "한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ #한국어";

		// Normalize
		CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
		System.out.println(normalized);
		// 한국어를 처리하는 예시입니다ㅋㅋ #한국어

		// Tokenize
		Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
		System.out.println(OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens));
		// [한국어, 를, 처리, 하는, 예시, 입니, 다, ㅋㅋ, #한국어]
		System.out.println(OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens));
		// [한국어(Noun: 0, 3), 를(Josa: 3, 1), 처리(Noun: 5, 2), 하는(Verb(하다): 7, 2), 예시(Noun:
		// 10, 2),
		// 입니다(Adjective(이다): 12, 3), ㅋㅋㅋ(KoreanParticle: 15, 3), #한국어(Hashtag: 19, 4)]

		// Phrase extraction
		List<KoreanPhraseExtractor.KoreanPhrase> phrases = OpenKoreanTextProcessorJava.extractPhrases(tokens, true,
				true);
		System.out.println(phrases);
		// [한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2),
		// #한국어(Hashtag: 18, 4)]

	}

	private ArrayList<String> getLine(File data) {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			String line;
			////////////////////////////////////////////////////////////////
			BufferedReader in = new BufferedReader(new FileReader(data));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			in.close();
			////////////////////////////////////////////////////////////////
		} catch (IOException e) {
			System.err.println(e); //
			System.exit(1);
		}
		return lines;
	}
}
