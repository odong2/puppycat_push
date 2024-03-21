package com.architecture.admin.services.token;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tokenCurlService", url = "${totalAdmin.domain}/v1/token")
public interface TokenCurlService {

    /**
     * 토큰 유효성 검사
     *
     * @param token access token
     * @return
     */
    @GetMapping(value = "")
    String getTokenValidation(@RequestHeader(value = "Authorization") String token,
                              @RequestParam(name = "menuIdx", defaultValue = "") Integer menuIdx);

}
