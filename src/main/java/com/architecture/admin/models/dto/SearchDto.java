package com.architecture.admin.models.dto;

import com.architecture.admin.libraries.PaginationLibray;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 공통 페이징, 검색 Dto
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchDto {
    // 검색, 페이징
    private String date;        // 날짜
    private String nowDate;     // 현재 날짜
    private String searchType;  // 검색 종류
    private String searchWord;  // 검색어
    private PaginationLibray pagination;  // 페이징

    // selectBox 검색
    private String conditionFirst;
    private String conditionSecond;

    private String lang; // 언어

    // default paging
    public SearchDto() {
        this.page = 1;
        // 시작번호
        this.offset = 0;
        // DB 조회 갯수
        this.limit = 10;
        // 한 페이지 리스트 수
        this.recordSize = this.limit;
        // 최대 표시 페이징 갯수
        this.pageSize = 5;
    }

    public int getOffset() {
        return (page -1) * recordSize;
    }

    private int page;
    // 시작위치
    private int offset;
    // 리스트 갯수
    private int limit;
    // 한 페이지 리스트 수
    private int recordSize;
    // 최대 표시 페이징 갯수
    private int pageSize;

}
