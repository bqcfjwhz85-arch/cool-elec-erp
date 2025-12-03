package com.cool.modules.config.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.config.entity.ApprovalFlowEntity;
import com.cool.modules.config.service.ApprovalFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.cool.modules.config.entity.table.ApprovalFlowEntityTableDef.APPROVAL_FLOW_ENTITY;

/**
 * 审批流配置管理Controller
 */
@Tag(name = "审批流配置", description = "审批流配置管理")
@CoolRestController(
    value = "/admin/config/approvalFlow",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminApprovalFlowController extends BaseController<ApprovalFlowService, ApprovalFlowEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(APPROVAL_FLOW_ENTITY.FLOW_TYPE, APPROVAL_FLOW_ENTITY.ENABLED)
            .keyWordLikeFields(APPROVAL_FLOW_ENTITY.FLOW_NAME)
            .select(
                APPROVAL_FLOW_ENTITY.ID, APPROVAL_FLOW_ENTITY.FLOW_NAME,
                APPROVAL_FLOW_ENTITY.FLOW_TYPE, APPROVAL_FLOW_ENTITY.TIMEOUT_HOURS,
                APPROVAL_FLOW_ENTITY.ENABLED, APPROVAL_FLOW_ENTITY.REMARK,
                APPROVAL_FLOW_ENTITY.CREATE_TIME, APPROVAL_FLOW_ENTITY.UPDATE_TIME
            )
        );
        
        // 列表查询配置
        setListOption(createOp()
            .fieldEq(APPROVAL_FLOW_ENTITY.FLOW_TYPE, APPROVAL_FLOW_ENTITY.ENABLED)
        );
    }
    
    @Operation(summary = "根据流程类型获取启用的审批流")
    @GetMapping("/getEnabledByType")
    public R<ApprovalFlowEntity> getEnabledByType(Integer flowType) {
        ApprovalFlowEntity flow = this.service.getEnabledFlowByType(flowType);
        return R.ok(flow);
    }
    
    @Operation(summary = "获取审批流详情（包含节点）")
    @GetMapping("/getWithNodes")
    public R<ApprovalFlowEntity> getWithNodes(Long flowId) {
        ApprovalFlowEntity flow = this.service.getFlowWithNodes(flowId);
        return R.ok(flow);
    }
    
    @Operation(summary = "启用/禁用审批流")
    @PostMapping("/updateEnabled")
    public R<String> updateEnabled(@RequestBody JSONObject params) {
        Long flowId = params.getLong("flowId");
        Integer enabled = params.getInt("enabled");
        this.service.updateEnabled(flowId, enabled);
        return R.ok("操作成功");
    }
}

