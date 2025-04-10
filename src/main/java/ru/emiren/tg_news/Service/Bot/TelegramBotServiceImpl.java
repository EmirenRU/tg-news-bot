package ru.emiren.tg_news.Service.Bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetChatMenuButton;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.emiren.tg_news.Model.User;
import ru.emiren.tg_news.Service.Role.RoleService;
import ru.emiren.tg_news.Service.User.UserService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Getter
public class TelegramBotServiceImpl {

    private final CommandHandler commandHandler;
    private final TelegramBot bot;
    private final List<BotCommand> botCommands;
    private final UserService userService;
    private final String receipt;
    private final RoleService roleService;

    @Autowired
    public TelegramBotServiceImpl(CommandHandler commandHandler, TelegramBot telegramBot, List<BotCommand> botCommands, UserService userService, String receipt, RoleService roleService) {
        this.commandHandler = commandHandler;
        this.bot = telegramBot;
        this.botCommands = botCommands;
        this.userService = userService;
        this.receipt = receipt;
        this.roleService = roleService;
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    if (update.preCheckoutQuery() != null) {
                        handlePreCheckout(update);
                    } else if (update.callbackQuery() != null) {
                        log.debug("Update {}", update.callbackQuery());
                        handleCallbackQuery(update);
                    } else if (update.message() != null) {
                        handleUpdate(update);
                    }
                } catch (Exception e) {
                    log.warn("Update {} failed with error", update.callbackQuery(), e);
                    if (update.message() != null) {
                        bot.execute(new SendMessage(update.message().chat().id(),
                                "An error occurred. Please try again or contact support."));
                    }
                }

            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        setupMenuButtons();
    }

    private void handleSuccessfulPayment(Update update) {
        Message message = update.message();
        SuccessfulPayment sp = update.message().successfulPayment();
        long chatId = message.chat().id();

        User user = userService.findById(message.from().id()).orElse(null);
        if (user == null) {
            user = new User();
            user.setId(chatId);
            user.setUsername(message.from().username());
            user.setUserId(message.from().id());

        }

        user.setDateOfSubscription(user.getDateOfSubscription() == null ? new Date() : user.getDateOfSubscription());
        user.setEndOfSubscription(user.getEndOfSubscription() == null
                ? Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
                : Date.from(user.getEndOfSubscription().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        user.setRole(roleService.findRole("PREMIUM"));

        String res = receipt.formatted(sp.totalAmount() + " " + sp.currency(), user.getEndOfSubscription(), sp.invoicePayload());
        userService.saveUser(user);

        bot.execute(new SendMessage(chatId, res));
    }

    private void handlePreCheckout(Update update) {
        PreCheckoutQuery query = update.preCheckoutQuery();
        log.info("Pre checkout query {}", query);
        if (isValidPayment(query)) {
            bot.execute(new AnswerPreCheckoutQuery(query.id()));
            log.info("Pre checkout successful");
        } else {
            bot.execute(new AnswerPreCheckoutQuery(query.id(), "Payment validation failed"));
            log.warn("Pre checkout query {} failed", query);
        }
    }

    private boolean isValidPayment(PreCheckoutQuery query) {
        return (query != null && query.invoicePayload().startsWith("premium_sub_") && userService.ifExists(query.from().id()));
    }

    private void handleCallbackQuery(Update update) {
        log.info("In handleCallbackQuery");
        log.info("Handle callback query: {}", update.callbackQuery().from().id());
        log.info("Handle callback query: {}", update);

        commandHandler.handleCallbackCommand(update);
    }

    private void handleUpdate(Update update) {

        if (update.message() != null && update.message().text() != null) {
            Message message = update.message();
            String command = message.text().split(" ")[0];
            commandHandler.handleCommand(command, update);
        } else if (update.message().successfulPayment() != null) {
            handleSuccessfulPayment(update);
        }
    }

    private void setupMenuButtons(){
        bot.execute(new SetMyCommands(botCommands.toArray(new BotCommand[0])));
    }
}
