package com.uk.accomodations.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class KeyboardFactory {


    public static ReplyKeyboardMarkup getAccommodationTypeSelection() {
        // Create a separate KeyboardRow for each accommodation type to display as columns
        KeyboardRow row1 = new KeyboardRow();
        row1.add("1 BHK");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("2 BHK");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Terraced");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("Detached");

        KeyboardRow row5 = new KeyboardRow();
        row5.add("Semi-detached");

        KeyboardRow row6 = new KeyboardRow();
        row6.add("Flats/Studio");

        KeyboardRow row7 = new KeyboardRow();
        row6.add("Go Back to Main Menu");

        // Combine all rows into a single list for the keyboard markup
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1, row2, row3, row4, row5, row6, row7));
        keyboardMarkup.setResizeKeyboard(true);  // Adjust keyboard size to fit the screen
        keyboardMarkup.setOneTimeKeyboard(true); // Optional: hide after the selection is made
        keyboardMarkup.setSelective(true);

        return keyboardMarkup;

    }

    public static ReplyKeyboardMarkup getRegionSelection() {
        KeyboardRow row = new KeyboardRow();
        row.add("England");
        row.add("Scotland");
        row.add("Wales");
        row.add("Northern Ireland");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);  // Resize keyboard to fit screen
        keyboardMarkup.setOneTimeKeyboard(true); // Hide after selection (optional)
        return keyboardMarkup;
    }


    public static ReplyKeyboardRemove hideKeyboard() {
        return new ReplyKeyboardRemove(true);  // This will hide the keyboard
    }

}
