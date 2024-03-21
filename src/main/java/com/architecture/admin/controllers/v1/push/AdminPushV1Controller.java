package com.architecture.admin.controllers.v1.push;


import com.architecture.admin.controllers.v1.BaseController;
import com.architecture.admin.models.dto.push.AdminPushDto;
import com.architecture.admin.services.push.admin.AdminPushRegistService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/push/admin")
public class AdminPushV1Controller extends BaseController {

    private final AdminPushRegistService adminPushRegistService;

    /**
     * 푸시 등록하기
     *
     * @param adminPushDto type contentsType contentsIdx title body img
     * @return 처리결과
     */
    @PostMapping("")
    public ResponseEntity registAdminPush(@RequestHeader(value = "Authorization") String token,
                                          @ModelAttribute AdminPushDto adminPushDto) {

        Long result = adminPushRegistService.registAdminPush(token, adminPushDto);

        String sMessage;
        // 등록 완료
        if (result > 0) {
            sMessage = super.langMessage("lang.push.admin.success.regist");
        }
        // 등록 실패
        else {
            sMessage = super.langMessage("lang.push.admin.exception.registFail");
        }
        // response object
        JSONObject data = new JSONObject(result);

        return displayJson(true, "1000", sMessage, data);
    }
}
