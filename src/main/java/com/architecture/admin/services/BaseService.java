package com.architecture.admin.services;

import com.architecture.admin.config.SessionConfig;
import com.architecture.admin.libraries.*;
import com.architecture.admin.libraries.exception.CurlException;
import com.architecture.admin.models.daosub.block.BlockMemberDaoSub;
import com.architecture.admin.models.daosub.member.MemberDaoSub;
import com.architecture.admin.models.dto.block.BlockMemberDto;
import com.architecture.admin.services.admin.AdminCurlService;
import com.architecture.admin.services.token.TokenCurlService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*****************************************************
 * 코어 서비스
 ****************************************************/
@Service
public class BaseService {

    // 시간 라이브러리 참조
    @Autowired
    protected DateLibrary dateLibrary;

    // 암호화 라이브러리
    @Autowired
    protected SecurityLibrary securityLibrary;

    // 세션
    @Autowired
    protected HttpSession session;

    // 텔레그램
    @Autowired
    protected TelegramLibrary telegramLibrary;

    // Redis 라이브러리
    @Autowired
    protected RedisLibrary redisLibrary;

    // Curl 라이브러리
    @Autowired
    protected CurlLibrary curlLibrary;

    /**
     * 메시지 가져오는 라이브러리
     */
    @Autowired
    protected MessageSource messageSource;
    @Autowired
    protected MemberDaoSub memberDaoSub;
    @Autowired
    protected AdminCurlService adminCurlService;
    @Autowired
    protected TokenCurlService tokenCurlService;
    // 차단 회원 관련
    @Autowired
    private BlockMemberDaoSub blockMemberDaoSub;

    /*****************************************************
     * 세션 값 가져오기
     ****************************************************/
    public String getSession(String id) {
        return (String) session.getAttribute(id);
    }

    /*****************************************************
     * 회원 정보 불러오기
     ****************************************************/
    public String getMemberInfo(String key) {

        JSONObject json = new JSONObject(getSession(SessionConfig.MEMBER_INFO));

        return json.getString(key);
    }

    /*****************************************************
     * 레디스
     ****************************************************/
    // 레디스 값 생성
    public void setRedis(String key, String value, Integer expiredSeconds) {
        redisLibrary.setData(key, value, expiredSeconds);
    }

    // 레디스 값 불러오기
    public String getRedis(String key) {
        return redisLibrary.getData(key);
    }

    // 레디스 값 삭제하기
    public void removeRedis(String key) {
        redisLibrary.deleteData(key);
    }

    /*****************************************************
     * Curl
     ****************************************************/
    // get
    public String getCurl(String url, String header) {
        return curlLibrary.get(url, header);
    }

    // post
    public String postCurl(String url, Map dataset) {
        return curlLibrary.post(url, dataset);
    }

    /*****************************************************
     * 암호화 처리
     ****************************************************/
    // 양방향 암호화 암호화
    public String encrypt(String str) throws Exception {
        return securityLibrary.aesEncrypt(str);
    }

    // 양방향 암호화 복호화
    public String decrypt(String str) throws Exception {
        return securityLibrary.aesDecrypt(str);
    }

    // 단방향 암호화
    public String md5encrypt(String str) {
        return securityLibrary.md5Encrypt(str);
    }

    /*****************************************************
     * 디버깅
     ****************************************************/
    public void d() {
        int iSeq = 2;
        System.out.println("======================================================================");
        System.out.println("클래스명 : " + Thread.currentThread().getStackTrace()[iSeq].getClassName());
        System.out.println("메소드명 : " + Thread.currentThread().getStackTrace()[iSeq].getMethodName());
        System.out.println("줄번호 : " + Thread.currentThread().getStackTrace()[iSeq].getLineNumber());
        System.out.println("파일명 : " + Thread.currentThread().getStackTrace()[iSeq].getFileName());
    }

    public void pushAlarm(String sendMessage) {
        telegramLibrary.sendMessage(sendMessage);
    }

    public void pushAlarm(String sendMessage, String sChatId) {
        telegramLibrary.sendMessage(sendMessage, sChatId);
    }

    /*****************************************************
     * Language 값 가져오기
     ****************************************************/
    public String langMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    public String langMessage(String code, @Nullable Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }


    /*****************************************************
     * get locale Language 현재 언어 값
     ****************************************************/
    public String getLocaleLang() {
        String localLang = LocaleContextHolder.getLocale().toString().toLowerCase();

        switch (localLang) {
            case "ko_kr", "ko", "kr":
                return "ko";
            case "en":
                return "en";
            default:
                return "en";
        }
    }

    /*****************************************************
     * ip 값 가져오기
     * private => public 으로 변환
     ****************************************************/
    public String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /*****************************************************
     * email값인지 체크하기
     ****************************************************/
    public boolean isEmail(String email) {
        boolean validation = false;

        if (Objects.equals(email, "") || email == null) {
            return false;
        }

        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            validation = true;
        }

        return validation;
    }


    /*****************************************************
     * 야간 시간인지 체크하기
     ****************************************************/
    public boolean checkNightTime() {
        // 21시부터 8시까지 매너타임
        List<Integer> list = Arrays.asList(21, 22, 23, 0, 1, 2, 3, 4, 5, 6, 7);

        // 현재 시간
        LocalTime now = LocalTime.now();
        int nowHour = now.getHour();

        return list.contains(nowHour);
    }

    /*****************************************************
     * 한명이라도 차단 한/된 상태인지 체크
     ****************************************************/
    public boolean bChkBlock(String memberUuid, String blockUuid) {
        // 차단 관련 데이터 set
        BlockMemberDto blockMemberDto = new BlockMemberDto();
        blockMemberDto.setMemberUuid(memberUuid);
        blockMemberDto.setBlockUuid(blockUuid);
        Integer check1 = blockMemberDaoSub.getBlockByUuid(blockMemberDto);

        blockMemberDto.setMemberUuid(blockUuid);
        blockMemberDto.setBlockUuid(memberUuid);
        Integer check2 = blockMemberDaoSub.getBlockByUuid(blockMemberDto);

        boolean result = false;
        if (check1 > 0 || check2 > 0) {
            result = true;
        }
        return result;
    }

    /*****************************************************
     * JWT Token
     ****************************************************/
    /**
     * 토큰으로 어드민 정보 가져오기
     *
     * @param token
     * @return adminId
     */
    public String getAdminIdByToken(String token) {

        String adminInfo = adminCurlService.getAdminIdByToken(token);
        JSONObject adminInfoObject = new JSONObject(adminInfo);

        if (!((boolean) adminInfoObject.get("result"))) {
            throw new CurlException(adminInfoObject);
        }

        JSONObject adminInfoResult = (JSONObject) adminInfoObject.get("data");

        return adminInfoResult.getString("adminId");
    }

    /**
     * 토큰 상태값 체크
     *
     * @param token
     * @return
     */
    public void getTokenValidation(String token, Integer menuIdx) {

        String jsonString = tokenCurlService.getTokenValidation(token, menuIdx);
        JSONObject tokenValidateObject = new JSONObject(jsonString);

        if (!(tokenValidateObject.getBoolean("result"))) {
            throw new CurlException(tokenValidateObject);
        }

    }

}
