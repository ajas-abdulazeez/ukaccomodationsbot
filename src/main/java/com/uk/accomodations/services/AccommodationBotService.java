package com.uk.accomodations.services;

import com.uk.accomodations.constants.ConstantPool;
import com.uk.accomodations.utils.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class AccommodationBotService extends AbilityBot{

    private final ResponseHandler responseHandler;
    private final String token;
    private final String username;

    public AccommodationBotService(@Value("${BOT_TOKEN}") String token,
                                   @Value("${telegram.bot.username}") String username,
                                   Environment environment) {
        super(token, username);  // Pass to AbilityBot constructor
        this.token = token;
        this.username = username;
        this.responseHandler = new ResponseHandler(silent, db);  // Ensure this initialization works
    }



    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(ConstantPool.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> responseHandler.replyToStart(ctx.chatId()))
                .build();
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action = (abilityBot, upd) -> responseHandler.replyToButtons(getChatId(upd), upd.getMessage());
        return Reply.of(action, Flag.TEXT,upd -> responseHandler.userIsActive(getChatId(upd)));
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            // Check if the update contains a callback query
            if (update.hasCallbackQuery()) {
                // Process the callback query
                responseHandler.onCallbackQueryReceived(update.getCallbackQuery());
            }
        }
        // Call the superclass method to handle other types of updates
        super.onUpdatesReceived(updates);
    }

    @Override
    public void onClosing() {
        super.onClosing();
    }
}
