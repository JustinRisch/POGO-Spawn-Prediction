package org.sparkexample;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Pokemon implements Serializable {
	private static final long serialVersionUID = 1L;
	public static String folder = "kempt data/";
	// encounter_id, spawnpoint_id, pokemon_id, latitude, longitude,
	// disappear_time
	String encounter_id = "", spawnpoint_id = "";
	Integer pokemon_id = -1;
	Double lat, lng;
	// String originalCSV = "";
	Date disappear_time;
	// 2016-09-08 13:32:44
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static List<Integer> tooCommon = Arrays.asList(129, 48, 96, 133, 21, 13, 16, 69, 10, 19, 41);

	public Pokemon(String csv) throws ParseException {
		
		String[] props = csv.split(",");
		encounter_id = props[0];
		spawnpoint_id = props[1];
		pokemon_id = Integer.parseInt(props[2]);
		lat = Double.parseDouble(props[3]);
		lng = Double.parseDouble(props[4]);
		disappear_time = sdf.parse(props[5]);
	}

	@Override
	public String toString() {
		// encounter_id, spawnpoint_id, pokemon_id, latitude, longitude,
		// disappear_time
		return String.join(",", encounter_id, spawnpoint_id, pokemon_id.toString(), lat.toString(), lng.toString(),
				sdf.format(disappear_time));
	}

	public boolean isNear(Pokemon b) {
		return isNear(b, .01);
	}

	public boolean equals(Pokemon obj) {
		return this.toString().equals(obj.toString());
	}

	public boolean isNear(Pokemon b, double radius) {
		double dlat = Math.abs(lat - b.lat), dlong = Math.abs(lng - b.lng);
		if (dlat > radius || dlong > radius)
			return false;
		return (dlat * dlat) + (dlong * dlong) <= radius * radius;
	}

	static final HashMap<Integer, String> pokedex = new HashMap<>();
	static {
		try {
			for (String line : Files.readAllLines(Paths.get("Pokedex"))) {
				String[] p = line.split(",");
				pokedex.put(Integer.parseInt(p[0]), p[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return pokedex.getOrDefault(this.pokemon_id, "Not found!!!");
	}

}
