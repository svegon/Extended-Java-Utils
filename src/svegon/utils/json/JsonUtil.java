package svegon.utils.json;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;
import svegon.utils.interfaces.JsonSerializable;
import svegon.utils.optional.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonUtil {
    private JsonUtil() {
        throw new AssertionError();
    }

    public static final Gson GSON = new Gson();
    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement parseFile(Path path) throws IOException, JsonParseException {
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    public static JsonElement parseURL(String url) throws IOException, JsonParseException {
        try(InputStream input = URI.create(url).toURL().openStream()) {
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(reader);
            return JsonParser.parseReader(bufferedReader);
        }
    }

    public static Optional<JsonArray> parseFileToArray(Path path) throws IOException, JsonParseException {
        JsonElement json = parseFile(path);

        if (json.isJsonArray()) {
            return Optional.of(json.getAsJsonArray());
        }

        return Optional.empty();
    }

    public static Optional<JsonArray> parseURLToArray(String url) throws IOException, JsonParseException {
        JsonElement json = parseURL(url);

        return Optional.ofNullable(json.isJsonArray() ? json.getAsJsonArray() : null);
    }

    public static Optional<JsonObject> parseFileToObject(Path path) throws IOException {
        JsonElement json = parseFile(path);

        return Optional.ofNullable(json.isJsonObject() ? json.getAsJsonObject() : null);
    }

    public static Optional<JsonObject> parseURLToObject(String url) throws IOException {
        JsonElement json = parseURL(url);

        return Optional.ofNullable(json.isJsonObject() ? json.getAsJsonObject() : null);
    }

    public static void saveToFile(JsonElement json, Path path) throws IOException {
        try(BufferedWriter writer = Files.newBufferedWriter(path)) {
            PRETTY_GSON.toJson(json, writer);
        }
    }

    public static Optional<JsonElement> getProperty(JsonObject map, String property) {
        return Optional.ofNullable(map.get(property));
    }

    public static Optional<JsonObject> getAsJsonObject(JsonElement json) {
        return Optional.ofNullable(json.isJsonObject() ? json.getAsJsonObject() : null);
    }

    public static Optional<JsonArray> getAsJsonArray(JsonElement json) {
        return Optional.ofNullable(json.isJsonArray() ? json.getAsJsonArray() : null);
    }

    public static Optional<String> getAsString(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isString()) {
                return Optional.of(primitive.getAsString());
            }
        }

        return Optional.empty();
    }

    public static OptionalBoolean getAsBoolean(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                return OptionalBoolean.of(json.getAsBoolean());
            }
        }

        return OptionalBoolean.empty();
    }

    public static OptionalByte getAsByte(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalByte.of(json.getAsByte());
            }
        }

        return OptionalByte.empty();
    }

    public static OptionalShort getAsShort(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalShort.of(json.getAsShort());
            }
        }

        return OptionalShort.empty();
    }

    public static OptionalInt getAsInt(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalInt.of(json.getAsInt());
            }
        }

        return OptionalInt.empty();
    }

    public static OptionalLong getAsLong(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalLong.of(json.getAsLong());
            }
        }

        return OptionalLong.empty();
    }

    public static OptionalChar getAsChar(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalChar.of(primitive.getAsCharacter());
            }
        }

        return OptionalChar.empty();
    }

    public static OptionalFloat getAsFloat(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalFloat.of(json.getAsFloat());
            }
        }

        return OptionalFloat.empty();
    }

    public static OptionalDouble getAsDouble(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return OptionalDouble.of(json.getAsDouble());
            }
        }

        return OptionalDouble.empty();
    }

    public static Optional<BigInteger> getAsBigInteger(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return Optional.of(json.getAsBigInteger());
            }
        }

        return Optional.empty();
    }

    public static Optional<BigDecimal> getAsBigDecimal(JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isNumber()) {
                return Optional.of(json.getAsBigDecimal());
            }
        }

        return Optional.empty();
    }

    public static Object parseFromJsonGeneric(JsonElement element) {
        if (element.isJsonObject()) {
            Map<String, Object> object = Maps.newLinkedHashMap();

            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                object.put(entry.getKey(), parseFromJsonGeneric(entry.getValue()));
            }

            return object;
        }

        if (element.isJsonArray()) {
            List<Object> array = Lists.newArrayList();

            element.getAsJsonArray().forEach(e -> array.add(parseFromJsonGeneric(e)));

            return array;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }

            if (primitive.isString()) {
                return primitive.getAsString();
            }

            if (primitive.isNumber()) {
                double number = primitive.getAsDouble();

                // I'll check for int at least
                if (number == (int) number) {
                    return (int) number;
                }

                return number;
            }

            throw new IllegalArgumentException();
        }

        if (element.isJsonNull()) {
            return null;
        }

        throw new IllegalArgumentException();
    }

    public static JsonElement parseToJsonGeneric(@Nullable Object object) {
        if (object == null) {
            return JsonNull.INSTANCE;
        }

        if (object instanceof JsonElement) {
            return (JsonElement) object;
        }

        if (object instanceof JsonSerializable serializable) {
            return serializable.serialize();
        }

        if (object instanceof Map<?, ?> map) {
            JsonObject jsonObject = new JsonObject();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                jsonObject.add(entry.getKey().toString(), parseToJsonGeneric(entry.getValue()));
            }

            return jsonObject;
        }

        if (object instanceof List<?> list) {
            JsonArray jsonArray = new JsonArray();

            list.forEach((e) -> jsonArray.add(parseToJsonGeneric(e)));

            return jsonArray;
        }

        if (object instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }

        if (object instanceof Number num) {
            return new JsonPrimitive(num);
        }

        if (object instanceof Character character) {
            return new JsonPrimitive(character);
        }

        if (object instanceof CharSequence) {
            return new JsonPrimitive(object.toString());
        }

        return JsonUtil.GSON.toJsonTree(object);
    }
}
