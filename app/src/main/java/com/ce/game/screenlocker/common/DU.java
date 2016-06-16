package com.ce.game.screenlocker.common;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.ce.game.screenlocker.BuildConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * desc: debugU  改进版本
 * Created by KyleCe on 2015/9/9.
 *
 * @author KyleCe
 */
public class DU {

    public final static boolean ON = true;
//    public final static boolean ON = false;
//    public final static boolean ON = UrlAddress.HOST.equals("http://119.29.16.165/");

    public static Toast toast = null;

    private static final int DIVIDER_SIDES_LEN = 20;
    private static final int DIVIDER_LEN = 40;


    private static final int HEADER = 3;

    /**
     * desc：同single toast ，简化输入
     */
    public static void t(Context context, String msg) {
        singleToast(context, msg);
    }

    /**
     * toast and print the msg, separate with 't'
     */
    public static void tp(Context context, String msg) {
        t(context, msg);
        s('t', msg);
    }

    /**
     * 单toast，解决toast重复显示问题
     */
    public static void singleToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    /**
     * print if in debug mode
     *
     * @see #sd(Object...)
     */
    static public void sdb(Object... objs) {
        if (BuildConfig.DEBUG) sd(objs);
    }

    /**
     * 以第一个字符串为分割对象，后继内容每个各占一行地打印到后面的行
     * 预期格式如下：
     * -----------------------objs[0]------------------------
     * *  objs[1]
     * *  objs[2]
     * *  objs[3]
     * *  ...
     * ------------------------------------------------------
     */
    static public void sd(Object... objs) {
        if (objs == null || objs.length == 0 || !ON) return;
        if (objs.length == 1) {
            sop(objs);
            return;
        }

        synchronized ("1") {
            // get divider string and its len
            String divider = objs[0].toString();
            int dividerLen = divider.length();

            // get the longest len of the objs len
            int longestLine = 0;
            for (Object ob : objs) {
                if (ob == null) continue;
                longestLine = Math.max(longestLine, ob.toString().length());
            }

            // the fixed len of every line
            int fixedLen = Math.max(longestLine + HEADER * 2, dividerLen + DIVIDER_SIDES_LEN * 2);

            // the first line: divider line
            printMinusByLen(DIVIDER_SIDES_LEN);
            print(divider);
            printMinusByLen(fixedLen - dividerLen - DIVIDER_SIDES_LEN);
            println();

            // print the rest content line by line
            int len = objs.length;
            for (int i = 1; i < len; i++) {
                if (objs[i] == null) continue;

                // header + space : "*  "
                printStarByLen(1);
                printSpaceByLen(2);

                // content
                print(objs[i]);

                // space
                printSpaceByLen(fixedLen - HEADER - objs[i].toString().length());

                println();
            }

            // print wrapper line
            printEqualByLen(fixedLen);
            System.out.println();
        }

    }

    /**
     * print minus "-" by length
     */
    private static void printMinusByLen(int len) {
        if (len <= 0) return;

        for (int i = 0; i < len; i++)
            print("-");
    }

    /**
     * print star "*" by length
     */
    private static void printStarByLen(int len) {
        if (len <= 0) return;

        for (int i = 0; i < len; i++)
            print("*");
    }

    /**
     * print equal "=" by length
     */
    private static void printEqualByLen(int len) {
        if (len <= 0) return;

        for (int i = 0; i < len; i++)
            print("=");
    }

    /**
     * print minus "-" by length
     */
    private static void printSpaceByLen(int len) {
        if (len <= 0) return;

        for (int i = 0; i < len; i++)
            print(" ");
    }

    /**
     * print object o, short cut of System.out.print()
     */
    private static void print(Object o) {
        if (o == null) return;

        System.out.print(o);
    }

    /**
     * print line, short cut of System.out.println()
     */
    private static void println() {
        System.out.println();
    }


    /**
     * 打印函数,命名为d
     * 以参数的第一个非null的toString结果的第一个字符为分割符，打印内容
     */
    static public void d(Object... objs) {
        sop(objs);
    }

    /**
     * 打印函数,命名为s
     * 以参数的第一个非null的toString结果的第一个字符为分割符，打印内容
     */
    static public void s(Object... objs) {
        sop(objs);
    }

    /**
     * 打印函数
     */
    static public void sop(Object... objs) {
        if (objs == null || !ON) return;
        if (objs.length >= 1 && objs[0] instanceof Character) {
            char c = (char) objs[0];
            printDividerLine(c);
        } else {
            // no divider char input, choose the default #
            printDividerLine('#');
        }
        int len = objs.length;
        int k = objs[0] instanceof Character ? 1 : 0;
        for (int i = k; i < len; i++)
            if (objs[i] == null) continue;
            else {
                System.out.print(objs[i].toString());
                // not last one, print the divider
                if (i != len - 1)
                    System.out.print(" -- ");
            }
        System.out.println();
    }

