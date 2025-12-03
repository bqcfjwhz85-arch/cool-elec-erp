package com.cool.modules.customer.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.request.R;
import com.cool.modules.customer.entity.CustomerEntity;
import com.cool.modules.customer.service.CustomerService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cool.modules.customer.entity.table.CustomerEntityTableDef.CUSTOMER_ENTITY;

/**
 * 客户管理Controller
 */
@Tag(name = "客户管理", description = "客户管理")
@CoolRestController(
    value = "/admin/customer/info",
    api = {"add", "delete", "update", "page", "list", "info"}
)
@RequiredArgsConstructor
public class AdminCustomerController extends BaseController<CustomerService, CustomerEntity> {
    
    private final CustomerService customerService;
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(CUSTOMER_ENTITY.CUSTOMER_TYPE, CUSTOMER_ENTITY.REGION, CUSTOMER_ENTITY.STATUS)
            .keyWordLikeFields(CUSTOMER_ENTITY.CUSTOMER_CODE, CUSTOMER_ENTITY.CUSTOMER_NAME, 
                              CUSTOMER_ENTITY.CONTACT_PERSON, CUSTOMER_ENTITY.CONTACT_PHONE)
            .select(
                CUSTOMER_ENTITY.ID, CUSTOMER_ENTITY.CUSTOMER_CODE, CUSTOMER_ENTITY.CUSTOMER_NAME, 
                CUSTOMER_ENTITY.CONTACT_PERSON, CUSTOMER_ENTITY.CONTACT_PHONE, CUSTOMER_ENTITY.CONTACT_ADDRESS, 
                CUSTOMER_ENTITY.REGION, CUSTOMER_ENTITY.DELIVERY_ADDRESS, CUSTOMER_ENTITY.DELIVERY_PROVINCE,
                CUSTOMER_ENTITY.DELIVERY_CITY, CUSTOMER_ENTITY.DELIVERY_DISTRICT,
                CUSTOMER_ENTITY.CUSTOMER_TYPE, CUSTOMER_ENTITY.STATUS, 
                CUSTOMER_ENTITY.CREATE_TIME, CUSTOMER_ENTITY.UPDATE_TIME, CUSTOMER_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(CUSTOMER_ENTITY.STATUS)
        );
    }
    
    /**
     * 批量导入客户信息
     */
    @Operation(summary = "批量导入客户", description = "通过Excel批量导入客户信息")
    @PostMapping("/import")
    public Object importCustomers(@RequestParam("file") MultipartFile file) throws IOException {
        CoolPreconditions.check(file.isEmpty(), "上传文件不能为空");
        CoolPreconditions.check(!file.getOriginalFilename().endsWith(".xlsx") && 
                               !file.getOriginalFilename().endsWith(".xls"), "仅支持Excel文件格式");
        
        // 读取Excel
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        
        // 设置表头别名
        reader.addHeaderAlias("客户编码", "customerCode");
        reader.addHeaderAlias("客户名称", "customerName");
        reader.addHeaderAlias("联系人", "contactPerson");
        reader.addHeaderAlias("联系电话", "contactPhone");
        reader.addHeaderAlias("联系地址", "contactAddress");
        reader.addHeaderAlias("所属区域", "region");
        reader.addHeaderAlias("客户类型", "customerType");
        reader.addHeaderAlias("状态", "status");
        reader.addHeaderAlias("备注", "remark");
        
        // 读取数据
        List<CustomerEntity> customers = reader.readAll(CustomerEntity.class);
        
        CoolPreconditions.check(CollUtil.isEmpty(customers), "Excel中没有有效数据");
        
        // 导入结果统计
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failList = new ArrayList<>();
        
        for (int i = 0; i < customers.size(); i++) {
            CustomerEntity customer = customers.get(i);
            try {
                // 数据校验
                if (StrUtil.isBlank(customer.getCustomerCode())) {
                    throw new RuntimeException("客户编码不能为空");
                }
                if (StrUtil.isBlank(customer.getCustomerName())) {
                    throw new RuntimeException("客户名称不能为空");
                }
                
                // 检查编码是否已存在
                CustomerEntity exists = customerService.getByCode(customer.getCustomerCode());
                if (exists != null) {
                    throw new RuntimeException("客户编码已存在");
                }
                
                // 设置默认值
                if (customer.getCustomerType() == null) {
                    customer.setCustomerType(1); // 默认企业
                }
                if (customer.getStatus() == null) {
                    customer.setStatus(1); // 默认启用
                }
                
                // 保存客户
                customerService.save(customer);
                successCount++;
                
            } catch (Exception e) {
                failCount++;
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("row", i + 2); // Excel行号（从2开始，第1行是表头）
                failItem.put("customerCode", customer.getCustomerCode());
                failItem.put("customerName", customer.getCustomerName());
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }
        
        // 返回导入结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", customers.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        
        return R.ok(result);
    }
    
    /**
     * 导出客户列表
     */
    @Operation(summary = "导出客户列表", description = "导出客户信息到Excel")
    @GetMapping("/export")
    public void exportCustomers(@RequestParam(required = false) Integer customerType,
                                @RequestParam(required = false) String region,
                                @RequestParam(required = false) Integer status,
                                HttpServletResponse response) throws IOException {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(CUSTOMER_ENTITY.CUSTOMER_TYPE.eq(customerType, customerType != null))
            .and(CUSTOMER_ENTITY.REGION.eq(region, StrUtil.isNotBlank(region)))
            .and(CUSTOMER_ENTITY.STATUS.eq(status, status != null));
        
        // 查询数据
        List<CustomerEntity> customers = customerService.list(queryWrapper);
        
        // 创建Excel写入器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        
        // 设置表头
        writer.addHeaderAlias("customerCode", "客户编码");
        writer.addHeaderAlias("customerName", "客户名称");
        writer.addHeaderAlias("contactPerson", "联系人");
        writer.addHeaderAlias("contactPhone", "联系电话");
        writer.addHeaderAlias("contactAddress", "联系地址");
        writer.addHeaderAlias("region", "所属区域");
        writer.addHeaderAlias("customerType", "客户类型");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("createTime", "创建时间");
        
        // 转换数据
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CustomerEntity customer : customers) {
            Map<String, Object> row = new HashMap<>();
            row.put("customerCode", customer.getCustomerCode());
            row.put("customerName", customer.getCustomerName());
            row.put("contactPerson", customer.getContactPerson());
            row.put("contactPhone", customer.getContactPhone());
            row.put("contactAddress", customer.getContactAddress());
            row.put("region", customer.getRegion());
            row.put("customerType", customer.getCustomerType() == 1 ? "企业" : "个人");
            row.put("status", customer.getStatus() == 1 ? "启用" : "禁用");
            row.put("remark", customer.getRemark());
            row.put("createTime", customer.getCreateTime());
            rows.add(row);
        }
        
        // 写入数据
        writer.write(rows, true);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=customers.xlsx");
        
        // 输出到响应流
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
    
    /**
     * 下载导入模板
     */
    @Operation(summary = "下载导入模板", description = "下载客户导入Excel模板")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 创建Excel写入器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        
        // 设置表头
        writer.addHeaderAlias("customerCode", "客户编码");
        writer.addHeaderAlias("customerName", "客户名称");
        writer.addHeaderAlias("contactPerson", "联系人");
        writer.addHeaderAlias("contactPhone", "联系电话");
        writer.addHeaderAlias("contactAddress", "联系地址");
        writer.addHeaderAlias("region", "所属区域");
        writer.addHeaderAlias("customerType", "客户类型");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("remark", "备注");
        
        // 写入示例数据
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> example = new HashMap<>();
        example.put("customerCode", "CUST001");
        example.put("customerName", "示例客户有限公司");
        example.put("contactPerson", "张三");
        example.put("contactPhone", "13800138000");
        example.put("contactAddress", "北京市朝阳区XXX路XXX号");
        example.put("region", "华北");
        example.put("customerType", "1");
        example.put("status", "1");
        example.put("remark", "客户类型：1-企业 2-个人；状态：1-启用 0-禁用");
        rows.add(example);
        
        writer.write(rows, true);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=customer_import_template.xlsx");
        
        // 输出到响应流
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
}

