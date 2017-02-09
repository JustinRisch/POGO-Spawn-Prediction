package pogopredict;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.google.common.util.concurrent.AtomicDouble;
import scala.Tuple2;
import scala.Tuple3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class PokemonVisualizer {

	static final AtomicDouble maxlat = new AtomicDouble(Double.MIN_VALUE), minlat = new AtomicDouble(Double.MAX_VALUE),
			maxlng = new AtomicDouble(Double.MIN_VALUE), minlng = new AtomicDouble(Double.MAX_VALUE);;
	static final int scale = 20, width = 360 * scale, height = 360 * scale;

	// 114, 334
	public static void main(String[] args) throws IOException {
		SparkConf conf = new SparkConf().setAppName("org.sparkexample.WordCount").setMaster("local");
		JavaSparkContext context = new JavaSparkContext(conf);
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < width; i++)
			bi.setRGB(i, height / 2, Color.WHITE.getRGB());
		for (int j = 0; j < height; j++)
			bi.setRGB(width / 2, j, Color.WHITE.getRGB());
		for (int i = 0; i < 160; i += 10) {
			List<Tuple3<Integer, Integer, Integer>> coords = getPokemonCoords(context, i);
			int maxlng = Integer.MIN_VALUE, maxlat = Integer.MIN_VALUE, minlng = Integer.MAX_VALUE,
					minlat = Integer.MAX_VALUE;

			for (Tuple3<Integer, Integer, Integer> e : coords) {
				try {
					if (maxlng < e._2() - height / 2)
						maxlng = e._2() - height / 2;
					else if (minlng > e._2() - height / 2)
						minlng = e._2() - height / 2;

					if (maxlat < e._1() - width / 2)
						maxlat = e._1() - width / 2;
					else if (minlat > e._1() - width / 2)
						minlat = e._1() - width / 2;

					int color = bi.getRGB(e._1(), e._2());
					if (color == Color.black.getRGB()) {
						color = Color.blue.getRGB();
					} else if (color == Color.blue.getRGB()) {
						color = Color.green.getRGB();
					} else if (color == Color.green.getRGB()) {
						color = Color.yellow.getRGB();
					} else if (color == Color.yellow.getRGB()) {
						color = Color.red.getRGB();
					} else if (color == Color.red.getRGB()) {

					}
					bi.setRGB(e._1(), e._2(), color);
				} catch (ArrayIndexOutOfBoundsException AiOOB) {
					System.out.println("ERRORED: " + e._1() + "," + e._2());
				}
			}
			
			ImageIO.write(bi, "PNG", new File("PokesVisualized - " + i + " ("
					+ (int) (100d * ((double) coords.size()) / total) + "%).png"));
			System.out.println(coords.size() + "/" + total + " pokemon mapped.");
			System.out.println("Max: " + maxlat / scale + ", " + maxlng / scale);
			System.out.println("Min: " + minlat / scale + ", " + minlng / scale);
			System.out.println("Center: " + (maxlat + minlat) / (2 * scale) + ", " + (maxlng + minlng) / (2 * scale));
		}
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

	private static List<Tuple3<Integer, Integer, Integer>> getPokemonCoords(JavaSparkContext context, int i) {
		JavaRDD<String> pFile = context.textFile(Pokemon.folder + "p12345.csv");
		total = pFile.count();
		JavaRDD<Pokemon> pokes = pFile.map(f -> new Pokemon(f))
				.filter(p -> Pokemon.tooCommon
						.subList(0, Math.min(i, Pokemon.tooCommon.size()) )
						.contains(p.pokemon_id));
		return pokes.map(f -> new Tuple3<Integer, Integer, Integer>((int) Math.floor((f.lat * scale) + width / 2),
				(int) Math.floor(f.lng * scale) + height / 2, f.pokemon_id)).collect();
	}
}
