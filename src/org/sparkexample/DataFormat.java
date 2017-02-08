package org.sparkexample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.sun.mail.iap.ParsingException;

// cell_id,encounter_id,spawn_id,pokemon_type_id,latitude,longitude,despawn_time_ms,scan_time_ms
//encounter_id, spawnpoint_id, pokemon_id, latitude, longitude, disappear_time
public class DataFormat {
	static Path target = Paths.get("kempt data/weatherPokes");

	public static void main(String[] args) throws IOException {
		addWeatherDataTo("p12345.csv", Arrays.asList(6, 5));
	}

	private static void addWeatherDataTo(String file, List<Integer> ids) throws IOException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<Pokemon> weatherRDD = context.textFile(t.toString()).map(f -> new Pokemon(f));
		JavaRDD<Pokemon> pokes = context.textFile(Pokemon.folder + file).map(f -> new Pokemon(f))
				.filter(p -> !ids.contains(p.pokemon_id));//
		pokes = pokes.subtract(weatherRDD);

		List<Pokemon> p = pokes.collect();
		pokes = null;
		weatherRDD = null;
		if (!Files.exists(t))
			Files.createFile(t);
		try {
			for (Pokemon poke : p) {
				String weather = Weathergrab.getHistoricalWeather(poke.lat, poke.lng, poke.disappear_time);
				Files.write(t, (poke.toString() + "," + weather).getBytes(), StandardOpenOption.APPEND);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		context.close();

	}

	static Path t = Paths.get("kempt data/uncommon.csv");

	private static void filterCommonAndDuplicateFrom(String... strings) throws IOException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<String> pFile = context.textFile(Pokemon.folder + strings[0]);
		for (int i = 1; i < strings.length; i++)
			pFile = pFile.union(context.textFile(Pokemon.folder + strings[i]));
		List<String> pokes = pFile.map(f -> new Pokemon(f)).filter(p -> !Pokemon.tooCommon.contains(p.pokemon_id))
				.map(p -> p.toString() + "\n").distinct().collect();//
		Files.deleteIfExists(t);
		Files.createFile(t);

		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String poke : pokes) {
			sb.append(poke);
			i++;
			if (i == 500) {

				Files.write(t, sb.toString().getBytes(), StandardOpenOption.APPEND);
				sb = new StringBuilder();
				i = 0;
			}
		}
		context.close();
	}

	private static void convertFromFormat2() throws IOException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		Files.deleteIfExists(target);
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<String> unformatted = context.textFile("pokemon 2.csv");
		JavaRDD<String> formatted = unformatted.map(p -> p.split(",")).map(p -> String.join(",", p[1], p[2], p[3], p[4],
				p[5], Pokemon.sdf.format(new Date(Long.parseLong(p[6]) * 10000L))) + "\n");
		unformatted = null;
		Files.createFile(target);
		formatted.foreach(s -> Files.write(target, s.getBytes(), StandardOpenOption.APPEND));
		context.close();
	}
	/*
	 * private static List<Tuple3<Integer, Integer, Integer>>
	 * getPokemonCoords(JavaSparkContext context) {
	 * 
	 * JavaRDD<String> pFile = context.textFile(Pokemon.folder + "p"); pFile =
	 * pFile.union(context.textFile(Pokemon.folder + "p2")); pFile =
	 * pFile.union(context.textFile(Pokemon.folder + "p3")); pFile =
	 * pFile.union(context.textFile(Pokemon.folder + "p4")); pFile =
	 * pFile.union(context.textFile(Pokemon.folder + "p5"));
	 * 
	 * JavaRDD<Pokemon> pokes = pFile.map(f -> new Pokemon(f)).filter(p ->
	 * !tooCommon.contains(p.pokemon_id)); return pokes.map(f -> new
	 * Tuple3<Integer, Integer, Integer>((int) Math.floor((f.lat * scale) +
	 * width / 2), (int) Math.floor(f.lng * scale) + height / 2,
	 * f.pokemon_id)).collect(); }
	 */
}
