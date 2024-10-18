package com.uk.accomodations.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccommodationsApiService {


    private final RestTemplate restTemplate;

    @Autowired
    public AccommodationsApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<String> fetchAccommodations(long chatId) {
        // Step 1: Call the real API and obtain a JSON response
        String jsonResponse = callRealAccommodationApi();
        return parseAccommodationJson(jsonResponse);
    }

    public String extractUrlFromAccommodation(String accommodation) {
        // Logic to extract the URL from the accommodation details
        // For demonstration, returning a fixed URL. You should implement your logic.
        return "http://example.com"; // Replace with actual URL extraction
    }


    // Real method to call the API using RestTemplate
    private String callRealAccommodationApi() {
        String url = "https://eecf8975-6c57-4990-969b-6a32dc2c0aff.mock.pstmn.io/hotels";
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Parse the JSON response using Jackson
    private List<String> parseAccommodationJson(String jsonResponse) {
        List<String> accommodations = new ArrayList<>();
        try {
            // Use Jackson's ObjectMapper to parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode hotelsArray = objectMapper.readTree(jsonResponse);

            // Loop through the JSON array of hotels
            for (JsonNode hotel : hotelsArray) {
                String name = hotel.get("name").asText();
                String city = hotel.get("city").asText();
                int price = hotel.get("price").asInt();
                int rating = hotel.get("rating").asInt();
                String imageUrl = hotel.get("image_url").asText();
                String mapUrl = hotel.get("map_url").asText();

                // Build the description string for each hotel
                String accommodationDetails = "<b>" + name + "</b>\n" +
                        "📍 <i>" + city + "</i>\n" +
                        "💵 Price: <b>IDR " + price + "</b>\n" +
                        "⭐ Rating: <b>" + rating + "</b> stars\n" +
                        "<a href=\"" + imageUrl + "\">🏨 View Image</a>\n" +
                        "<a href=\"" + mapUrl + "\">🗺️ View on Map</a>";

                accommodations.add(accommodationDetails);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accommodations;
    }
}
