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

import java.io.InputStream;
import java.util.Set;


import java.util.ArrayList;
import java.util.List;

public class SimpleBot implements LongPollingSingleThreadUpdateConsumer {

private final TelegramClient telegramClient;
private final WeatherService weatherService;

public SimpleBot(String botToken){
    this.telegramClient = new OkHttpTelegramClient(botToken);
    this.weatherService = new WeatherService();
}

private String pikPhotoPath(int code){
    if (code == 1000){// ясно
        return "/weather/5325893484839374078_120.jpg";
}
    if (code == 1009) { // пасмурно (overcast)
        return "/weather/5325893484839374081_121.jpg";
    }
    Set<Integer> rainCodes = Set.of(1180, 1183, 1186, 1189, 1192, 1195);
    if (rainCodes.contains(code)) {
        return "/weather/5325893484839374087_121.jpg";
    }

    return null; // если не распознали — просто текст
}
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            if (text.startsWith("/start")) {
                String answer = "Привет! я бот погоды.\n" +
                        "Напиши название города. \n" +
                        "Например: Москва\n" +
                        "Можешь выбрать из предложенных.";

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

                ReplyKeyboardMarkup replyKeyboard = ReplyKeyboardMarkup.builder()
                        .keyboard(keyboard)
                        .resizeKeyboard(true)
                        .build();

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

            String city = text.trim();
            WeatherService.WeatherResult res = weatherService.getWeatherCode(city);

            String photoPath = pikPhotoPath(res.code);
            if (photoPath != null) {
                try (InputStream is = getClass().getResourceAsStream(photoPath)) {
                    if (is != null) {
                        SendPhoto photo = SendPhoto.builder()
                                .chatId(chatId)
                                .photo(new InputFile(is, "weather.jpg"))
                                .caption(res.text)
                                .build();

                        telegramClient.execute(photo);

                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
    }}
