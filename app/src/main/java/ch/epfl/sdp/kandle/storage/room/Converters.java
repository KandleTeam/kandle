package ch.epfl.sdp.kandle.storage.room;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

public class Converters {


    @TypeConverter
    public static Date toDate(Long dateLong) {
        if (dateLong == null) {
            throw new NullPointerException();
        }
        return new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }
        return date.getTime();
    }

    @TypeConverter
    public static String StringListToString(List<String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> stringToStringList(@Nullable String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type strings = new TypeToken<List<String>>() {
        }.getType();

        Gson gson = new Gson();
        return gson.fromJson(data, strings);
    }
}
