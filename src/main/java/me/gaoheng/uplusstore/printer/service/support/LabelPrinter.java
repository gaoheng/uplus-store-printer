package me.gaoheng.uplusstore.printer.service.support;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.util.Collection;

public class LabelPrinter {

    private void init() {
        System.setProperty("jna.encoding", "GBK");
        String printerName = "Gprinter  GP-3120TU";
        TscLibDll.INSTANCE.openport(printerName);
        TscLibDll.INSTANCE.setup("40", "30", "5", "8", "0", "2", "0");
        TscLibDll.INSTANCE.sendcommand("SET CUTTER BATCH");
        TscLibDll.INSTANCE.clearbuffer();
    }

    private void sendCmds(Collection<String> cmds) {
        cmds.forEach(cmd -> {
            TscLibDll.INSTANCE.sendcommand(cmd);
        });
    }

    public void print(Collection<String> commands, int count) {
        try {
            init();
            sendCmds(commands);
            TscLibDll.INSTANCE.printlabel("1", String.valueOf(count));
        } finally {
            TscLibDll.INSTANCE.closeport();
        }
    }

    public interface TscLibDll extends Library {
        TscLibDll INSTANCE = (TscLibDll) Native.loadLibrary("d:\\TSCLIB", TscLibDll.class);

        int about();

        int openport(String pirnterName);

        int closeport();

        int sendcommand(String printerCommand);

        int setup(String width, String height, String speed, String density, String sensor, String vertical, String offset);

        int downloadpcx(String filename, String image_name);

        int barcode(String x, String y, String type, String height, String readable, String rotation, String narrow, String wide, String code);

        int printerfont(String x, String y, String fonttype, String rotation, String xmul, String ymul, String text);

        int clearbuffer();

        int printlabel(String set, String copy);

        int formfeed();

        int nobackfeed();

        int windowsfont(int x, int y, int fontheight, int rotation, int fontstyle, int fontunderline, String szFaceName, String content);
    }

}
