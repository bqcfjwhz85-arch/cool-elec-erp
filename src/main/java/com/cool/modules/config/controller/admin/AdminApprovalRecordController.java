package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.config.entity.ApprovalRecordEntity;
import com.cool.modules.config.service.ApprovalProcessService;
import com.cool.modules.config.service.ApprovalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.cool.modules.config.entity.table.ApprovalRecordEntityTableDef.APPROVAL_RECORD_ENTITY;

/**
 * 审批记录管理Controller
 */
@Tag(name = "审批记录", description = "审批记录管理")
@CoolRestController(
    value = "/admin/config/approvalRecord",
    api = {"page", "list", "info"}
)
public class AdminApprovalRecordController extends BaseController<ApprovalRecordService, ApprovalRecordEntity> {
    
    @Autowired
    private ApprovalProcessService approvalProcessService;
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(APPROVAL_RECORD_ENTITY.INSTANCE_TYPE, APPROVAL_RECORD_ENTITY.INSTANCE_ID,
                    APPROVAL_RECORD_ENTITY.STATUS, APPROVAL_RECORD_ENTITY.APPROVER_ID,
                    APPROVAL_RECORD_ENTITY.APPROVAL_RESULT)
            .keyWordLikeFields(APPROVAL_RECORD_ENTITY.INSTANCE_NO, APPROVAL_RECORD_ENTITY.FLOW_NAME,
                              APPROVAL_RECORD_ENTITY.NODE_NAME, APPROVAL_RECORD_ENTITY.APPROVER_NAME)
            .select(
                APPROVAL_RECORD_ENTITY.ID, APPROVAL_RECORD_ENTITY.FLOW_ID, APPROVAL_RECORD_ENTITY.FLOW_NAME,
                APPROVAL_RECORD_ENTITY.NODE_ID, APPROVAL_RECORD_ENTITY.NODE_NAME, APPROVAL_RECORD_ENTITY.NODE_ORDER,
                APPROVAL_RECORD_ENTITY.INSTANCE_ID, APPROVAL_RECORD_ENTITY.INSTANCE_TYPE, APPROVAL_RECORD_ENTITY.INSTANCE_NO,
                APPROVAL_RECORD_ENTITY.APPROVER_ID, APPROVAL_RECORD_ENTITY.APPROVER_NAME,
                APPROVAL_RECORD_ENTITY.APPROVAL_RESULT, APPROVAL_RECORD_ENTITY.APPROVAL_OPINION, APPROVAL_RECORD_ENTITY.APPROVAL_TIME,
                APPROVAL_RECORD_ENTITY.STATUS, APPROVAL_RECORD_ENTITY.APPLICANT_ID, APPROVAL_RECORD_ENTITY.APPLICANT_NAME,
                APPROVAL_RECORD_ENTITY.APPLY_TIME, APPROVAL_RECORD_ENTITY.CREATE_TIME
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(APPROVAL_RECORD_ENTITY.INSTANCE_TYPE, APPROVAL_RECORD_ENTITY.INSTANCE_ID,
                    APPROVAL_RECORD_ENTITY.STATUS)
        );
    }
    
    @Operation(summary = "审批处理")
    @PostMapping("/approve")
    public R<String> approve(@RequestBody JSONObject params) {
        Long recordId = params.getLong("recordId");
        Long approverId = params.getLong("approverId");
        String approverName = params.getStr("approverName");
        Integer result = params.getInt("result");
        String opinion = params.getStr("opinion");
        
        approvalProcessService.approve(recordId, approverId, approverName, result, opinion);
        return R.ok("审批处理成功");
    }
    
    @Operation(summary = "撤回审批")
    @PostMapping("/withdraw")
    public R<String> withdraw(@RequestBody JSONObject params) {
        Integer instanceType = params.getInt("instanceType");
        Long instanceId = params.getLong("instanceId");
        
        approvalProcessService.withdraw(instanceType, instanceId);
        return R.ok("撤回成功");
    }
    
    @Operation(summary = "获取审批历史")
    @GetMapping("/history")
    public R<List<ApprovalRecordEntity>> getHistory(Integer instanceType, Long instanceId) {
        List<ApprovalRecordEntity> records = this.service.getApprovalHistory(instanceType, instanceId);
        return R.ok(records);
    }
    
    @Operation(summary = "获取我的待审批列表")
    @GetMapping("/myPending")
    public R<List<ApprovalRecordEntity>> getMyPending(Long approverId) {
        List<ApprovalRecordEntity> records = this.service.getMyPendingApprovals(approverId);
        return R.ok(records);
    }
    
    @Operation(summary = "获取我的已审批列表")
    @GetMapping("/myApproved")
    public R<List<ApprovalRecordEntity>> getMyApproved(Long approverId) {
        List<ApprovalRecordEntity> records = this.service.getMyApprovedRecords(approverId);
        return R.ok(records);
    }
    
    @Operation(summary = "获取审批进度")
    @GetMapping("/progress")
    public R<List<ApprovalRecordEntity>> getProgress(Integer instanceType, Long instanceId) {
        List<ApprovalRecordEntity> records = approvalProcessService.getApprovalProgress(instanceType, instanceId);
        return R.ok(records);
    }
    
    @Operation(summary = "检查审批权限")
    @GetMapping("/checkPermission")
    public R<Boolean> checkPermission(Long recordId, Long userId) {
        boolean hasPermission = approvalProcessService.checkApprovalPermission(recordId, userId);
        return R.ok(hasPermission);
    }
}

