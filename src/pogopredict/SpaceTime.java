package pogopredict;

import java.math.BigDecimal;
import java.util.Date;

import weather.Weathergrab;

public abstract class SpaceTime {
	Double lat, lng;
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

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	@Override
	public String toString() {
		return String.join(",", lat + "", lng + "", Weathergrab.sdf.format(day));
	}

	public Double roundLocation(String d) {
		return new BigDecimal(d).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public Double roundLocation(Double d) {
		return new BigDecimal(d).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public boolean equals(SpaceTime obj) {
		return roundLocation(lat).equals(roundLocation(obj.getLat()))
				&& roundLocation(lng).equals(roundLocation(obj.getLng()))
				&& Weathergrab.sdf.format(this.getDay()).equals(Weathergrab.sdf.format(obj.getDay()));
	}
}
