package com.architecture.admin.libraries;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*****************************************************
 * 시간 라이브러리
 ****************************************************/
@Component
@Data
public class DateLibrary {

    /**
     * date 형식 시간 구하기
     * @return UTC 기준 시간 yyyy-MM-dd hh:mm:ss
     */
    public String getDatetime() {
        java.util.Date dateNow = new java.util.Date(System.currentTimeMillis());
        
        // 타임존 UTC 기준
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatDatetime.setTimeZone(utcZone);

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        return formatDatetime.format(dateNow);
    }

    /**
     * 로컬시간을 UTC 시간으로 변경
     * @param date 로컬 시간 yyyy-MM-dd hh:mm:ss
     * @return UTC 기준 시간 yyyy-MM-dd hh:mm:ss
     */
    public String localTimeToUtc(String date) {
        // 타임존 UTC 기준값
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatDatetime.setTimeZone(utcZone);
        Timestamp timestamp = Timestamp.valueOf(date);

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        return formatDatetime.format(timestamp);
    }


    /**
     * UTC 시간을 로컬시간으로 변경
     * @param date UTC 시간 yyyy-MM-dd hh:mm:ss
     * @return 로컬 시간 yyyy-MM-dd hh:mm:ss
     */
    public String utcToLocalTime(String date) {
        // 입력시간을 Timestamp 변환
        long utcTime = Timestamp.valueOf( date ).getTime();
        TimeZone z = TimeZone.getDefault();
        int offset = z.getOffset(utcTime); // getRawOffset는 썸머타임 반영 문제로 getOffset 사용
        // Timestamp 변환시 UTC 기준으로 로컬타임과 차이 발생하여 2회 적용
        long localDateTime = utcTime + (offset * 2) ;

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatDatetime.format(new Timestamp(localDateTime));
    }

    /**
     * 몇 주 전 날짜 구하기 (UTC 기준)
     *
     * @return
     */
    public String getAgoWeek(int amount) {

        Calendar cal = Calendar.getInstance();
        String stringAgoWeek = null;

        cal.add(Calendar.DATE, -amount);
        Date agoWeek = cal.getTime();
        SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // 타임존 UTC 기준
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        formatDatetime.setTimeZone(utcZone);

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        stringAgoWeek = formatDatetime.format(agoWeek);

        return stringAgoWeek;
    }

    /**
     * 몇 시간 전 날짜 구하기 (UTC 기준)
     *
     * @return
     */
    public String getAgoHour(int amount) {

        Calendar cal = Calendar.getInstance();
        String stringAgoHourDate = null;

        cal.add(Calendar.HOUR, -amount);
        Date agoOneHour = cal.getTime();
        SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // 타임존 UTC 기준
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        formatDatetime.setTimeZone(utcZone);

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        stringAgoHourDate = formatDatetime.format(agoOneHour);

        return stringAgoHourDate;
    }

    /**
     * timestamp 형식 시간 구하기
     */
    public String getTimestamp() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        return String.valueOf(time.getTime() / 1000L);
    }
}
