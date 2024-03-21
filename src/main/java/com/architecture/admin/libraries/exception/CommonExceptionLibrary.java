package com.architecture.admin.libraries.exception;

import com.architecture.admin.libraries.ServerLibrary;
import com.architecture.admin.libraries.TelegramLibrary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/*****************************************************
 * 예외 처리 - 기본 제공
 ****************************************************/
@RestControllerAdvice
@ResponseStatus(HttpStatus.OK)
public class CommonExceptionLibrary {
    private final TelegramLibrary telegramLibrary;


    private final ObjectMapper objectMapper;

    // 텔레그램 푸시 알림 true/false
    @Value("${use.exceptionError.telegram}")
    private boolean useExceptionTelegram;

    @Value("${env.server}")
    private String server;  // 서버

    public CommonExceptionLibrary(TelegramLibrary telegramLibrary, ObjectMapper objectMapper) {
        this.telegramLibrary = telegramLibrary;
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity except(Exception e, Model model) {
        model.addAttribute("exception", e);
        return displayError(e.getMessage(), "9500");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity exceptBind(BindException e) {
        final String sMESSAGE = "message";
        final String[] message = new String[1];
        JSONObject obj = new JSONObject();
        obj.put("result", false);

        e.getAllErrors().forEach(objectError -> {
            if (!obj.has(sMESSAGE) || obj.getString(sMESSAGE) == null) {
                message[0] = objectError.getDefaultMessage();
            }
        });

        return displayError(message[0], "9400");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity handle404(NoHandlerFoundException e) {
        return displayError(e.getMessage(), "9404");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity customExceptionHandle(CustomException e) {
        return displayError(e.getCustomError().getMessage(), e.getCustomError().getCode());
    }

    @ExceptionHandler(CurlException.class)
    public ResponseEntity handleCustomException(CurlException ex) throws JsonProcessingException {
        JsonNode jsonResponseNode = objectMapper.readTree(objectMapper.writeValueAsString(ex.getJsonResponse()));

        String code = jsonResponseNode.get("code").asText();
        String message = jsonResponseNode.get("message").asText();

        return displayError(message, code);
    }

    private ResponseEntity displayError(String sMessage, String sCode) {
        JSONObject obj = new JSONObject();
        obj.put("result", false);
        obj.put("code", sCode);
        obj.put("message", sMessage);

        // 텔레그램 푸시알림
        if (useExceptionTelegram) {
            pushException(obj, 200);
        }

        return new ResponseEntity(obj.toString(), HttpStatus.resolve(200));
    }

    // 텔레그램 푸시
    private void pushException(JSONObject obj, int httpCode) {
        HttpServletRequest request = ServerLibrary.getCurrReq();
        String referrer = request.getHeader("Referer");
        String url = request.getRequestURI();
        String query = request.getQueryString();
        String method = request.getMethod();
        String sendMessgae = "=== [PUSH] ===" + "\n"
                + "referrer :: " + referrer + "\n"
                + "server :: " + server + "\n"
                + "url :: " + url + "\n"
                + "query :: " + query + "\n"
                + "method :: " + method + "\n"
                + "httpCode :: " + httpCode + "\n"
                + "JSONObject :: " + obj;

        telegramLibrary.sendMessage(sendMessgae);
    }
}
