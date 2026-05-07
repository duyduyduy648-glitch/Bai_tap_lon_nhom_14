package com.auction.dao;

import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ItemAdapter implements JsonDeserializer<Item> {
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        if (jsonObject.has("artist") || jsonObject.has("painter") || jsonObject.has("material")) {
            return context.deserialize(jsonObject, Art.class);
        } else if (jsonObject.has("brand") || jsonObject.has("warrantyMonths")) {
            return context.deserialize(jsonObject, Electronics.class);
        } else if (jsonObject.has("engine") || jsonObject.has("mileage")) {
            return context.deserialize(jsonObject, Vehicle.class);
        }

        // Mặc định tạm thời ép về Art nếu không nhận diện được
        return context.deserialize(jsonObject, Art.class);
    }
}