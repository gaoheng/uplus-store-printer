package me.gaoheng.uplusstore.printer.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.gaoheng.uplusstore.printer.web.controller.PrintController;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class Printer {

    public void printLabel(Label label, Integer quantity) {
        log.debug("Print {} copies of label {}.", quantity, label);
    }

    public void printRepceipt(Receipt receipt) {
        log.debug("Print receipt {}.", receipt);
    }

    @Data
    public static class Label implements Serializable {
        private String code;
        private String name;
        private BigDecimal price;
        private String color;
        private String size;
    }

    @Data
    public static class Receipt implements Serializable {
        private String id;
        private String quantity;
        private BigDecimal total;
        private BigDecimal discount;
        private BigDecimal paid;
        private Date orderTime;

        List<PrintController.Item> items;
    }

}
