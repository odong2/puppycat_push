package com.architecture.admin.models.daosub.block;

import com.architecture.admin.models.dto.block.BlockMemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface BlockMemberDaoSub {

    /**
     * 정상적인 차단 내역 체크
     *
     * @param blockMemberDto memberIdx, blockIdx
     * @return count
     */
    Integer getBlockByUuid(BlockMemberDto blockMemberDto);
}
