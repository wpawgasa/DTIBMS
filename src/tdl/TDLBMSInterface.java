/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdl;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import tdl.messaging.PPLI;
import tdl.messaging.TDLMessageHandler;

/**
 *
 * @author wpawgasa
 */
public class TDLBMSInterface {
    private InputStream serialInputStream;
    private OutputStream serialOutputStream;
    private String timestamp;
    
    public class ReceiveSerialThread extends Thread {
        private volatile boolean isThreadAlive = true;

        @Override
        public void run() {
            try {
                while (isThreadAlive) {
                    
                }
            } catch(Exception ex) {
                
            }
        }
    }
    
    public class portListener implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                case SerialPortEvent.BI:
                case SerialPortEvent.OE:
                case SerialPortEvent.FE:
                case SerialPortEvent.PE:
                case SerialPortEvent.CD:
                case SerialPortEvent.CTS:
                case SerialPortEvent.DSR:
                case SerialPortEvent.RI:
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;
                case SerialPortEvent.DATA_AVAILABLE:
                    //if (readBuffer.length() <= 0) {
                    StringBuilder readBuffer = new StringBuilder();
                    List<Integer> inputInt = new ArrayList<Integer>();
                    //}
                    int c;
                    int b;
                    int b_idx = 0;

                    boolean msgEnd = false;
                    try {
                        //while ((b = (byte) inputStream.read()) != (byte) 10) {
                        //    if (b != (byte) 13) {
                        do {
                            b = (int) serialInputStream.read();
                            //System.out.println(b);
                            if (b != -1) {
                                //if (b != 13) {  
                                //System.out.println(b);
                                inputInt.add(b);
                                b_idx++;
                                //readBuffer.append(b + ",");
                            } else {
                                if (b_idx >= 2) {
                                    if ((inputInt.get(b_idx - 1) == 10 && inputInt.get(b_idx - 2) == 13) || (inputInt.get(b_idx - 1) == 4 && inputInt.get(b_idx - 2) == 3)) {
                                        msgEnd = true;
                                    }
                                }
                            }
                            //}

                        } while (!msgEnd);
                        //System.out.println(readBuffer.charAt(readBuffer.length() - 2));
                        //if (readBuffer.charAt(readBuffer.length() - 2) == (char) 10) {
                        //readBuffer.substring(0, readBuffer.length());
                        //String scannedInput = readBuffer.toString();
                        //readBuffer = null;
                        //String[] rxBytesStrArray = scannedInput.split(",");
                        //byte[] rxBytes = new byte[rxBytesStrArray.length];
                        byte[] rxBytes = new byte[inputInt.size()];
                        String rxMsg = "";
//                        for (int j = 0; j < rxBytesStrArray.length; j++) {
//                            int byteInt = Integer.parseInt(rxBytesStrArray[j]);
//                            //frameMsg = frameMsg+" "+byteInt;
//                            rxBytes[j] = (byte) byteInt;
//                            rxMsg = rxMsg + (char) byteInt;
//                        }
                        for (int j = 0; j < inputInt.size(); j++) {
                            int byteInt = inputInt.get(j);
                            //frameMsg = frameMsg+" "+byteInt;
                            rxBytes[j] = (byte) byteInt;
                            rxMsg = rxMsg + (char) byteInt;
                        }
                        timestamp = new java.util.Date().toString();
                        System.out.println(timestamp + ": input received:" + rxMsg);
                        //displayArea.append(timestamp + ": input received:" + scannedInput + "\n");
                        System.out.println(TDLMessageHandler.isCmdMode);
                        if (TDLMessageHandler.isCmdMode) {

                            if (TDLMessageHandler.cmdReqStack.size() > 0) {
                                TDLMessageHandler.cmdResStack.add(rxMsg);
                            }
                        } else {
                            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + rxBytes[rxBytes.length - 1]);
                            if ((int) rxBytes[rxBytes.length - 1] == 10) {
//                                if(!tmpStr.equals("")) {
//                                    rxMsg = tmpStr+rxMsg;
//                                }
                                if (rxMsg.substring(0, 6).equalsIgnoreCase("$GPRMC")) {
                                    PPLI ppli = TDLMessageHandler.decodeOwnPosition(rxMsg);
                                    System.out.println("own position: " + ppli.getPosLat() + ", " + ppli.getPosLon());
                                    //ppli.setPosId(ownRadioId);
                                    //ppli.setPosName(ownprofileId);

                                    //ownTrack.add(ppli);
                                    //currentPosition.setText(ppli.getPosLat()+", "+ppli.getPosLon());
                                }
                                tmpStr = "";
                            } //else {
                            // tmpStr = tmpStr+rxMsg;
                            //}
                            if ((int) rxBytes[rxBytes.length - 1] == 4) {
                                if (rxMsg.charAt(0) == (char) 1) {

                                    TDLMessageHandler.deFraming(rxBytes);
                                }
                            }
                        }
                        //}
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
