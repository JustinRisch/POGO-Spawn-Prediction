package weather;

import java.text.ParseException;
import java.util.Date;

import pogopredict.Pokemon;
import pogopredict.SpaceTime;

public class Weather extends SpaceTime {
	private static final long serialVersionUID = 3815096556658833143L;
	Double lat = 200d, lng = 200d, rainfall = -1d, temp = -200d;
	Date day;

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getRainfall() {
		return rainfall;
	}

	public void setRainfall(Double rainfall) {
		this.rainfall = rainfall;
	}

	public Double getTemp() {
		return temp;
	}

	public void setTemp(Double temp) {
		this.temp = temp;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public Weather(String csv) throws ParseException {
		String[] props = csv.split(",");
		lat = roundLocation(props[0]);
		lng = roundLocation(props[1]);
		// it's stored in the pokemon format which is more specific, not the
		// weather one.
		day = Weathergrab.sdf.parse(props[2]);
		rainfall = Pokemon.getDoubleOrNull(props[3]);
		temp = Pokemon.getDoubleOrNull(props[4]);

	}

	public SpaceTime getSpaceTime() {
		SpaceTime s = new SpaceTime() {
		};
		s.lat = this.lat;
		s.lng = this.lng;
		s.day = this.day;
		return s;
	}

	public Weather(Pokemon p) throws ParseException {
		lat = roundLocation(p.getLat());
		lng = roundLocation(p.getLng());
		rainfall = p.getRainfall();
		temp = p.getTemp();
		day = Weathergrab.sdf.parse(Weathergrab.sdf.format(p.getDay()));
	}

	public Weather(Double lat, Double lng, Date day) {
		this.lat = roundLocation(lat);
		this.lng = roundLocation(lng);
		this.day = day;
		this.rainfall = null;
		this.temp = null;
	}
	public Weather(Double lat, Double lng, Date day, Double rain, Double temp) {
		this.lat = roundLocation(lat);
		this.lng = roundLocation(lng);
		this.day = day;
		this.rainfall = rain;
		this.temp = temp;
	}

	@Override
	public String toString() {
		return String.join(",", lat + "", lng + "", Weathergrab.sdf.format(day), rainfall + "", temp + "");
	}
}
