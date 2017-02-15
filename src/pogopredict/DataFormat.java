package pogopredict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.scalatest.selenium.WebBrowser;

import com.google.common.util.concurrent.AtomicDouble;

import scala.Tuple3;
import weather.Weather;
import weather.Weathergrab;

// cell_id,encounter_id,spawn_id,pokemon_type_id,latitude,longitude,despawn_time_ms,scan_time_ms
//encounter_id, spawnpoint_id, pokemon_id, latitude, longitude, disappear_time
@SuppressWarnings("unused")
public class DataFormat {
	static Path target = null;

	public static void main(String[] args) throws IOException, ParseException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		HashMap<SpaceTime, Weather> weatherCache = new HashMap<>();
		target = Paths.get("kempt data/WeatherCacheComplex2");
		for (String s : Files.readAllLines(Paths.get("kempt data/WeatherCache"))) {
			Weather space = new Weather(s);
			weatherCache.put(space.getSpaceTime(), space);
		}
		for (String s : Files.readAllLines(Paths.get("kempt data/WeatherCacheComplex"))) {
			if (s.trim().isEmpty())
				continue;
			Weather space = new Weather( new Pokemon(s));
			weatherCache.put(space.getSpaceTime(), space);
		}
		JavaPairRDD<SpaceTime, Iterable<Pokemon>> pRDD = context.textFile("kempt data/fixLATLONG")
				.map(p -> new Pokemon(p)).groupBy(p -> new Weather(p).getSpaceTime());
		
		Files.deleteIfExists(target);
		Files.createFile(target);
		StringBuilder sb = new StringBuilder();
		AtomicInteger total = new AtomicInteger(0), matches = new AtomicInteger(0);
		System.out.println("Cache Size: " + weatherCache.keySet().size());
		
		pRDD.foreach(wp -> {
			SpaceTime w = wp._1;
			total.getAndIncrement();
			Optional<SpaceTime> key = weatherCache.keySet().stream().filter(e -> e.equals(wp._1)).findFirst();
			Weather realWeather;
			if (key.isPresent()) {
				realWeather = weatherCache.get(key.get());
				matches.getAndIncrement();
			} else { // if there's no key found, just write the original info.
				realWeather = Weathergrab.getHistoricalWeather(w.lat, w.lng, w.day);
				weatherCache.put(realWeather.getSpaceTime(), realWeather);
			}
			wp._2.forEach(p -> sb.append(
					String.join(",", p.toStringNoWeather(), realWeather.getRainfall() + "", realWeather.getTemp() + "")
							+ "\n"));
			if (sb.length() >= 100000) {
				Files.write(target, sb.toString().getBytes(), StandardOpenOption.APPEND);
				sb.setLength(0);
				System.out.println(matches.get() + "/" + total.get() + " Calls: " + (total.get() - matches.get()));
			}
		});
		System.out.println("Total: " + total.get());
		System.out.println("Matches: " + matches.get());
		context.close();
	}

	private static void fixNullNulls(String string) throws IOException {
		target = Paths.get("kempt data/f" + string);
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<String> f = context.textFile("kempt data/" + string);
		Files.deleteIfExists(target);
		Files.createFile(target);
		f.map(e -> e.replace("null,null,", ""))
				.foreach(e -> Files.write(target, (e + "\n").getBytes(), StandardOpenOption.APPEND));
		context.close();
	}

	private static void checkLatLong(JavaRDD<Pokemon> p) throws InterruptedException {
		System.out.println("Total: " + p.count());
		JavaRDD<Pokemon> temp = p.filter(f -> f.lat > 90 || f.lat < -90 || f.lng > 180 || f.lng < -180);
		System.out.println("Errored: " + temp.count());
		Thread.sleep(3000);
	}

	private static JavaRDD<Pokemon> fixLatLong(JavaRDD<Pokemon> p) throws IOException {
		AtomicDouble temp = new AtomicDouble();
		JavaRDD<Pokemon> returnable = p.map(f -> {
			if (f.lat > 90 || f.lat < -90 || f.lng > 180 || f.lng < -180) {
				temp.set(f.lat);
				f.lat = f.lng;
				f.lng = temp.get();
			}
			return f;
		});
		Files.deleteIfExists(target);
		Files.createFile(target);
		returnable.foreach(poke -> Files.write(target, (poke.toString() + "\n").getBytes(), StandardOpenOption.APPEND));
		return returnable;
	}

	private static JavaRDD<Pokemon> getPokemon(JavaSparkContext context, String file, List<Integer> only) {
		return context.textFile(Pokemon.folder + file).map(f -> new Pokemon(f))
				.filter(f -> only.contains(f.pokemon_id));
	}

	private static void addWeatherDataTo(String file, List<Integer> only) throws IOException {
		String weatheredFile = Pokemon.folder + "WEATHERED "
				+ only.toString().replace("[", "").replace("]", "").replaceAll(",", "");
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaRDD<Pokemon> p = getPokemon(context, file, only);
		if (!Files.exists(Paths.get(weatheredFile)))
			Files.createFile(Paths.get(weatheredFile));
		else {
			List<String> w = context.textFile(weatheredFile)
					.filter(f -> f != null && !f.trim().replaceAll("\n", "").isEmpty()).collect();
			List<Pokemon> pL = p.collect();
			p = context.parallelize(pL.subList(w.size() - 1, pL.size() - 1));

		}
		try {
			p.foreach(poke -> {
				Weather weather = Weathergrab.getHistoricalWeather(poke.lat, poke.lng, poke.day);
				String pokeString = poke.toString();
				if (pokeString.endsWith(",null,null"))
					pokeString = pokeString.substring(pokeString.length() - 1 - ",null,null".length());
				String out = (pokeString + "," + weather.getRainfall() + "," + weather.getTemp()).replaceAll("\"", "")
						+ "\n";
				System.out.print(out);
				Files.write(Paths.get(weatheredFile), out.getBytes(), StandardOpenOption.APPEND);
				Thread.sleep(6000);
			});
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
				.map(p -> p.toString() + "\n").distinct().collect();

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

}
