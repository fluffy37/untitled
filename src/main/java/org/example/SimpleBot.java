package org.example;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.util.Set;


import java.util.ArrayList;
import java.util.List;

public class SimpleBot implements LongPollingSingleThreadUpdateConsumer {

private final TelegramClient telegramClient;
private final WeatherService weatherService;
private final Map<String, Map<String, String>> pendingCityPick = new HashMap<>();

public SimpleBot(String botToken){
    this.telegramClient = new OkHttpTelegramClient(botToken);
    this.weatherService = new WeatherService();
}
    private ReplyKeyboardMarkup defaultKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Миасс"));
        row1.add(new KeyboardButton("Москва"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Париж"));
        row2.add(new KeyboardButton("Лондон"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Екатеринбург"));
        row3.add(new KeyboardButton("Лос-Анджелес"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .build();
    }
    private void sendWeather(String chatId, WeatherService.WeatherResult res) {
        String photoPath = pikPhotoPath(res.code);

        if (photoPath != null) {
            try (InputStream is = getClass().getResourceAsStream(photoPath)) {
                if (is != null) {
                    SendPhoto photo = SendPhoto.builder()
                            .chatId(chatId)
                            .photo(new InputFile(is, "5325893484839374308_119.jpg"))
                            .caption(res.text)
                            .build();
                    SendMessage back = SendMessage.builder()
                            .chatId(chatId)
                            .text("Можешь выбрать город из списка или написать свой 👇")
                            .replyMarkup(defaultKeyboard())
                            .build();

                    try {
                        telegramClient.execute(back);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    telegramClient.execute(photo);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String startPhotoPath = "/weather/GettyImages-1657231123-1-2.jpg";

        try (InputStream is = getClass().getResourceAsStream(startPhotoPath)) {
            if (is != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(res.text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


private String pikPhotoPath(int code){
    if (code == 1000){// ясно
        return "/weather/5325893484839374078_120.jpg";
}
    Set<Integer> snowCodes = Set.of(1210, 1213, 1216, 1219, 1222, 1225);
    if (snowCodes.contains(code)) {
        return "/weather/5325893484839374104_121.jpg";
    }
    Set<Integer> mistFogCodes = Set.of(1030, 1135, 1147);
    if (mistFogCodes.contains(code)) {
        return "/weather/5325893484839374231_121.jpg";
    }

    if (code == 1009) { // пасмурно (overcast)
        return "/weather/5325893484839374081_121.jpg";
    }
    Set<Integer> rainCodes = Set.of(1180, 1183, 1186, 1189, 1192, 1195);
    if (rainCodes.contains(code)) {
        return "/weather/5325893484839374087_121.jpg";
    }

    return null;
}
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            Map<String, String> pickMAp = pendingCityPick.get(chatId);
            if (pickMAp != null && pickMAp.containsKey(text)) {
                String latLon = pickMAp.get(text);
                pendingCityPick.remove(chatId);
                WeatherService.WeatherResult res = weatherService.getWeatherCode(latLon);
                sendWeather(chatId, res);
                return;
            }


            if (text.startsWith("/start")) {
                String answer = """
Рады вас видеть в нашем прогнозе погоды имени Макса Эмилиана Ферстаппена!!
Здесь вы можете узнать погоду в любом нужном для вас городе🌇, вам всего лишь необходимо написать нужный вам город и вам сразу же выдаст необходимую информацию 😇

Если есть вопросы обращайтесь к нашему администратору @Kvtski.
Приятного пользования!😘
""";


                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Миасс"));
                row1.add(new KeyboardButton("Москва"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Париж"));
                row2.add(new KeyboardButton("Лондон"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Екатеринбург"));
                row3.add(new KeyboardButton("Лос-Анджелес"));

                List<KeyboardRow> keyboard = new ArrayList<>();
                keyboard.add(row1);
                keyboard.add(row2);
                keyboard.add(row3);

                ReplyKeyboardMarkup replyKeyboard = defaultKeyboard();
                String startPhotoPath = "/weather/GettyImages-1657231123-1-2.jpg";

                System.out.println("START PHOTO URL = " + SimpleBot.class.getResource(startPhotoPath));

                try (InputStream is = SimpleBot.class.getResourceAsStream(startPhotoPath)) {
                    if (is != null) {
                        SendPhoto photo = SendPhoto.builder()
                                .chatId(chatId)
                                .photo(new InputFile(is, "GettyImages-1657231123-1-2.jpg"))
                                .build();
                        telegramClient.execute(photo);
                    } else {
                        System.out.println("START PHOTO STREAM = null (не нашёл файл в resources)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(answer)
                        .replyMarkup(replyKeyboard)
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

            String query = text.trim();

            List<WeatherService.LocationOption> opts;
            try {
                opts = weatherService.searchLocations(query);
                if (!query.contains(",") && query.matches(".*[А-Яа-яЁё].*")) {
                    List<WeatherService.LocationOption> ru =
                            opts.stream().filter(o -> "Russia".equalsIgnoreCase(o.country)).toList();

                    if (!ru.isEmpty()) {
                        opts = ru;
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (opts.isEmpty()) {
                SendMessage msg = SendMessage.builder()
                        .chatId(chatId)
                        .text("Не нашёл город 😔 Попробуй уточнить (например: Москва, RU).")
                        .build();
                try {
                    telegramClient.execute(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

// 1 вариант — сразу погода
            if (opts.size() == 1) {
                String latLon = opts.get(0).latLon(); // ВАЖНО: метод должен называться latLon()
                WeatherService.WeatherResult res = weatherService.getWeatherCode(latLon);
                sendWeather(chatId, res);
                return;
            }

// Несколько вариантов — показываем кнопки (макс 5)
            int limit = Math.min(5, opts.size());
            Map<String, String> map = new HashMap<>();
            List<KeyboardRow> keyboard = new ArrayList<>();

            for (int i = 0; i < limit; i++) {
                WeatherService.LocationOption o = opts.get(i);

                String regionPart = (o.region == null || o.region.isBlank()) ? "" : (", " + o.region);
                String buttonText = (i + 1) + ") " + o.name + regionPart + ", " + o.country;

                map.put(buttonText, o.latLon()); // ВАЖНО: latLon()

                KeyboardRow row = new KeyboardRow();
                row.add(new KeyboardButton(buttonText));
                keyboard.add(row);
            }

            pendingCityPick.put(chatId, map);

            ReplyKeyboardMarkup replyKeyboard = ReplyKeyboardMarkup.builder()
                    .keyboard(keyboard)
                    .resizeKeyboard(true)
                    .build();

            SendMessage msg = SendMessage.builder()
                    .chatId(chatId)
                    .text("Нашёл несколько городов с таким названием. Выбери нужный:")
                    .replyMarkup(replyKeyboard)
                    .build();

            try {
                telegramClient.execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }}
