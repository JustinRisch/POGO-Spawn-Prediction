package org.sparkexample;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.sf.corn.converter.ParsingException;
import net.sf.corn.converter.json.JsTypeComplex;
import net.sf.corn.converter.json.JsTypeList;
import net.sf.corn.converter.json.JsTypeSimple;
import net.sf.corn.converter.json.JsonStringParser;
import net.sf.corn.httpclient.HttpResponse;
import java.util.Date;
import net.sf.corn.httpclient.HttpClient;
import net.sf.corn.httpclient.HttpClient.HTTP_METHOD;

public class Weathergrab {
	private static String wukey = "0b0bbc6b532e7d06";
	static Double lat = 37.5968974, lng = -77.3552527;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static void main(String[] args) throws ParsingException, URISyntaxException, IOException,
			net.sf.corn.converter.ParsingException, ParseException {
		Date date = sdf.parse("19911105");
		System.out.println(getHistoricalWeather(lat, lng, date));
	}

	static String wUrl = "http://api.wunderground.com/api/";

	// 0b0bbc6b532e7d06/history_YYYYMMDD/q/CA/San_Francisco.json
	public static String getHistoricalWeather(Double lat, Double lng, Date date)
			throws ParsingException, URISyntaxException, IOException, net.sf.corn.converter.ParsingException {
		String weatherUrl = wUrl + wukey + "/history_" + sdf.format(date) + "/q/" + lat + "," + lng + ".json";
		System.out.println("call: " + weatherUrl);
		return fetchWeather(weatherUrl);
	}

	private static String fetchWeather(String url)
			throws URISyntaxException, IOException, ParsingException, net.sf.corn.converter.ParsingException {
		HttpClient client = new HttpClient(new URI(url));
		HttpResponse response = client.sendData(HTTP_METHOD.GET);
		try {
			if (!response.hasError()) {
				String jsonString = response.getData();
				JsTypeComplex jsonResponse = (JsTypeComplex) JsonStringParser.parseJsonString(jsonString);
				JsTypeComplex history = (JsTypeComplex) jsonResponse.get("history");
				JsTypeList sum = (JsTypeList) history.get("dailysummary");
				JsTypeComplex date = ((JsTypeComplex) sum.get(0));
				JsTypeSimple precipi = (JsTypeSimple) date.get("precipi");
				JsTypeSimple maxTempi = (JsTypeSimple) date.get("maxtempi");
				return precipi + "," + maxTempi;
			}
		} catch (Exception e) {
			System.out.println("No data found.");
		}
		return "-1,-99";
	}

}