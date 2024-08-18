package com.easypan.service;


import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:  Service
 * @Author: false
 * @Date: 2024/06/18 11:25:12
 */
public interface FileInfoService extends IService<FileInfo> {

//	/**
//     * 根据条件查询列表
//     */
//	List<FileInfo> findListByParam(FileInfoQuery query);

//
//	/**
//	 * 分页查询
//	 */
//	PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query);

//    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);

	@Transactional(rollbackFor = Exception.class)
	UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    FileInfo newFoloder(String filePid, String userId, String fileName);

	FileInfo rename(String userId, String fileId, String fileName);

	List<FileInfo> loadAllFolder(String userId, String filePid, String currentFildIds);

	void changeFileFolder(String userId, String fileIds, String filePid);

	String createDownloadUrl(String userId, String fileId);

	void removeFile2RecycleBatch(String userId, String fileIds);

	void recoverFileBatch(String userId, String fileIds);

	void delFileBatch(String userId, String fileIds, boolean adminOp);

    Long getUserUseSpace(String userId);

	void checkRootFilePid(String rootFilePid, String userId, String fileId);

    void saveShare(String fileId, String shareFileIds, String myFolderId, String shareUserId, String userId);

	PaginationResultVO loadFileList(FileInfoQuery fileInfoQuery);
}
