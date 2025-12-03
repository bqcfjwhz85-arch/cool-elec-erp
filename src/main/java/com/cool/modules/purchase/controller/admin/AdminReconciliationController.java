package com.cool.modules.purchase.controller.admin;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.modules.purchase.entity.ReconciliationEntity;
import com.cool.modules.purchase.entity.table.ReconciliationEntityTableDef;
import com.cool.modules.purchase.service.ReconciliationService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.cool.core.request.R.ok;
import static com.cool.modules.purchase.entity.table.ReconciliationEntityTableDef.RECONCILIATION_ENTITY;

/**
 * 对账单管理Controller
 */
@Tag(name = "对账单管理", description = "对账单管理")
@CoolRestController(
    value = "/admin/purchase/reconciliation",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminReconciliationController extends BaseController<ReconciliationService, ReconciliationEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 分页查询配置
        setPageOption(createOp()
            .fieldEq(RECONCILIATION_ENTITY.SUPPLIER_ID, RECONCILIATION_ENTITY.STATUS)
            .keyWordLikeFields(RECONCILIATION_ENTITY.BILL_NO, RECONCILIATION_ENTITY.SUPPLIER_NAME)
            .select(
                RECONCILIATION_ENTITY.ID, RECONCILIATION_ENTITY.BILL_NO,
                RECONCILIATION_ENTITY.SUPPLIER_ID, RECONCILIATION_ENTITY.SUPPLIER_NAME,
                RECONCILIATION_ENTITY.START_DATE, RECONCILIATION_ENTITY.END_DATE,
                RECONCILIATION_ENTITY.ORDER_COUNT, RECONCILIATION_ENTITY.TOTAL_AMOUNT,
                RECONCILIATION_ENTITY.PAID_AMOUNT, RECONCILIATION_ENTITY.UNPAID_AMOUNT,
                RECONCILIATION_ENTITY.STATUS, RECONCILIATION_ENTITY.DIFFERENCE_REASON,
                RECONCILIATION_ENTITY.CREATE_TIME, RECONCILIATION_ENTITY.CREATE_BY
            )
        );
    }
    
    @Operation(summary = "获取对账单详情", description = "获取单个对账单详情信息，包含订单明细")
    @PostMapping("/info")
    public Object info(@RequestBody JSONObject requestParams) {
        Long id = requestParams.getLong("id");
        CoolPreconditions.check(id == null, "ID不能为空");
        
        // 查询对账单基本信息
        ReconciliationEntity reconciliation = service.getById(id);
        CoolPreconditions.check(reconciliation == null, "对账单不存在");
        
        // 查询订单明细（使用服务层方法）
        com.cool.modules.purchase.service.ReconciliationItemService itemService = 
            SpringUtil.getBean(com.cool.modules.purchase.service.ReconciliationItemService.class);
        java.util.List<com.cool.modules.purchase.entity.ReconciliationItemEntity> items = 
            itemService.listByReconciliationId(id);
        
        reconciliation.setItems(items);
        
        return ok(reconciliation);
    }
    
    @Operation(summary = "根据对账单编号获取对账单", description = "根据对账单编号获取对账单详情")
    @GetMapping("/getByBillNo")
    public Object getByBillNo(@Parameter(description = "对账单编号") @RequestParam String billNo) {
        ReconciliationEntity reconciliation = service.getByBillNo(billNo);
        CoolPreconditions.check(reconciliation == null, "对账单不存在");
        return ok(reconciliation);
    }
    
    @Operation(summary = "生成对账单编号", description = "生成新的对账单编号")
    @GetMapping("/generateBillNo")
    public Object generateBillNo() {
        String billNo = service.generateBillNo();
        return ok(billNo);
    }
    
    @Operation(summary = "生成对账单", description = "根据筛选条件生成对账单")
    @PostMapping("/generateReconciliation")
    public Object generateReconciliation(@RequestBody JSONObject params) {
        ReconciliationEntity reconciliation = service.generateReconciliation(params);
        return ok(reconciliation);
    }
    
    @Operation(summary = "导出对账单", description = "导出对账单为Excel文件")
    @GetMapping("/exportReconciliation")
    public void exportReconciliation(@Parameter(description = "对账单ID") @RequestParam Long reconciliationId,
                                     HttpServletResponse response) throws Exception {
        CoolPreconditions.check(reconciliationId == null, "对账单ID不能为空");
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=reconciliation_" + reconciliationId + ".xlsx");
        
        service.exportReconciliation(reconciliationId, response);
    }
}

