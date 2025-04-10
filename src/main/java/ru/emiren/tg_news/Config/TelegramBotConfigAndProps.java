package ru.emiren.tg_news.Config;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TelegramBotConfigAndProps {

    @Bean
    public Keyboard mainMenuKeyboard() {
        return new InlineKeyboardMarkup(
//                new InlineKeyboardButton("Start").callbackData("/start"),
                new InlineKeyboardButton("News").callbackData("/news")
        ).addRow(
                new InlineKeyboardButton("Help").callbackData("/help"),
                new InlineKeyboardButton("Payments").callbackData("/payments")
        ).addRow(
                new InlineKeyboardButton("Settings").callbackData("/settings"),
                new InlineKeyboardButton("Subscription").callbackData("/subscription")
        ).addRow(
                new InlineKeyboardButton("About").callbackData("/about")
        ).addRow(
                new InlineKeyboardButton("Exit").callbackData("/exit")
        );
    }

//    @Bean
//    public Keyboard SettingsKeyboard() {
//        return new InlineKeyboardMarkup(
//
//        )
//    }

    @Bean
    public List<BotCommand> menuCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Start the Bot"));
        return commands;
    }

}
