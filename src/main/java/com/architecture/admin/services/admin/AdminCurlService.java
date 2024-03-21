package com.architecture.admin.services.admin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "adminCurlService", url = "${totalAdmin.domain}/v1/admin")
public interface AdminCurlService {

    /**
     * 회원 uuid 조회
     *
     * @param token access token
     * @return
     */
    @GetMapping(value = "/id")
    String getAdminIdByToken(@RequestHeader(value = "Authorization") String token);

}
