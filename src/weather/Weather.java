package weather;

import java.text.ParseException;
import java.util.Date;

import pogopredict.Pokemon;
import java.math.BigDecimal;

public class Weather {

	Double lat = 200d, lng = 200d, rainfall = -1d, temp = -200d;
	Date day;

	public static void main(String[] args) throws ParseException {
		String test = "0,0,1,52.5481150517243,13.3864913238756,2016-07-24 22:54:00,0.00,84";
		Pokemon testPoke = new Pokemon(test);
		Weather w = new Weather(testPoke);
		System.out.println(w.toString());
	}

	public Double roundLocation(Double d) {
		return new BigDecimal(d).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}


	public Weather(Pokemon p) throws ParseException {
		lat = roundLocation(p.getLat());
		lng = roundLocation(p.getLng());
		rainfall = p.getRainfall();
		temp = p.getTemp();
		day = Weathergrab.sdf.parse(Weathergrab.sdf.format(p.getDisappear_time()));
	}

	@Override
	public String toString() {
		return String.join(",", lat + "", lng + "", Weathergrab.sdf.format(day), rainfall + "", temp + "");
	}
}
