package com.easypan.mappers;

import com.easypan.entity.po.FileShare;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:  Mapper
 * @Author: false
 * @Date: 2024/06/27 16:01:13
 */
public interface ShareMapper<T, P> extends BaseMapper {


    /**
     * 根据ShareId更新
     */
    Integer updateByShareId(@Param("bean") T t, @Param("shareId") String shareId);


    /**
     * 根据ShareId删除
     */
    Integer deleteByShareId(@Param("shareId") String shareId);


    /**
     * 根据ShareId获取对象
     */
    FileShare selectByShareId(@Param("shareId") String shareId);

    Integer deleteFileShareBatch(@Param("shareIdArray") String[] shareIdArray, @Param("userId") String userId);

    void updateShareShowCount(@Param("shareId") String shareId);
}