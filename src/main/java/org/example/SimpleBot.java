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

import java.util.ArrayList;
import java.util.List;

public class SimpleBot implements LongPollingSingleThreadUpdateConsumer {

private final TelegramClient telegramClient;
private final WeatherService weatherService;

public SimpleBot(String botToken){
    this.telegramClient = new OkHttpTelegramClient(botToken);
    this.weatherService = new WeatherService();
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
            String answer = weatherService.getWeather(city);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(answer)
                    .build();

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
