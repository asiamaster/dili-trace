package com.dili.trace.jobs;

import com.dili.ss.util.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * TEST job
 */
@Component
public class TestJob {
    //交易数据当前时间往前推1小时
    private Integer tradeInterval = -1;
    private String queDateFormatter = "yyyyMMddHH:mm:ss";

    /**
     * test
     */
    @Scheduled(cron = "0 10 */1 * * ?")
    public void getTradeData() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Date endTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        String appid = "appid";

        String timp = String.valueOf(new Date().getTime() / 1000L);
        String stime = DateUtils.format(DateUtils.addHours(endTime, tradeInterval), queDateFormatter);
        String etime = DateUtils.format(endTime, queDateFormatter);
        String mdfive = "md5";
        String url = "orderdata" + "?appid=" + appid + "&timestamp=" + timp + "&sign=" + mdfive + "&stime=" + stime + "&etime=" + etime;
    }

    /**
     * get
     *
     * @param now
     */
    public void getTradeData(LocalDateTime now) {
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Date endTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        String appid = "appid";

        String timp = String.valueOf(new Date().getTime() / 1000L);
        String stime = DateUtils.format(DateUtils.addHours(endTime, tradeInterval), queDateFormatter);
        String etime = DateUtils.format(endTime, queDateFormatter);
        String mdfive = "md5";
        System.out.println(Arrays.asList(stime, etime));
    }

    /**
     * main
     *
     * @param args
     */
    public static void main(String[] args) {
        LocalDateTime now = new Date().toInstant().atZone(ZoneId.systemDefault()).withMinute(0).withSecond(0).toLocalDateTime();

        Date endTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        System.out.println(endTime);

    }


}
