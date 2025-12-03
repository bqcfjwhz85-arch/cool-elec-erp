package com.cool.modules.monitor.controller.admin;

import com.cool.core.annotation.CoolRestController;
import com.cool.core.request.R;
import com.cool.modules.monitor.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Tag(name = "Monitor", description = "Monitor Management")
@CoolRestController(value = "/admin/monitor", api = {"add", "delete", "update", "page", "list", "info"})
public class AdminMonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "Dashboard Data")
    @GetMapping("/dashboard")
    public R dashboard() {
        return R.ok(monitorService.getDashboardData());
    }

    @Operation(summary = "Finance Data")
    @GetMapping("/finance")
    public R finance() {
        return R.ok(monitorService.getFinanceData());
    }

    @Operation(summary = "Approval Data")
    @GetMapping("/approval")
    public R approval() {
        return R.ok(monitorService.getApprovalData());
    }

    @Operation(summary = "Flow Data")
    @GetMapping("/flow")
    public R flow() {
        return R.ok(monitorService.getFlowData());
    }

    @Operation(summary = "Source Data")
    @GetMapping("/source")
    public R source() {
        return R.ok(monitorService.getSourceData());
    }

    @Operation(summary = "Product Data")
    @GetMapping("/product")
    public R product() {
        return R.ok(monitorService.getProductData());
    }

    @Operation(summary = "Log Data")
    @GetMapping("/log")
    public R log() {
        return R.ok(monitorService.getLogData());
    }
}
