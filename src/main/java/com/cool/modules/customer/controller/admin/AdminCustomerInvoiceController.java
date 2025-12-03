package com.cool.modules.customer.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.customer.entity.CustomerInvoiceEntity;
import com.cool.modules.customer.service.CustomerInvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.customer.entity.table.CustomerInvoiceEntityTableDef.CUSTOMER_INVOICE_ENTITY;

/**
 * 客户开票信息管理Controller
 */
@Tag(name = "客户开票信息管理", description = "客户开票信息管理")
@CoolRestController(
    value = "/admin/customer/invoice",
    api = {"add", "delete", "update", "page", "list", "info"}
)
public class AdminCustomerInvoiceController extends BaseController<CustomerInvoiceService, CustomerInvoiceEntity> {
    
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(CUSTOMER_INVOICE_ENTITY.CUSTOMER_ID, CUSTOMER_INVOICE_ENTITY.INVOICE_TYPE, 
                    CUSTOMER_INVOICE_ENTITY.IS_DEFAULT)
            .keyWordLikeFields(CUSTOMER_INVOICE_ENTITY.INVOICE_TITLE, CUSTOMER_INVOICE_ENTITY.TAX_NUMBER, 
                              CUSTOMER_INVOICE_ENTITY.RECIPIENT)
            .select(
                CUSTOMER_INVOICE_ENTITY.ID, CUSTOMER_INVOICE_ENTITY.CUSTOMER_ID, 
                CUSTOMER_INVOICE_ENTITY.INVOICE_TYPE, CUSTOMER_INVOICE_ENTITY.INVOICE_TITLE, 
                CUSTOMER_INVOICE_ENTITY.TAX_NUMBER, CUSTOMER_INVOICE_ENTITY.REGISTERED_ADDRESS, 
                CUSTOMER_INVOICE_ENTITY.REGISTERED_PHONE, CUSTOMER_INVOICE_ENTITY.BANK_NAME, 
                CUSTOMER_INVOICE_ENTITY.BANK_ACCOUNT, CUSTOMER_INVOICE_ENTITY.RECIPIENT, 
                CUSTOMER_INVOICE_ENTITY.RECIPIENT_PHONE, CUSTOMER_INVOICE_ENTITY.RECIPIENT_ADDRESS, 
                CUSTOMER_INVOICE_ENTITY.IS_DEFAULT, CUSTOMER_INVOICE_ENTITY.CREATE_TIME, 
                CUSTOMER_INVOICE_ENTITY.REMARK
            )
        );
        
        setListOption(createOp()
            .fieldEq(CUSTOMER_INVOICE_ENTITY.CUSTOMER_ID)
        );
    }
}

