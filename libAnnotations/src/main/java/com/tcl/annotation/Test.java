package com.tcl.annotation;

import com.google.gson.Gson;


/**
 * Created by lzy on 2021/12/1.
 */
public class Test {
    public static void printCellName(String cellName) {
        System.out.println("printCellName" + cellName);
    }

    public static <T> T testGson(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }
}
