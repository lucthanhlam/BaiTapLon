package net.luclam.schedule;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.StringRes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class AppHelper {

    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static void showToastSuccess(Context context, @StringRes int message) {
        Toasty.success(context, message, Toasty.LENGTH_SHORT).show();
    }

    public static void showToastError(Context context, @StringRes int message) {
        Toasty.error(context, message, Toasty.LENGTH_SHORT).show();
    }

    public static String hashingMd5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static int getColorPurple() {
        return Color.argb(255, 156, 39, 176);
    }

    public static long convertEpochTimeFromDate(String strDate) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static Date convertStringToDate(String str) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String toStringDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
