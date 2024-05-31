package com.nirvana.notepad.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class TextDecoderEncoder {

    public static String encode(String text){
        try {
            text = URLEncoder.encode(text,"UTF-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String decode(String text){
        try {
            text = URLDecoder.decode(text,"UTF-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }
}
