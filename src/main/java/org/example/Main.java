package org.example;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        // Берём токен из переменной окружения BOT_TOKEN
        String botToken = System.getenv("BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            System.err.println("Ошибка: переменная окружения BOT_TOKEN не задана!");
            return;
        }

        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(botToken, new SimpleBot(botToken));
            System.out.println("Bot started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
