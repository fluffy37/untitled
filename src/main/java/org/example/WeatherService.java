package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WeatherService {

    private static final String API_KEY_ENV_NAME = "WEATHER_API_KEY";
    private static final String URL_TEMPLATE =
            "https://api.weatherapi.com/v1/current.json?key=%s&q=%s&lang=ru";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = System.getenv(API_KEY_ENV_NAME);
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            System.err.println("Ошибка: переменная окружения " + API_KEY_ENV_NAME + " не задана!");
        }
    }

    public String getWeather(String city) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Сервер не настроен: нет ключа погодного API 😢";
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
                return "Не удалось получить погоду для \"" + city + "\" 😔";
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

            return String.format(
                    "Погода в %s (%s):\n%.1f°C (ощущается как %.1f°C), %s",
                    name, country, temp, feelsLike, condition
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "Произошла ошибка при запросе погоды 😢";
        }
    }
}
