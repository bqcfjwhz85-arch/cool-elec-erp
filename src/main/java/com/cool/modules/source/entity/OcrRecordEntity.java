package com.cool.modules.source.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * OCR识别记录实体
 */
@Getter
@Setter
@Table(value = "erp_ocr_record", comment = "OCR识别记录表")
@Schema(description = "OCR识别记录")
public class OcrRecordEntity extends BaseEntity<OcrRecordEntity> {
    
    @Id(keyType = KeyType.Auto)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "关联业务ID（报备单ID）")
    private Long businessId;
    
    @Schema(description = "业务类型：SOURCE-报备单")
    private String businessType;
    
    @Schema(description = "原始图片URL")
    private String imageUrl;
    
    @Schema(description = "识别引擎：BAIDU/ALIYUN/TENCENT")
    private String ocrEngine;
    
    @Schema(description = "识别结果JSON")
    private String recognizeResult;
    
    @Schema(description = "识别置信度（0-1）")
    private BigDecimal confidence;
    
    @Schema(description = "识别状态：0-失败 1-成功 2-部分成功")
    private Integer status;
    
    @Schema(description = "错误信息")
    private String errorMessage;
    
    @Schema(description = "识别耗时（毫秒）")
    private Long costTime;
    
    @Schema(description = "备注")
    private String remark;
}
