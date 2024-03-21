package com.architecture.admin.services.push.admin;

import com.architecture.admin.libraries.S3Library;
import com.architecture.admin.libraries.exception.CustomError;
import com.architecture.admin.libraries.exception.CustomException;
import com.architecture.admin.models.dao.push.AdminPushDao;
import com.architecture.admin.models.daosub.push.AdminPushDaoSub;
import com.architecture.admin.models.dto.push.AdminPushDto;
import com.architecture.admin.services.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

/*****************************************************
 * 푸시 공통 모델러
 ****************************************************/
@Service
@RequiredArgsConstructor
public class AdminPushRegistService extends BaseService {
    private final AdminPushDao adminPushDao;
    private final AdminPushDaoSub adminPushDaoSub;
    private final S3Library s3Library;
    @Value("${cloud.aws.s3.img.url}")
    private String imgDomain;

    /*****************************************************
     *  Modules
     ****************************************************/
    public Long registAdminPush(String token,
                                AdminPushDto adminPushDto) {

        // 토큰 유효성 검사
        super.getTokenValidation(token, 31);
        // 회원 UUID 조회 & 세팅
        String adminId = super.getAdminIdByToken(token);
        adminPushDto.setAdminId(adminId);

        //  타입 idx
        if (adminPushDto.getTypeIdx() == null || adminPushDto.getTypeIdx() < 1) {
            // 타입을 입력해주세요
            throw new CustomException(CustomError.PUSH_TYPE_EMPTY);
        }

        //  컨텐츠 타입
        if (adminPushDto.getContentsType() == null || adminPushDto.getContentsType() < 1) {
            // 컨텐츠 타입을 입력해주세요
            throw new CustomException(CustomError.PUSH_CONTENTS_TYPE_EMPTY);
        }

        //  컨텐츠 IDX
        if (adminPushDto.getContentsIdx() == null || adminPushDto.getContentsIdx() < 1) {
            // 컨텐츠 IDX를 입력해주세요
            throw new CustomException(CustomError.PUSH_CONTENTS_IDX_EMPTY);
        }

        //  푸시 title
        if (adminPushDto.getTitle() == null || adminPushDto.getTitle().equals("")) {
            //  푸시 제목을 입력해주세요
            throw new CustomException(CustomError.PUSH_TITLE_EMPTY);
        }

        //  푸시 body
        if (adminPushDto.getBody() == null || adminPushDto.getBody().equals("")) {
            // 푸시 내용을 입력해주세요
            throw new CustomException(CustomError.PUSH_BODY_EMPTY);
        }
        // 공지글이면 정상적인 공지글인지 체크하기
        if (adminPushDto.getContentsType() == 1) {
            Integer noticeState = getNoticeStateByIdx(adminPushDto.getContentsIdx());
            // 정상 컨텐츠가 아니면
            if (noticeState == null || noticeState != 1) {
                // 공지 IDX를 확인해 주세요
                throw new CustomException(CustomError.PUSH_NOTICE_IDX_ERROR);

            }
        }
        // 컨텐츠 글이면 정상적인 컨텐츠인지 체크
        else if (adminPushDto.getContentsType() == 2) {
            Integer contentsState = getContentsStateByIdx(adminPushDto.getContentsIdx());
            // 정상 컨텐츠가 아니면
            if (contentsState == null || contentsState != 1) {
                // 컨텐츠 IDX를 확인해 주세요
                throw new CustomException(CustomError.PUSH_CONTENTS_IDX_ERROR);
            }
        }
        // 잘못된 접근입니다
        else {
            // 잘못된 컨텐츠 타입 입니다
            throw new CustomException(CustomError.PUSH_CONTENTS_TYPE_ERROR);
        }

        // 작동중인 워커가 있는지 체크
        Boolean workerCheck = getWorkerCheck();
        // 작동중인 워커가 있으면
        if (Boolean.TRUE.equals(workerCheck)) {
            // 기존에 등록 된 푸시가 진행 중입니다
            throw new CustomException(CustomError.PUSH_WORWER_ERROR);
        }

        // 이미지가 있으면
        if (adminPushDto.getUploadFile() != null) {
            List<MultipartFile> uploadFile = adminPushDto.getUploadFile(); // 이미지
            // 업로드할 이미지 유효성 검사
            s3Library.checkUploadFiles(uploadFile);
            // s3에 저장될 path
            String s3Path = "push/admin";
            // s3 upload (원본)
            List<HashMap<String, Object>> uploadResponse = s3Library.uploadFileNew(uploadFile, s3Path);
            // 이미지 url 추출
            String url = uploadResponse.get(0).get("fileUrl").toString();
            // 풀url
            String fullUrl = imgDomain + url;
            // 이미지 url 세팅
            adminPushDto.setImg(fullUrl);
        } else {
            adminPushDto.setImg("");
        }

        Long result = insertAdminPush(adminPushDto);

        if (result > 0) {
            // 기존 푸시 state = 1 로 변경
            changePushState(adminPushDto);
            // 워커 실행
            startWorker();
        }
        return result;
    }

    /*****************************************************
     *  SubFunction - select
     ****************************************************/
    /**
     * 공지 IDX로 state 값 가져오기
     *
     * @param noticeIdx contentsIdx
     * @return state
     */
    public Integer getNoticeStateByIdx(Long noticeIdx) {
        return adminPushDaoSub.getNoticeStateByIdx(noticeIdx);
    }

    /**
     * 컨텐츠 IDX로 state 값 가져오기
     *
     * @param contentsIdx contentsIdx
     * @return state
     */
    public Integer getContentsStateByIdx(Long contentsIdx) {
        return adminPushDaoSub.getContentsStateByIdx(contentsIdx);
    }

    /**
     * state 가 정상인 워커가 있는지 체크
     *
     * @return
     */
    public Boolean getWorkerCheck() {
        Integer iCount = adminPushDaoSub.getWorkerCheck();

        return iCount > 0;
    }
    /*****************************************************
     *  SubFunction - insert
     ****************************************************/
    /**
     * 푸시 등록
     *
     * @param adminPushDto menuIdx title
     * @return insertedIdx
     */
    public Long insertAdminPush(AdminPushDto adminPushDto) {
        adminPushDto.setRegDate(dateLibrary.getDatetime());

        adminPushDao.insertAdminPush(adminPushDto);
        return adminPushDto.getInsertedIdx();
    }

    /*****************************************************
     *  SubFunction - Update
     ****************************************************/
    public void changePushState(AdminPushDto adminPushDto) {
        adminPushDto.setIdx(adminPushDto.getInsertedIdx());

        adminPushDao.updateState(adminPushDto);
    }

    public void startWorker() {
        adminPushDao.updateWorkerState();
    }
    /*****************************************************
     *  SubFunction - Delete
     ****************************************************/
}
