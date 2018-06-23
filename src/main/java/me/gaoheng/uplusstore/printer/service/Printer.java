package me.gaoheng.uplusstore.printer.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.gaoheng.uplusstore.printer.service.support.LabelPrinter;
import me.gaoheng.uplusstore.printer.service.support.ReceiptPrinter;
import me.gaoheng.uplusstore.printer.web.controller.PrintController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class Printer {

    private LabelPrinter labelPrinter = new LabelPrinter();
    private ReceiptPrinter receiptPrinter = new ReceiptPrinter();

    public void printLabel(Label label, Integer quantity) {
        log.debug("Start printing {} copies of label {}.", quantity, label);
        List<String> cmds = new ArrayList<>();
        int x = mmToDot(1) + 4;
        int y = mmToDot(1);
        int gap = mmToDot(1) / 2;
        int height = mmToDot(10);
        cmds.add("BARCODE " + (x + 24) + "," + y + ",\"128\"," + height + ",0,0,2,2,\"" + label.getCode() + "\"");
        y = y + height + gap;
        height = 24;
        cmds.add("TEXT " + x + "," + y + ",\"TSS24.BF2\",0,1,1,\"货号：" + label.getCode() + "\"");
        y = y + height + gap;
        List<String> splitName = split("品名：" + label.getName());
        for(String name : splitName) {
            cmds.add("TEXT " + x + "," + y + ",\"TSS24.BF2\",0,1,1,\"" + name + "\"");
            y = y + height + gap;
        }

        String color = "颜色：" + label.getColor();
        String size = "尺码：" + label.getSize();
        cmds.add("TEXT " + x + "," + y + ",\"TSS24.BF2\",0,1,1,\"" + color + "\"");
        cmds.add("TEXT " + mmToDot(25) + "," + y + ",\"TSS24.BF2\",0,1,1,\"￥" + label.getPrice() + "\"");
        y = y + height + gap;
        cmds.add("TEXT " + x + "," + y + ",\"TSS24.BF2\",0,1,1,\"" + size + "\"");

        labelPrinter.print(cmds, quantity);
    }

    private static final int MM_TO_FACTOR = 8;
    public static int mmToDot(int mm) {
        return mm * MM_TO_FACTOR;
    }
    
    private List<String> split(String skuName) {
        List<String> split = new ArrayList<>();
        int size = 12;

        int from = 0;
        while(from < skuName.length()) {
            int to = from + size;
            if(to > skuName.length()) {
                to = skuName.length();
            }
            String sub = StringUtils.substring(skuName, from, to);
            split.add(sub);
            from = to;
        }

        return split;

    }

    public void printReceipt(Receipt receipt) {
        receiptPrinter.print(receipt);
    }

    @Data
    public static class Label implements Serializable {
        private String code;
        private String name;
        private String price;
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

        List<PrintController.Item> items;
    }

}
