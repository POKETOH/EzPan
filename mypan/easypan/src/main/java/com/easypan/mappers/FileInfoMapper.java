package com.easypan.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easypan.entity.po.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: Mapper
 * @Author: false
 * @Date: 2024/06/18 11:25:12
 */
public interface FileInfoMapper extends BaseMapper<FileInfo> {



    void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
                                @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList,
                                @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);
    void delFileBatch(@Param("userId")String userId,
                      @Param("filePidList")List<String> filePidList,
                      @Param("fileIdList")List<String> fileIdList,
                      @Param("oldDelFlag")Integer oldDelFlag);
    Long selectUseSpace(@Param("userId") String userId);

    List<FileInfo> findFolderInfo(String userId, Integer folderType, String[] pathArrary, String fileId);
}