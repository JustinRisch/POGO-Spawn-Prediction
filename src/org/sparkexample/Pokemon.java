package org.sparkexample;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Pokemon implements Serializable {
	private static final long serialVersionUID = 1L;
	// encounter_id, spawnpoint_id, pokemon_id, latitude, longitude,
	// disappear_time
	String encounter_id = "", spawnpoint_id = "";
	Integer pokemon_id = -1;
	Double lat, lng;
	String originalCSV = "";
	Date disappear_time;
//2016-09-08 13:32:44 
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Pokemon(String csv) throws ParseException {
		String[] props = csv.split(",");
		encounter_id = props[0];
		spawnpoint_id = props[1];
		pokemon_id = Integer.parseInt(props[2]);
		lat = Double.parseDouble(props[3]);
		lng = Double.parseDouble(props[4]);
		originalCSV = csv;
		disappear_time = sdf.parse(props[5]); 
	}

	public boolean isNear(Pokemon b) {
		return isNear(b, .01);
	}

	public boolean isNear(Pokemon b, double radius) {
		double dlat = Math.abs(lat - b.lat), dlong = Math.abs(lng - b.lng);
		if (dlat > radius || dlong > radius)
			return false;
		return (dlat * dlat) + (dlong * dlong) <= radius * radius;
	}

	public String toString() {
		return originalCSV;
	}

}
