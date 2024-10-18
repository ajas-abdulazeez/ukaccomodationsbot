package com.uk.accomodations.utils;

import com.uk.accomodations.constants.ConstantPool;
import com.uk.accomodations.enums.UserState;
import com.uk.accomodations.services.AccommodationsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.uk.accomodations.constants.ConstantPool.START_TEXT;
import static com.uk.accomodations.enums.UserState.*;


public class ResponseHandler {

    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    private final AccommodationsApiService accommodationsApiService;


    @Autowired
    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        this.chatStates = db.getMap(ConstantPool.CHAT_STATES);

        RestTemplate restTemplate = new RestTemplate();
        this.accommodationsApiService = new AccommodationsApiService(restTemplate);

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
        System.out.print("Inside chat username is "+message.getFrom().getUserName());

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

    public void onCallbackQueryReceived(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        // Check the callback data to determine which action to perform
        if ("UPDATE_MESSAGE_TEXT".equals(callbackData)) {
            // Update the chat state when the button is clicked
            chatStates.put(chatId, STOP_CONVERSATION);

            // Send confirmation message to the user
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Chat closed, If you want to start again click here /start to start. Thank you!!!");

            sender.execute(message);
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
        System.out.println("entering here");
        // Prepare the accommodation data
        List<String> accommodations = accommodationsApiService.fetchAccommodations(chatId);

        System.out.println("response obtained");
        if (accommodations == null || accommodations.isEmpty()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Sorry, no accommodations are available at this time.");
            sendMessage.setReplyMarkup(KeyboardFactory.hideKeyboard());
            sender.execute(sendMessage);

        } else {
            System.out.println("entering else");
            // Iterate through each accommodation and send a separate message
            for (String accommodation : accommodations) {
                // Create the SendMessage object for each accommodation
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));

                // Construct the message text
                String messageText = "Here are the accommodation details:\n\n" + accommodation;

                // Create inline keyboard markup for the current accommodation
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                // Extract URL from accommodation data (this should return the correct URL)
                String url = accommodationsApiService.extractUrlFromAccommodation(accommodation); // Make sure this method is implemented

                // Create the inline button
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Open link");
                button.setUrl(url); // Set the URL for the button

                // Add the button to a row
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);

                // Set the keyboard to the message
                inlineKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                // Set the message text
                sendMessage.setText(messageText);
                sendMessage.setParseMode("HTML"); // Set the parse mode to Markdown


                sender.execute(sendMessage);

            }
        }

        chatStates.put(chatId, OBTAINING_RESULTS);
    }





    private void replyToObtainingResults(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Thank you for choosing me!! :)");

        // Create inline button
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // Create the InlineKeyboardButton with text and callback data
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Close the chat");
        button.setCallbackData("UPDATE_MESSAGE_TEXT");

        // Add button to the row
        rowInline.add(button);
        rowsInline.add(rowInline);

        // Set the keyboard to the markup
        markupInline.setKeyboard(rowsInline);

        // Attach markup to the message
        sendMessage.setReplyMarkup(markupInline);
        // Send the message
        sender.execute(sendMessage);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }



}
