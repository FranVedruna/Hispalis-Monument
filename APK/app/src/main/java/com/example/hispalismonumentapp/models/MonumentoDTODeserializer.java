package com.example.hispalismonumentapp.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MonumentoDTODeserializer implements JsonDeserializer<MonumentoDTO> {
    @Override
    public MonumentoDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        MonumentoDTO dto = new MonumentoDTO();

        // Deserializar campos simples
        if (jsonObject.has("id")) dto.setId(jsonObject.get("id").getAsInt());
        if (jsonObject.has("nombre")) dto.setNombre(jsonObject.get("nombre").getAsString());
        if (jsonObject.has("descripcionEs")) dto.setDescripcionEs(jsonObject.get("descripcionEs").getAsString());
        if (jsonObject.has("descripcionEn")) dto.setDescripcionEn(jsonObject.get("descripcionEn").getAsString());
        if (jsonObject.has("fotoUrl")) dto.setFotoUrl(jsonObject.get("fotoUrl").getAsString());
        if (jsonObject.has("latitud")) dto.setLatitud(jsonObject.get("latitud").getAsDouble());
        if (jsonObject.has("longitud")) dto.setLongitud(jsonObject.get("longitud").getAsDouble());
        if (jsonObject.has("wikiPath")) {
            JsonElement wikiPath = jsonObject.get("wikiPath");
            dto.setWikiPath(wikiPath.isJsonNull() ? null : wikiPath.getAsString());
        }

        // Manejo especial para types
        if (jsonObject.has("types")) {
            JsonElement typesElement = jsonObject.get("types");
            List<TypeDTO> types = new ArrayList<>();

            if (typesElement.isJsonArray()) {
                JsonArray typesArray = typesElement.getAsJsonArray();
                for (JsonElement element : typesArray) {
                    TypeDTO typeDTO = new TypeDTO();

                    if (element.isJsonPrimitive()) {
                        // Caso 1: Es un string simple (ej. "Cultural")
                        typeDTO.setTypeName(element.getAsString());
                    } else if (element.isJsonObject()) {
                        // Caso 2: Es un objeto (ej. {"typeName": "Cultural"})
                        JsonObject typeObj = element.getAsJsonObject();
                        if (typeObj.has("typeName")) {
                            typeDTO.setTypeName(typeObj.get("typeName").getAsString());
                        } else if (typeObj.has("name")) {
                            // Por si acaso el servidor usa "name" en lugar de "typeName"
                            typeDTO.setTypeName(typeObj.get("name").getAsString());
                        }
                    }

                    types.add(typeDTO);
                }
            }
            dto.setTypes(types);
        }

        return dto;
    }
}