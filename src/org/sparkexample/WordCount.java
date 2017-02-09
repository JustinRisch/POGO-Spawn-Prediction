package org.sparkexample;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class WordCount {
	private static final FlatMapFunction<String, String> WORDS_EXTRACTOR = (s) -> Arrays.asList(s.split("\\),\\("));
	static String folder = "pokes/";

	public static void main(String[] args) throws IOException {

		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		makePFile(context);
		File largestSample = Files.list(Paths.get(folder)).map(f -> f.toFile())
				.sorted((f, f2) -> (int) (f2.length() - f.length())).findFirst().get();
		JavaRDD<String> pFile = context.textFile(largestSample.getAbsolutePath());
		JavaRDD<Pokemon> pokes = pFile.map(e -> new Pokemon(e));
		System.out.println(largestSample.getName()+":"+pokes.count());
		// Files.deleteIfExists(Paths.get(folder));
		// Files.createDirectories(Paths.get(folder));
		//pokes.foreach(ps -> System.out.println(ps));
		// pokes.foreach(e->System.out.println(e.pokemon_id));
		context.close();

	}

	private static void makePFile(JavaSparkContext context) throws IOException {
		JavaRDD<String> file = context.textFile("pdb");
		JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
		Files.deleteIfExists(Paths.get("p3"));
		Files.write(Paths.get("p3"), "".getBytes());
		words.map(e -> e.replaceAll("\\(", "").replaceAll("=", "").replaceAll("\\)", "").replaceAll("'", "") + "\n")
				.foreach(e -> Files.write(Paths.get("p3"), e.getBytes(), StandardOpenOption.APPEND));
	}

}
