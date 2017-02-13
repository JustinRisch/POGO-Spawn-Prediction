package pogopredict;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import weather.Weathergrab;

public abstract class SpaceTime implements Serializable {

	private static final long serialVersionUID = 1L;
	public Double lat;
	public Double lng;
	public Date day;

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
		if (day != null)
			return String.join(",", lat + "", lng + "", Weathergrab.sdf.format(day));
		else
			return String.join(",", lat + "", lng + "", "");
	}

	public boolean isNear(SpaceTime b, double radius) {
		double dlat = Math.abs(lat - b.lat), dlong = Math.abs(lng - b.lng);
		if (dlat > radius || dlong > radius)
			return false;
		return (dlat * dlat) + (dlong * dlong) <= radius * radius;
	}

	public Double roundLocation(String d) {
		return new BigDecimal(d).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public Double roundLocation(Double d) {
		return new BigDecimal(d).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public boolean equals(SpaceTime obj) {
		boolean isNear = this.isNear(obj, .01);
		boolean sameDay = Weathergrab.sdf.format(this.getDay()).equals(Weathergrab.sdf.format(obj.getDay()));
		return isNear && sameDay;
	}
}
