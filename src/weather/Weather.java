package weather;

import java.text.ParseException;
import java.util.Date;

import pogopredict.Pokemon;
import pogopredict.SpaceTime;

import java.math.BigDecimal;

public class Weather extends SpaceTime {

	Double lat = 200d, lng = 200d, rainfall = -1d, temp = -200d;
	Date day;

	public static void main(String[] args) throws ParseException {
		String test = "0,0,1,52.5481150517243,13.3864913238756,2016-07-24 22:54:00,0.00,84";
		Pokemon testPoke = new Pokemon(test);
		Weather w = new Weather(testPoke);
		System.out.println(w.toString());
		System.out.println(new Weather(test).toString());
	}

	public Double roundLocation(String d) {
		return new BigDecimal(d).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public Double roundLocation(Double d) {
		return new BigDecimal(d).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public Weather(String csv) throws ParseException {
		String[] props = csv.split(",");
		lat = roundLocation(props[3]);
		lng = roundLocation(props[4]);
		// it's stored in the pokemon format which is more specific, not the
		// weather one.
		day = Pokemon.sdf.parse(props[5]);
		if (props.length > 6)
			rainfall = Pokemon.getDoubleOrNull(props[6]);
		if (props.length > 7)
			temp = Pokemon.getDoubleOrNull(props[7]);
	}

	public Weather(Pokemon p) throws ParseException {
		lat = roundLocation(p.getLat());
		lng = roundLocation(p.getLng());
		rainfall = p.getRainfall();
		temp = p.getTemp();
		day = Weathergrab.sdf.parse(Weathergrab.sdf.format(p.getDay()));
	}

	@Override
	public String toString() {
		return String.join(",", lat + "", lng + "", Weathergrab.sdf.format(day), rainfall + "", temp + "");
	}
}
