package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    public static class WeatherResult {
        public final String text;
        public final int code;
        public final int isDay;

        public WeatherResult(String text, int code, int isDay) {
            this.text = text;
            this.code = code;
            this.isDay = isDay;
        }
    }

    private static final String API_KEY_ENV_NAME = "WEATHER_API_KEY";
    private static final String URL_TEMPLATE =
            "https://api.weatherapi.com/v1/current.json?key=%s&q=%s&lang=ru";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        String rawKey = System.getenv(API_KEY_ENV_NAME);
        if (rawKey != null) {
            this.apiKey = rawKey.trim();
        } else {
            this.apiKey = null;
        }

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("Ошибка: переменная окружения " + API_KEY_ENV_NAME + " не задана!");
        }
    }

    public WeatherResult getWeatherCode(String city) {
        if (apiKey == null || apiKey.isEmpty()) {
            return new WeatherResult("Сервер не настроен: нет ключа погодного API 😢", -1, -1);
        }

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(URL_TEMPLATE, apiKey, encodedCity);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return new WeatherResult("Не удалось получить погоду для \"" + city + "\" 😔", -1, -1);
            }

            String body = response.body();
            JsonNode root = objectMapper.readTree(body);

            JsonNode location = root.path("location");
            JsonNode current = root.path("current");

            String name = location.path("name").asText();
            String country = location.path("country").asText();

            double temp = current.path("temp_c").asDouble();
            double feelsLike = current.path("feelslike_c").asDouble();
            String condition = current.path("condition").path("text").asText();

            int code = current.path("condition").path("code").asInt();
            int isDay = current.path("is_day").asInt();

            String text = String.format(
                    "Погода в %s (%s):\n%.1f°C (ощущается как %.1f°C), %s",
                    name, country, temp, feelsLike, condition
            );

            return new WeatherResult(text, code, isDay);

        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherResult("Произошла ошибка при запросе погоды 😢", -1, -1);
        }
    }

    public String getWeather(String city) {
        return getWeatherCode(city).text;
    }
    public static class LocationOption{
        public final String name, region, country;
        public final  double lat, lon;
        public LocationOption(String name, String region, String country, double lat, double lon) {
            this.name = name;
            this.region = region;
            this.country = country;
            this.lat = lat;
            this.lon = lon;
        }
        public String latLon(){
            return lat + "," + lon;
        }
    }
    public List<LocationOption> searchLocations(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("https://api.weatherapi.com/v1/search.json?key=%s&q=%s", apiKey, encoded);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return List.of();

        JsonNode arr = objectMapper.readTree(resp.body()); // массив локаций
        List<LocationOption> out = new ArrayList<>();
        for (JsonNode loc : arr) {
            out.add(new LocationOption(
                    loc.path("name").asText(),
                    loc.path("region").asText(),
                    loc.path("country").asText(),
                    loc.path("lat").asDouble(),
                    loc.path("lon").asDouble()
            ));
        }
        return out;
    }

}