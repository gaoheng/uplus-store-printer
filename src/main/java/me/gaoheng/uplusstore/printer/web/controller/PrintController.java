package me.gaoheng.uplusstore.printer.web.controller;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.gaoheng.uplusstore.printer.service.Printer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/print")
public class PrintController {

    @Autowired
    private Printer printer;

    @PostMapping("/sku-label")
    public Result printSKULabel(@RequestBody SKULabelData data) {

        try {
            printer.printLabel(data.getLabel(), data.getQuantity());
        } catch (Throwable t) {
            log.error("Error when printing label.", t);
        }

        return Result.builder()
                .status("SUCCESS")
                .code("SUCCESS")
                .msg("操作成功")
                .build();
    }

    @PostMapping("/receipt")
    public Result printReceipt(@RequestBody Printer.Receipt receipt) {

        try {
            printer.printReceipt(receipt);
        } catch (Throwable t) {
            log.error("Error when printing receipt.", t);
        }

        return Result.builder()
                .status("SUCCESS")
                .code("SUCCESS")
                .msg("操作成功")
                .build();
    }

    @Data
    public static class Item implements Serializable {
        private String skuCode;
        private String skuName;
        private String skuPrice;
        private String quantity;
        private String total;
    }

    @Data
    public static class SKULabelData implements Serializable {
        private Printer.Label label;

        private Integer quantity;
    }

    @Data
    @Builder
    public static class Result implements Serializable {
        private String status;
        private String code;
        private String msg;
    }

}
