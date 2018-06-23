package me.gaoheng.uplusstore.printer.service.support;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import me.gaoheng.uplusstore.printer.service.Printer;
import me.gaoheng.uplusstore.printer.web.controller.PrintController;
import org.apache.commons.lang3.StringUtils;

import javax.usb.*;
import javax.usb.util.UsbUtil;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
public class ReceiptPrinter {

    private UsbInterface usbInterface;
    private UsbPipe pipe;

    public void print(Printer.Receipt receipt) {
        try {
            init();
            open();
            doPrint(receipt);
        } catch (Throwable throwable) {
            log.error("Error when print.", throwable);
        } finally {
            close();
        }
    }

    private void println(String line) {
        try {
            pipe.syncSubmit(new String(line + "\n").getBytes("GBK"));
            log.debug("Println: [].", line);
        } catch (Throwable throwable) {
            log.error("Error when println[" + line + "].", throwable);
            throw new RuntimeException("Error when println.", throwable);
        }
    }

    private void printBarCode(String code) {
        try {
            pipe.syncSubmit(Uitl.set_bar_code_width(2));
            pipe.syncSubmit(Uitl.barcode_height(new Integer(80).byteValue()));
            pipe.syncSubmit(new String(Uitl.print_bar_code128(BarCode.CODE128, code), "UTF-8").getBytes());
        } catch (Throwable throwable) {
            log.error("Error when printBarCode.", throwable);
            throw new RuntimeException("Error when printBarCode.", throwable);
        }
    }

    private void printItems(List<PrintController.Item> items) {
        items.forEach(item -> {
            printItem(item.getSkuName(), item.getSkuCode(), item.getSkuPrice(), item.getQuantity(), item.getTotal());
        });
    }

    private void printItem(String name, String code, String price, String quantity, String cost) {
        println(name);
        println(StringUtils.joinWith("", left(code, 22), right(price, 10), right(quantity, 6), right(cost, 10)));
    }

    private String left(String content, int length) {
        return Strings.padEnd(content, length, " ".charAt(0));
    }

    private String right(String content, int length) {
        return Strings.padStart(content, length, " ".charAt(0));
    }

    private void doPrint(Printer.Receipt receipt) {
        try {
            pipe.syncSubmit(new String(Uitl.justification_center(), "UTF-8").getBytes());
            pipe.syncSubmit(new String(Uitl.set_chinese_super_on(), "UTF-8").getBytes());
            pipe.syncSubmit(new String(Uitl.double_height_width_on(), "UTF-8").getBytes());
            println("优+童装");
            pipe.syncSubmit(new String(Uitl.double_height_width_off(), "UTF-8").getBytes());
            pipe.syncSubmit(new String(Uitl.set_chinese_super_off(), "UTF-8").getBytes());
            pipe.syncSubmit(new String(Uitl.justification_left(), "UTF-8").getBytes());
            println("");
            printBarCode(receipt.getId());
            println("订单号: " + receipt.getId());
            println("商品                        单价  数量      金额");
            println("------------------------------------------------");
            printItems(receipt.getItems());
            println("------------------------------------------------");
            pipe.syncSubmit(new String(Uitl.justification_right(), "UTF-8").getBytes());
            println("共" + receipt.getQuantity() + "件商品, 合计: " + right(receipt.getTotal() + "元", 10));
            println("优惠: " + right(receipt.getDiscount() + "元", 10));
            println("实付: " + right(receipt.getPaid() + "元", 10));
            println("");
            println("");
            println("");
            println("");
            println("");
            pipe.syncSubmit(new String(Uitl.feedpapercut(), "UTF-8").getBytes());
        } catch (UsbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            init(services.getRootUsbHub());
        } catch(Throwable throwable) {
            log.error("Error when init usb printer.", throwable);
            throw new RuntimeException("Error when init usb printer", throwable);
        }
    }
    private void init(UsbDevice device) {
        if(device.isUsbHub()) {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                init(child);
            }
        } else {
            try {
                device.getActiveUsbConfiguration().getUsbInterfaces().forEach(i -> {
                    UsbInterface uif = (UsbInterface)i;
                    byte c = uif.getUsbInterfaceDescriptor().bInterfaceClass();
                    int cInt = UsbUtil.unsignedInt(c);
                    if(cInt == 7) {
                        usbInterface = uif;
                        try {
                            usbInterface.claim();
                        } catch (UsbException e) {
                            log.error("Error when init usb printer.", e);
                            throw new RuntimeException("Error when init usb printer.", e);
                        }
                        List endpoints = uif.getUsbEndpoints();
                        endpoints.forEach(ep -> {
                            UsbEndpoint ue = (UsbEndpoint) ep;
                            if (ue.getDirection() == javax.usb.UsbConst.ENDPOINT_DIRECTION_OUT) {
                                this.pipe = ue.getUsbPipe();
                            }
                        });
                    }
                });
            } catch (Throwable throwable) {
                log.error("Error when init usb printer.", throwable);
                throw new RuntimeException("Error when init usb printer.", throwable);
            }
        }
    }

    private void open() {
        if (this.pipe == null) {
            throw new RuntimeException("Error when open usb printer, pipe is null.");
        }
        try {
            pipe.open();
        } catch (UsbException e) {
            log.error("Error when open usb printer.", e);
            throw new RuntimeException("Error when open usb printer.", e);
        }
    }

    private void close() {
        try {
            pipe.close();
        } catch (UsbException e) {
            log.error("Error when close usb printer.", e);
            throw new RuntimeException("Error when close usb printer.", e);
        }

        try {
            usbInterface.release();
        } catch (UsbException e) {
            log.error("Error when close usb printer.", e);
            throw new RuntimeException("Error when close usb printer.", e);
        }

    }

    public static class BarCode {
        public static final byte UPC_A       = 0;
        public static final byte UPC_E       = 1;
        public static final byte EAN13       = 2;
        public static final byte EAN8        = 3;
        public static final byte CODE39      = 4;
        public static final byte ITF         = 5;
        public static final byte NW7         = 6;
        //public static final byte CODE93      = 72;
        public static final byte CODE128     = 73;
    }
}
