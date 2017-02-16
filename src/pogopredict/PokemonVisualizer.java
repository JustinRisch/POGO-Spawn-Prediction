package pogopredict;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.google.common.util.concurrent.AtomicDouble;
import scala.Tuple2;
import scala.Tuple3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

@SuppressWarnings("unused")
public class PokemonVisualizer {

	static final AtomicDouble maxlat = new AtomicDouble(Double.MIN_VALUE), minlat = new AtomicDouble(Double.MAX_VALUE),
			maxlng = new AtomicDouble(Double.MIN_VALUE), minlng = new AtomicDouble(Double.MAX_VALUE);;
	static final int scale = 10, maxLng = 360 * scale, maxLat = 180 * scale;

	// 114, 334
	static File f = new File("PokesVisualized.png");
	static BufferedImage bi = new BufferedImage(maxLat, maxLng, BufferedImage.TYPE_INT_RGB);
	static double avgTemp = 0d;
	static int counttotal = 0;

	public static void main(String[] args) throws IOException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);

		// draws the axis
		for (int i = 0; i < maxLat; i++)
			bi.setRGB(i, maxLng / 2, Color.WHITE.getRGB());
		for (int j = 0; j < maxLng; j++)
			bi.setRGB(maxLat / 2, j, Color.WHITE.getRGB());

		JavaPairRDD<Tuple2<Integer, Integer>, Iterable<Pokemon>> coords = getPokemonByLocation(context);
		coords.foreach(f -> {
			try {
				Double lowest = 999.0, highest = -999.0, avg = 0d, count = 0d;

				for (Pokemon p : f._2) {
					if (p.getTemp() < lowest)
						lowest = p.getTemp();
					if (p.getTemp() > highest)
						lowest = p.getTemp();
					avg = avg + p.getTemp();
					avgTemp += p.getTemp();
					count++;
					counttotal++;
				}

				avg = avg / count;
				int color = Color.white.getRGB();
				if (avg > 80)
					color = Color.red.getRGB();
				else if (avg > 70)
					color = Color.yellow.getRGB();
				else if (avg > 60)
					color = Color.green.getRGB();
				else
					color = Color.blue.getRGB();
				bi.setRGB(f._1._1, f._1._2, color);

			} catch (ArrayIndexOutOfBoundsException AiOOB) {
				System.out.println("ERRORED: " + f._1 + "," + f._2);
			}
		});
		System.out.println("AVGTEMP: " + avgTemp);
		System.out.println("counttotal: " + counttotal);
		System.out.println("AVG: " + (avgTemp / counttotal));
		ImageIO.write(bi, "PNG", f);
		context.close();
	}

	private static void makeMap(JavaSparkContext context) throws IOException {
		final JavaRDD<String> pFile = context.textFile("non-common");
		JavaRDD<Pokemon> pokes = pFile.map(f -> new Pokemon(f));
		Files.deleteIfExists(Paths.get("TEST MAP.html"));
		Files.createFile(Paths.get("TEST MAP.html"));
		Files.write(Paths.get("TEST MAP.html"), "Double[] pokes = [".getBytes(), StandardOpenOption.APPEND);
		pokes.map(p -> new Tuple2<Double, Double>(p.lat, p.lng)).distinct().foreach(p -> {
			StringBuilder sb = new StringBuilder("[").append(p._1).append(",").append(p._2).append("],\n");
			Files.write(Paths.get("TEST MAP.html"), sb.toString().getBytes(), StandardOpenOption.APPEND);
		});
		Files.write(Paths.get("TEST MAP.html"), "];".getBytes(), StandardOpenOption.APPEND);
	}

	static double total = 0;
	static List<Integer> fire = Arrays.asList(4, 5, 6, 37, 38, 58, 59, 77, 78, 126, 136, 105),
			water = Arrays.asList(7, 8, 9, 54, 55, 60, 61, 86, 90, 98, 99, 116, 117, 118, 119, 120, 129, 134, 62, 72,
					73, 79, 80, 87, 91, 121, 130, 131);

	private static JavaPairRDD<Tuple2<Integer, Integer>, Iterable<Pokemon>> getPokemonByLocation(
			JavaSparkContext context) {
		JavaRDD<String> pFile = context.textFile(Pokemon.folder + "pw");
		total = pFile.count();
		JavaPairRDD<Tuple2<Integer, Integer>, Iterable<Pokemon>> pokes = pFile.filter(f -> !f.trim().isEmpty())
				.map(f -> new Pokemon(f)).filter(f -> fire.contains(f.pokemon_id))
				.groupBy(f -> new Tuple2<Integer, Integer>((int) Math.floor((f.lat * scale) + maxLat / 2),
						(int) Math.floor(f.lng * scale) + maxLng / 2));
		return pokes;
	}

	private static List<Tuple3<Integer, Integer, Integer>> getPokemonCoords(JavaSparkContext context, int i) {
		JavaRDD<String> pFile = context.textFile(Pokemon.folder + "pw");
		total = pFile.count();
		JavaRDD<Pokemon> pokes = pFile.map(f -> new Pokemon(f)).filter(
				p -> Pokemon.tooCommon.subList(0, Math.min(i, Pokemon.tooCommon.size())).contains(p.pokemon_id));
		return pokes.map(f -> new Tuple3<Integer, Integer, Integer>((int) Math.floor((f.lat * scale) + maxLat / 2),
				(int) Math.floor(f.lng * scale) + maxLng / 2, f.pokemon_id)).collect();
	}
}
