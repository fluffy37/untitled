package org.example;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class SimpleBot implements LongPollingSingleThreadUpdateConsumer {

private final TelegramClient telegramClient;
private final WeatherService weatherService;

public SimpleBot(String botToken){
    this.telegramClient = new OkHttpTelegramClient(botToken);
    this.weatherService = new WeatherService();
}

@Override
    public void  consume(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        String answer;

       if (text.startsWith("/start")){
           answer = "Привет! я бот погоды.\n" +
                   "Напиши название города. \n" +
                   "Например: Москва";
       } else {
           String city = text.trim();
           answer = weatherService.getWeather(city);
       }
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
