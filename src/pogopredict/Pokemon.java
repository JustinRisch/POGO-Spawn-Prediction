package pogopredict;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Pokemon extends SpaceTime implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String folder = "kempt data/";
	// encounter_id, spawnpoint_id, pokemon_id, latitude, longitude,
	// day
	String encounter_id = "", spawnpoint_id = "";
	Integer pokemon_id = -1;
	Double lat, lng, rainfall, temp;
	// String originalCSV = "";
	Date day;

	public String getEncounter_id() {
		return encounter_id;
	}

	public void setEncounter_id(String encounter_id) {
		this.encounter_id = encounter_id;
	}

	public String getSpawnpoint_id() {
		return spawnpoint_id;
	}

	public void setSpawnpoint_id(String spawnpoint_id) {
		this.spawnpoint_id = spawnpoint_id;
	}

	public Integer getPokemon_id() {
		return pokemon_id;
	}

	public void setPokemon_id(Integer pokemon_id) {
		this.pokemon_id = pokemon_id;
	}

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

	// 2016-09-08 13:32:44
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Pokemon(String csv) throws ParseException {
		String[] props = csv.split(",");
		encounter_id = props[0];
		spawnpoint_id = props[1];
		pokemon_id = Integer.parseInt(props[2]);
		lat = Double.parseDouble(props[3]);
		lng = Double.parseDouble(props[4]);
		day = sdf.parse(props[5]);
		// if weather data present
		if (props.length > 6)
			rainfall = getDoubleOrNull(props[6]);
		if (props.length > 7)
			temp = getDoubleOrNull(props[7]);
	}

	public static Double getDoubleOrNull(String x) {
		if (x == null)
			return 0d;
		x = x.trim();
		if (x.isEmpty() || x.equalsIgnoreCase("NULL"))
			return 0d;

		return Double.valueOf(x);

	}

	@Override
	public String toString() {
		// encounter_id, spawnpoint_id, pokemon_id, latitude, longitude,
		// day
		return String.join(",", encounter_id, spawnpoint_id, pokemon_id.toString(), lat.toString(), lng.toString(),
				sdf.format(day), rainfall + "", temp + "");
	}

	public boolean equals(Pokemon obj) {
		return encounter_id.equals(obj.encounter_id) && spawnpoint_id.equals(obj.spawnpoint_id)
				&& pokemon_id.equals(obj.pokemon_id) && lat.equals(obj.lat) && lng.equals(obj.lng);
	}


	public boolean isNear(SpaceTime b, double radius) {
		double dlat = Math.abs(lat - b.lat), dlong = Math.abs(lng - b.lng);
		if (dlat > radius || dlong > radius)
			return false;
		return (dlat * dlat) + (dlong * dlong) <= radius * radius;
	}

	static final HashMap<Integer, String> pokedex = new HashMap<>();
	static final HashMap<String, Integer> nameLookup = new HashMap<>();
	static {
		try {
			for (String line : Files.readAllLines(Paths.get("Pokedex"))) {
				String[] p = line.split(",");
				pokedex.put(Integer.parseInt(p[0]), p[1]);
				nameLookup.put(p[1], Integer.parseInt(p[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static List<Integer> tooCommon = new ArrayList<>();
	static {
		try {
			tooCommon = Files.readAllLines(Paths.get("raritylist")).stream().map(e -> e.split(",")[0])
					.map(e -> Pokemon.getIndex(e)).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getName() {
		return pokedex.getOrDefault(this.pokemon_id, "Not found!!!");
	}

	public static Integer getIndex(String name) {
		return nameLookup.getOrDefault(name, -1);
	}

}
