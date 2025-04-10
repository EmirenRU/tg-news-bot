package ru.emiren.tg_news.Service.Bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.LabeledPrice;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendInvoice;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.emiren.tg_news.DTO.NewsDTO;
import ru.emiren.tg_news.DTO.RoleDTO;
import ru.emiren.tg_news.DTO.UserChatIDs;
import ru.emiren.tg_news.DTO.UserDTO;
import ru.emiren.tg_news.Mapper.NewsMapper;
import ru.emiren.tg_news.Mapper.UserMapper;
import ru.emiren.tg_news.Model.Roles;
import ru.emiren.tg_news.Model.User;
import ru.emiren.tg_news.Service.News.NewsService;
import ru.emiren.tg_news.Service.Role.RoleService;
import ru.emiren.tg_news.Service.User.UserService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CommandHandler {

    private final NewsService newsService;
    private final UserService userService;
    private final RoleService roleRepository;

    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final TelegramBot bot;
    private final Keyboard mainKeyboard;

    private static final String unknownCommand = """
            Unknown command. Type /help for a list of commands.
            """;

    private static final String aboutCommandInfo = """
            Bot to provide news summary and some prediction
            Was created by Emiren: https://github.com/EmirenRU
            """;
    private static final String exitCommandInfo = """
            Goodbye, I'll be waiting.
            """;
    private static final String subscriptionCommandInfo = """
            User: %s
            Status: %s
            End of subscription: %s days
            """;
    private static final String subscriptionCommandInfoWithoutDate = """
            User: %s
            Status: %s
            """;
    private final String newsCommandText;
    private final ClassPathResource newsResource;

    @Autowired
    public CommandHandler(NewsService newsService,
                          UserService userService,
                          RoleService roleRepository,
                          TelegramBot telegramBot,
                          Keyboard mainMenuKeyboard,
                          String newsText,
                          ClassPathResource imgNewsCrownClassPathResource
                          ) {
        this.newsResource = imgNewsCrownClassPathResource;
        this.newsService = newsService;
        this.userService = userService;
        this.roleRepository = roleRepository;

        this.bot = telegramBot;
        this.mainKeyboard = mainMenuKeyboard;
        this.newsCommandText = newsText;

        commands.put("/start", this::startCommand);
        commands.put("/help",  this::helpCommand);
        commands.put("/news", this::newsCommand);
        commands.put("/payments", this::paymentCommand);
        commands.put("/settings", this::settingsCommand);
        commands.put("/subscription", this::subscriptionCommand);
        commands.put("/about", this::aboutCommand);
        commands.put("/exit", this::exitCommand);
    }

    private void newsCommand(Update update) {
        UserDTO user = UserMapper.mapToUserDTO(userService.findById(update.callbackQuery().from().id()).orElse(null));
        RoleDTO role = user != null ? user.getRole() : null;

        if (user != null) {
            if (role != null) {
                String text;

                if (role.getRole().equals("USER")) {
                    LocalDateTime now = LocalDateTime.now().minusMinutes(60);
                    log.info("localDate: {}", now);
                    List<NewsDTO> n = Objects.requireNonNull(newsService
                                    .getLastNewsForUser(now)
                                    .orElse(null))
                                      .stream()
                                      .map(NewsMapper::mapToNewsDTO)
                                      .toList();

                    log.info("News for {} has been created", n);

                    for (NewsDTO news : n) {
                        text = newsCommandText.formatted(news.getTitle(), news.getContent(), news.getPrediction(), news.getUrl(), news.getDate());
                        sendMessage(String.valueOf(update.callbackQuery().maybeInaccessibleMessage().chat().id()), text);
                    }
                } else if (role.getRole().equals("ADMIN") || role.getRole().equals("PREMIUM")) {
                    List<NewsDTO> n = Objects.requireNonNull(newsService
                                    .getLastNewsForPremiumUsers()
                                    .orElse(null))
                            .stream()
                            .map(NewsMapper::mapToNewsDTO)
                            .toList();

                    log.info("News for {} has been created", n);

                    for (NewsDTO news : n) {
                        text = newsCommandText.formatted(news.getTitle(), news.getContent(), news.getPrediction(), news.getUrl(), news.getDate());
                        sendMessage(String.valueOf(update.callbackQuery().maybeInaccessibleMessage().chat().id()), text);
                    }
                }
            }
            sendMessageWithKeyboard(String.valueOf(getChatIdFromCallback(update.callbackQuery())), "Menu", mainKeyboard);

        }
    }

    private void paymentCommand(Update update) {
        long chatId = getChatIdFromCallback(update.callbackQuery());
        String invoicePayload = "premium_sub_" + System.currentTimeMillis();

        log.info("Payment Section with chatId {}", chatId);
        log.info("Payment section with user {} and id {}", update.callbackQuery().from().id(), update.callbackQuery().id());

        int oneMonthPrice = 100;
        int threeMonthsPrice = 2;
        int sixMonthsPrice = 5;

        SendInvoice invoice = new SendInvoice(chatId,  "News Premium", "Premium for one month",  invoicePayload, "XTR",
                new LabeledPrice("1 month", oneMonthPrice)
        )
                .providerToken("")
                .sendEmailToProvider(false)
                .sendPhoneNumberToProvider(false)
                .needEmail(false)
                .needPhoneNumber(false)
                .needShippingAddress(false)
                .needName(false)
                .photoUrl("https://i.postimg.cc/02K7qzrJ/news-crown.png")
                .photoHeight(512)
                .photoSize(512)
                .replyMarkup(
                    new InlineKeyboardMarkup(
                        new InlineKeyboardButton("Pay").pay()
                    )
                );

        log.info("Payment section with chatId {}", chatId);
        log.info("invoice: {}", invoice);
        SendResponse response = bot.execute(invoice);

        if (!response.isOk()) {
            log.error("Payment section with chatId {} failed", chatId);
            bot.execute(new SendMessage(chatId, "Payment failed"));
        }
        log.info(response.toString());
    }

    private void settingsCommand(Update update) {
        sendMessageWithKeyboard(String.valueOf(getChatIdFromCallback(update.callbackQuery())), "Not implemented yet", mainKeyboard);
    }

    private void aboutCommand(Update update) {
        sendMessageWithKeyboard(String.valueOf(getChatIdFromCallback(update.callbackQuery())), aboutCommandInfo, mainKeyboard);
    }

    private void exitCommand(Update update) {
        Long chatId = getChatIdFromCallback(update.callbackQuery());
        Long userId = getUserIdFromCallback(update.callbackQuery());
        sendMessage(String.valueOf(chatId), exitCommandInfo);

        User user = userService.findById(userId).orElse(null);
        if (user != null) {
            userService.deleteUser(user);
        }
    }

    private void subscriptionCommand(Update update) {
        User user = userService.findById(getUserIdFromCallback(update.callbackQuery())).orElse(null);
        long chatId = getChatIdFromCallback(update.callbackQuery());

        if (user == null) {
            sendMessageWithKeyboard(String.valueOf(chatId), "Strange... We have no trace about you", mainKeyboard);
            return;
        }

        String role = user.getRole().getRole();
        String text;

        if (role.equals("USER") || role.equals("ADMIN")) {
            text = subscriptionCommandInfoWithoutDate.formatted(user.getUsername(), role);
        } else if (role.equals("PREMIUM")) {
            text = getPremiumUserMessage(user);
        } else {
            return;
        }

        sendMessageWithKeyboard(String.valueOf(chatId), text, mainKeyboard);
    }

    private String getPremiumUserMessage(User user) {
        Date endDate = user.getEndOfSubscription();
        if (endDate != null) {
            LocalDate endDay = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String days = String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), endDay));
            return subscriptionCommandInfo.formatted(user.getUsername(), user.getRole().getRole().toLowerCase(Locale.ROOT), days);
        } else {
            return subscriptionCommandInfoWithoutDate.formatted(user.getUsername(), user.getRole().getRole().toLowerCase(Locale.ROOT));
        }
    }

    private void helpCommand(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        String chatId = String.valueOf(callbackQuery.maybeInaccessibleMessage().chat().id());
        String response = "Available commands:\n/start - Start the bot\n/help - Show this help message\n...";
        sendMessageWithKeyboard(chatId, response, mainKeyboard);
    }

    public void handleCallbackCommand(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        Integer messageId = getMessageIdFromCallback(callbackQuery);
        String command = getDataFromCallback(callbackQuery);
        Long chatId = getChatIdFromCallback(callbackQuery);
        try {
            log.info("chatId {}, message id {}", chatId, messageId);
            log.info("command {}", command);
            removeInlineKeyboard(chatId, messageId, "temp");
            handleCommand(command, update);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    public void sendCriticalNews(NewsDTO newsDTO) {
        List<UserChatIDs> premiums = userService.getAllPremiumUsers();
        for (UserChatIDs premium : premiums) {
            sendMessage(premium.getChatId().toString(), newsCommandText.formatted(
                    newsDTO.getTitle(), newsDTO.getContent(), newsDTO.getPrediction(), newsDTO.getUrl(), newsDTO.getDate()
            ));
        }
    }

    private Long getUserIdFromCallback(CallbackQuery callbackQuery) {
        return callbackQuery.from().id();
    }

    private String getUsernameFromCallback(CallbackQuery callbackQuery) {
        return callbackQuery.from().username();
    }

    private Integer getMessageIdFromCallback(CallbackQuery callbackQuery) {
        return callbackQuery.maybeInaccessibleMessage().messageId();
    }

    private String getDataFromCallback(CallbackQuery callbackQuery) {
        return callbackQuery.data();
    }

    private Long getChatIdFromCallback(CallbackQuery callbackQuery) {
        return callbackQuery.maybeInaccessibleMessage().chat().id();
    }

    public void handleCommand(String command, Update update) {
        Command cmd = commands.get(command);
        if (cmd != null) {
            cmd.execute(update);
        } else {
            if (update.message() != null) {
                sendMessageWithKeyboard(String.valueOf(update.message().chat().id()), unknownCommand, mainKeyboard);
            }
            else if (update.callbackQuery() != null) {
                sendMessageWithKeyboard(String.valueOf(update.callbackQuery().maybeInaccessibleMessage().chat().id()), unknownCommand, mainKeyboard);
            }
        }
    }

    private void handlePayment(Update update) {
    }

    private void handlePreCheckout(Update update) {
    }

    public void startCommand(Update update) {
        Message message = update.message();
        String chatId = String.valueOf(message.chat().id());
        Long userId = message.from().id();
        if (userService.findById(userId).isEmpty()) {
            User newUser = new User();
            Roles role = roleRepository.findRole("USER");
            newUser.setRole(role);
            newUser.setUsername(message.from().username());
            newUser.setUserId(userId);
            newUser.setDateOfSubscription(null);
            newUser.setEndOfSubscription(null);
            userService.saveUser(newUser);
        }
        String response = "Welcome user...\nPress /help to see all possible commands.";
        sendMessageWithKeyboard(chatId, response, mainKeyboard);
    }

    private void sendMessageWithKeyboard(String chatId, String response, Keyboard keyboard) {

        SendMessage sendMessage =
                new SendMessage(chatId, response)
                    .replyMarkup(keyboard);
        bot.execute(sendMessage);
    }

    private void sendMessage(String chatId, String message) {
        if (!message.isEmpty()) {
            bot.execute(new SendMessage(chatId, message));
        } else {
            bot.execute(new SendMessage(chatId, "Unknown command. Type /help for a list of commands."));
        }
    }

    private void removeInlineKeyboard(Long chatId, Integer messageId, String newText){
        if (chatId == null || messageId == null) {
            log.error("Chat ID or Message ID is null. Cannot remove inline keyboard.");
            return;
        }
        DeleteMessage deleteMessage =
                new DeleteMessage(chatId, messageId);

        if (deleteMessage != null) {
            log.info("removing inline keyboard");
            bot.execute(deleteMessage);
        }
    }

//    @FunctionalInterface
//    public interface MessageCommand {
//        void execute(Message message);
//    }

//    @FunctionalInterface
//    public interface CallbackQueryCommand{
//        void execute(CallbackQuery callbackQuery);
//    }

    @FunctionalInterface
    public interface Command{
        void execute(Update update);
    }

//    @FunctionalInterface
//    public interface Command{
//        void execute();
//    }
}