    /**
     * desc: print the divider line
     *
     * @param c divider char
     */
    private static void printDividerLine(char c) {
        for (int i = 0; i < DIVIDER_SIDES_LEN; i++)
            System.out.print("-");
        for (int i = 0; i < DIVIDER_LEN; i++)
            System.out.print(c);
        for (int i = 0; i < DIVIDER_SIDES_LEN; i++)
            System.out.print("-");
        System.out.println();
    }


    /**
     * desc: 并没有加标志位判断，保证一定能够显示toast，所以调试时最好不要toast来查看调试信息
     * 默认为short
     */
    static public void toast(Context c, String s) {
        shortToast(c, s);
    }

    /**
     * desc: 加标志位判断，调试时才toast提示
     */
    static public void toastDebug(Context c, String s) {
        if (!ON) return;
        toast(c, s);
    }

    /**
     * Toast 函数 short
     */
    static public void shortToast(Context c, String s) {
        showToast(c, s, true);
    }

    /**
     * Toast 函数 long
     */
    static public void longToast(Context c, String s) {
        showToast(c, s, false);
    }


    /**
     * b: true 时short，false 时long
     */
    static public void showToast(Context c, String s, boolean b) {
        if (c == null) return;
        Toast.makeText(c, s, b ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    /**
     * check the object is null ?
     */
    static public boolean isNull(Object object) {
        return object == null;
    }

    /**
     * check the object is not null ?
     */
    static public boolean notNull(Object object) {
        return !isNull(object);
    }

    /**
     * Returns true if the string is not zero length
     *
     * @param str the string to be examined
     * @return true if str is not zero length
     */
    static public boolean notEmpty(@Nullable CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        return TextUtils.isEmpty(str);
    }


    /**
     * thread pool trick
     */

    private static final int MAX_THREAD_COUNT = 5;
    private static ExecutorService mThreadPoolExecutor;

    /**
     * get thread pool service, limited by the max thread count variable
     *
     * @return thread pool service executor
     * @see #MAX_THREAD_COUNT
     */
    public static ExecutorService getThreadPool() {
        if (mThreadPoolExecutor == null)
            synchronized (DU.class) {
                return mThreadPoolExecutor == null ? Executors.newFixedThreadPool(MAX_THREAD_COUNT)
                        : mThreadPoolExecutor;
            }
        return mThreadPoolExecutor;
    }

    /**
     * execute runnable with thread pool
     *
     * @param runnable to execute
     * @see #getThreadPool()
     */
    public static void execute(Runnable runnable) {
        if (DU.isNull(runnable)) return;

        getThreadPool().execute(runnable);
    }

    /**
     * @see #post(long, Handler, Runnable)
     */
    public static void post(Handler handler, Runnable runnable) {
        post(0, handler, runnable);
    }

    /**
     * @param delay    delay to schedule
     * @param handler  handler to handle runnable
     * @param runnable to run
     */
    public static void post(long delay, Handler handler, Runnable runnable) {
        if (handler == null) return;
        if (runnable == null) return;
        if (delay < 0) delay = Math.abs(delay);

        handler.postDelayed(runnable, delay);
    }


    /**
     * scheduled executor
     */
    private static final int MAX_SCHEDULE_COUNT = 3;
    private static ScheduledExecutorService scheduledExecutor;


    /**
     * get schedule executor service
     *
     * @return executor
     */
    public static ScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null)
            synchronized (DU.class) {
                return scheduledExecutor == null ? Executors.newScheduledThreadPool(MAX_SCHEDULE_COUNT)
                        : scheduledExecutor;
            }

        return scheduledExecutor;
    }

    /**
     * schedule a runnable of the period right now (in unit milliseconds)
     *
     * @param period   period
     * @param runnable
     */
    public static ScheduledFuture schedule(long period, final Runnable runnable) {
        return schedule(0, period, TimeUnit.MILLISECONDS, runnable);
    }

    /**
     * schedule a runnable of the period after delay (in unit milliseconds)
     *
     * @param delay    delay to post
     * @param period   period
     * @param runnable
     */
    public static ScheduledFuture schedule(long delay, long period, final Runnable runnable) {
        return schedule(delay, period, TimeUnit.MILLISECONDS, runnable);
    }

    /**
     * schedule a runnable of the period right now (in unit milliseconds)
     *
     * @param delay    delay to post
     * @param period   period
     * @param unit     time unit
     * @param runnable
     */
    public static ScheduledFuture schedule(long delay, long period, TimeUnit unit, final Runnable runnable) {
        if (isNull(runnable)) return null;

        return getScheduledExecutor().scheduleAtFixedRate(runnable, delay, period, unit);
    }

    public static long time(){
        return System.currentTimeMillis();
    }
}
