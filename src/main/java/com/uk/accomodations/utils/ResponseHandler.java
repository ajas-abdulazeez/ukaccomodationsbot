package com.uk.accomodations.utils;

import com.uk.accomodations.constants.ConstantPool;
import com.uk.accomodations.enums.UserState;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;
import static com.uk.accomodations.constants.ConstantPool.START_TEXT;
import static com.uk.accomodations.enums.UserState.*;

public class ResponseHandler {

    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(ConstantPool.CHAT_STATES);
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_NAME);
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, message);
            case AWAITING_REGION_SELECTION -> replyToRegionSelection(chatId);
            case AWAITING_POSTCODE_SELECTION -> replyToPostCodeSelection(chatId, message);
            case AWAITING_ACCOMMODATION_TYPE_SELECTION -> replyToAccommodationTypeSelection(chatId);
            case OBTAINING_RESULTS -> replyToObtainingResults(chatId);
            case STOP_CONVERSATION -> stopChat(chatId);
            default -> unexpectedMessage(chatId);
        }
    }

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("I did not expect that. For more information download our app available at playstore");
        sender.execute(sendMessage);
    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Thank you for your order. See you soon!\nPress /start to order again");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }


    private void promptWithKeyboardForState(long chatId, String text, ReplyKeyboard YesOrNo, UserState awaitingReorder) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(YesOrNo);
        sender.execute(sendMessage);
        chatStates.put(chatId, awaitingReorder);
    }

    private void replyToAccommodationSelection(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if ("1bhk".equalsIgnoreCase(message.getText())) {
            sendMessage.setText("We don't have 1bhk.\nsorry mate!! :)");
            sendMessage.setReplyMarkup(KeyboardFactory.getAccommodationTypeSelection());
            sender.execute(sendMessage);
        } else {
            sendMessage.setText("We don't have " + message.getText() + ". Please select from the options below.");
            sendMessage.setReplyMarkup(KeyboardFactory.getAccommodationTypeSelection());
            sender.execute(sendMessage);
            chatStates.put(chatId, OBTAINING_RESULTS);
        }
    }

    private void replyToName(long chatId, Message message) {
        // Set the region selection menu after the user provides their name
        promptWithKeyboardForState(chatId,
                "Hello " + message.getText() + ". Please choose your region from the options below:",
                KeyboardFactory.getRegionSelection(),  // Custom keyboard with region selection
                UserState.AWAITING_REGION_SELECTION);
    }

    private void replyToRegionSelection(long chatId) {
        promptWithKeyboardForState(chatId, "Please enter your postcode without space, for example LE13AB",
                KeyboardFactory.hideKeyboard(), // Custom keyboard
                UserState.AWAITING_POSTCODE_SELECTION);
    }


    private void replyToPostCodeSelection(long chatId, Message message) {
        promptWithKeyboardForState(chatId,
                "Hello Akash, For listing properties at" + message.getText() + ". Please choose your type of accommodation you are looking for from the options below:",
                KeyboardFactory.getAccommodationTypeSelection(),
                UserState.AWAITING_ACCOMMODATION_TYPE_SELECTION);
    }

    private void replyToAccommodationTypeSelection(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Here are the list of accommodations. \n Thankyou for choosing me!! :)");
        sendMessage.setReplyMarkup(KeyboardFactory.getRegionSelection());
        sender.execute(sendMessage);
        chatStates.put(chatId, OBTAINING_RESULTS);
    }

    private void replyToObtainingResults(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Thank you for choosing me!! :)");
        sendMessage.setReplyMarkup(KeyboardFactory.hideKeyboard());
        sender.execute(sendMessage);
        chatStates.put(chatId, STOP_CONVERSATION);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }



}
