package pogopredict;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class testConnect {
	private static String ip = "spark://192.168.1.22", port = "6066";

	public static void main(String[] args) throws InterruptedException {
		SparkConf conf = new SparkConf().setAppName("please").setMaster(ip + ":" + port);
		JavaSparkContext context = new JavaSparkContext(conf);

		JavaRDD<String> readme = context.textFile("/spark/README.md");
		readme.foreach(System.out::println);
		context.close();
	}
}
