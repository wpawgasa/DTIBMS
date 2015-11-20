/*
 * This class handle TDL message (Formatting/Deformatting, Framing/Deframing).
 * Created by Wichai Pawgasame (ODC3)
 * Date created: 15/06/2014
 * Last modified: 20/05/2015
 */
package tdl.messaging;

import com.google.common.primitives.Longs;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 * @author wichai.p
 */
public class TDLMessageHandler {

    public static boolean hasUnfinishedMsg = false;
    public final static LinkedList<String> txStack = new LinkedList<String>();
    public final static ConcurrentLinkedQueue<TDLMessage> rxStack = 
            new ConcurrentLinkedQueue<TDLMessage>();
    public final static LinkedList<String> cmdReqStack = 
            new LinkedList<String>();
    public final static LinkedList<String> cmdResStack = 
            new LinkedList<String>();
    public static boolean isCmdMode = false;
    public static double messageMaxBytes = 100;
    public static double messageOverheadBytes = 21;

    public static void deFraming(byte[] bytes) {
        int size = bytes.length;
        int startIdx = -1;
        int endIdx = -1;
        int startMsgIdx = -1;
        int endMsgIdx = -1;
//        try {
        String rxMsgT = new String(bytes);
        
        String rcvStr = "Rx Frame: ";
            for (int j = 0; j < bytes.length; j++) {
                rcvStr = rcvStr+" "+(int) bytes[j];
                
            }
            System.out.println(rcvStr);

        for (int i = 0; i < size; i++) {
            if (bytes[i] == (byte) 1) {
                if(startIdx==-1)
                    startIdx = i;
                
            }
            
            if (bytes[i] == (byte) 2) {
                if(startMsgIdx==-1)
                    startMsgIdx = i;

            }
            if (bytes[i] == (byte) 3) {
                if(endMsgIdx==-1&&i>startIdx&&i>startMsgIdx)
                    endMsgIdx = i;

            }
            if (bytes[i] == (byte) 4) {
                if(endIdx==-1&&i>startIdx&&i>startMsgIdx&&
                        i>endMsgIdx&&bytes[i-1]==(byte)3)
                    endIdx = i;

            }
        }

        if (startIdx == -1 || endIdx == -1 || endMsgIdx < startMsgIdx || 
                endIdx < startMsgIdx || endIdx < endMsgIdx || 
                startMsgIdx < startIdx) {
            String err = "Corrupted Message: invalid frame";
            TDLMessage rxMsg = 
              new TDLMessage(null, null, null, null, (byte) 48, err.getBytes());

            rxStack.offer(rxMsg);
            return;
        }
        byte[] msgType = {bytes[startIdx + 1]};
        byte[] fromId = {bytes[startIdx + 2], bytes[startIdx + 3]};
        byte[] toId = {bytes[startIdx + 4], bytes[startIdx + 5]};
        byte[] profileId = {bytes[startIdx + 6], bytes[startIdx + 7], 
            bytes[startIdx + 8], bytes[startIdx + 9]};
        byte[] checksum = Arrays.copyOfRange(bytes, startIdx + 10, startIdx + 18);
        byte[] msg = Arrays.copyOfRange(bytes, startMsgIdx + 1, endMsgIdx);
        
        long newChecksum = CRC32Checksum(msg);
        long receiveChecksum = Longs.fromByteArray(checksum);

        String profileStr = new String(profileId);
        String fromStr = "";
        for (int i = 0; i < fromId.length; i++) {
            int fromBI = (int) fromId[i];
            fromStr = fromStr + String.format("%02x", 
                    Byte.parseByte(Integer.toString(fromBI)));
        }
        if (newChecksum != receiveChecksum) {
            String err = "Corrupted Message: checksum not matched";
            TDLMessage rxMsgObj = new TDLMessage(profileStr, fromStr, 
                    toId.toString(), null, (byte) 48, err.getBytes());
            
            rxStack.offer(rxMsgObj);
            return;
        }
        
        TDLMessage rxMsgObj = new TDLMessage(profileStr, fromStr, 
                toId.toString(), null, msgType[0], msg);
        
            rxStack.offer(rxMsgObj);


    }

    public static byte[] pplitobytes(PPLI position) {
        double[] ppliDouble = new double[5];
        double lat = position.getPosLat();
        double lon = position.getPosLon();
        double speed = position.getSpeed();
        double tc = position.getTrueCourse();
        double mv = position.getMagVariation();
        ppliDouble[0] = lat;
        ppliDouble[1] = lon;
        ppliDouble[2] = speed;
        ppliDouble[3] = tc;
        ppliDouble[4] = mv;

        byte[] ppliDblBytes = TDLMessageHandler.double2Byte(ppliDouble);

        byte[] ppliBytes = new byte[ppliDblBytes.length + 12];

        System.arraycopy(ppliDblBytes, 0, ppliBytes, 0, ppliDblBytes.length);

        System.arraycopy(position.getPosDate().getBytes(), 0, ppliBytes, 
                ppliDblBytes.length, position.getPosDate().getBytes().length);

        System.arraycopy(position.getPosTime().getBytes(), 0, ppliBytes, 
                ppliDblBytes.length + position.getPosDate().getBytes().length, 
                position.getPosTime().getBytes().length);

        return ppliBytes;
    }

    public static PPLI bytestoPPLI(byte[] data) {
        PPLI decodedPPLI = new PPLI();
        byte[] ppliDblBytes = Arrays.copyOfRange(data, 0, 40);
        double[] ppliDouble = TDLMessageHandler.byte2Double(data, false);
        decodedPPLI.setPosLat(ppliDouble[0]);
        decodedPPLI.setPosLon(ppliDouble[1]);
        decodedPPLI.setSpeed(ppliDouble[2]);
        decodedPPLI.setTrueCourse(ppliDouble[3]);
        decodedPPLI.setMagVariation(ppliDouble[4]);
        decodedPPLI.setPosDate(new String(Arrays.copyOfRange(data, 40, 46)));
        decodedPPLI.setPosTime(new String(Arrays.copyOfRange(data, 46, 52)));

        return decodedPPLI;
    }

    public static void constructFrame(TDLMessage message) {
        byte[] start = {(byte) 1};
        byte[] startMsg = {(byte) 2};
        byte[] endMsg = {(byte) 3};
        byte[] end = {(byte) 4};
        //4 bytes profile id
        byte[] profile = message.getProfileId().substring(0, 4).getBytes(); 
        byte[] msgType = {message.getMsgType()}; // 1 byte message type
        byte[] from = hexStringToByteArray(message.getFromId());
        byte[] to = hexStringToByteArray(message.getToId());
        byte[] data = message.getMsg();
        int numBlk = 1;

        if (data.length > TDLMessageHandler.messageMaxBytes) {
            numBlk = (int) (data.length / TDLMessageHandler.messageMaxBytes);
            if (data.length % TDLMessageHandler.messageMaxBytes > 0) {
                numBlk++;
            }

        }
        long checksum = CRC32Checksum(data);

        byte[] checksumBytes = Longs.toByteArray(checksum);
        System.out.println("checksum len = "+checksumBytes.length);
        String checksumStr = "Checksum: ";
            for (int j = 0; j < checksumBytes.length; j++) {
                checksumStr = checksumStr+" "+(int) checksumBytes[j];
                
            }
            System.out.println(checksumStr);
        byte[] frame = null;
        int msgLength = data.length;
        int msgIdx = 0;
        for (int i = 0; i < numBlk; i++) {
            if (msgLength > TDLMessageHandler.messageMaxBytes) {
                msgLength = 
                        (int) (msgLength - TDLMessageHandler.messageMaxBytes);
            }
            int msgBlkLength = (int) TDLMessageHandler.messageMaxBytes;
            if (i == numBlk - 1) {
                msgBlkLength = msgLength;
            }
            msgIdx = (int) (TDLMessageHandler.messageMaxBytes * i);
            frame = new byte[start.length + 1 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength + endMsg.length + end.length];
            System.arraycopy(start, 0, frame, 0, 1);
            System.arraycopy(msgType, 0, frame, 1, 1);
            System.arraycopy(from, 0, frame, 2, from.length);
            System.arraycopy(to, 0, frame, 2 + from.length, to.length);
            System.arraycopy(profile, 0, frame, 2 + from.length + to.length, 
                    profile.length);
            System.arraycopy(checksumBytes, 0, frame, 2 + from.length + 
                    to.length + profile.length, checksumBytes.length);
            System.arraycopy(startMsg, 0, frame, 2 + from.length + to.length + 
                    profile.length + checksumBytes.length, startMsg.length);
            System.arraycopy(
                    Arrays.copyOfRange(data, msgIdx, msgIdx + msgBlkLength), 0,
                    frame, 2 + from.length + to.length + profile.length + 
                            checksumBytes.length + startMsg.length, msgBlkLength);
            System.arraycopy(endMsg, 0, frame, 2 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength, endMsg.length);
            System.arraycopy(end, 0, frame, 2 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength + endMsg.length, end.length);

            String txMsg = null;
            StringBuilder builder = new StringBuilder();

            for (int j = 0; j < frame.length; j++) {
                if (j < frame.length - 1) {
                    builder.append((int) frame[j] + ",");
                } else {
                    builder.append((int) frame[j]);
                }
            }
            txMsg = builder.toString();
            
            String[] txBytesStrArray = txMsg.split(",");
            byte[] txBytes = new byte[txBytesStrArray.length];
            for (int j = 0; j < txBytesStrArray.length; j++) {
                int byteInt = Integer.parseInt(txBytesStrArray[j]);
                txBytes[j] = (byte) byteInt;
            }
            txStack.add(txMsg);
        }
    }

    public static void SimFraming(TDLMessage message) {
        byte[] start = {(byte) 1};
        byte[] startMsg = {(byte) 2};
        byte[] endMsg = {(byte) 3};
        byte[] end = {(byte) 4};
        //4 bytes profile id
        byte[] profile = message.getProfileId().substring(0, 4).getBytes(); 
        byte[] msgType = {message.getMsgType()}; // 1 byte message type
        byte[] from = hexStringToByteArray(message.getFromId());
        byte[] to = hexStringToByteArray(message.getToId());
        byte[] data = message.getMsg();
        int numBlk = 1;

        if (data.length > TDLMessageHandler.messageMaxBytes) {
            numBlk = (int) (data.length / TDLMessageHandler.messageMaxBytes);
            if (data.length % TDLMessageHandler.messageMaxBytes > 0) {
                numBlk++;
            }

        }
        
        long checksum = CRC32Checksum(data);

        byte[] checksumBytes = Longs.toByteArray(checksum);

        byte[] frame = null;
        int msgLength = data.length;
        int msgIdx = 0;
        for (int i = 0; i < numBlk; i++) {
            if (msgLength > TDLMessageHandler.messageMaxBytes) {
                msgLength = 
                        (int) (msgLength - TDLMessageHandler.messageMaxBytes);
            }
            int msgBlkLength = (int) TDLMessageHandler.messageMaxBytes;
            if (i == numBlk - 1) {
                msgBlkLength = msgLength;
            }
            msgIdx = (int) (TDLMessageHandler.messageMaxBytes * i);
            frame = new byte[start.length + 1 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength + endMsg.length + end.length];
            System.arraycopy(start, 0, frame, 0, 1);
            System.arraycopy(msgType, 0, frame, 1, 1);
            System.arraycopy(from, 0, frame, 2, from.length);
            System.arraycopy(to, 0, frame, 2 + from.length, to.length);
            System.arraycopy(profile, 0, frame, 2 + from.length + to.length, 
                    profile.length);
            System.arraycopy(checksumBytes, 0, frame, 2 + from.length + 
                    to.length + profile.length, checksumBytes.length);
            System.arraycopy(startMsg, 0, frame, 2 + from.length + 
                    to.length + profile.length + checksumBytes.length, 
                    startMsg.length);
            System.arraycopy(
                    Arrays.copyOfRange(data, msgIdx, msgIdx + msgBlkLength), 0, 
                    frame, 2 + from.length + to.length + profile.length + 
                    checksumBytes.length + startMsg.length, msgBlkLength);
            System.arraycopy(endMsg, 0, frame, 2 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength, endMsg.length);
            System.arraycopy(end, 0, frame, 2 + from.length + to.length + 
                    profile.length + checksumBytes.length + startMsg.length + 
                    msgBlkLength + endMsg.length, end.length);

            String txMsg = null;
            StringBuilder builder = new StringBuilder();

            for (int j = 0; j < frame.length; j++) {
                if (j < frame.length - 1) {
                    builder.append((int) frame[j] + ",");
                } else {
                    builder.append((int) frame[j]);
                }
            }
            txMsg = builder.toString();

            String[] txBytesStrArray = txMsg.split(",");
            byte[] txBytes = new byte[txBytesStrArray.length];
            //String frameMsg = "Sim Frame content (before): ";
            for (int j = 0; j < txBytesStrArray.length; j++) {
                int byteInt = Integer.parseInt(txBytesStrArray[j]);
                //frameMsg = frameMsg+" "+byteInt;
                txBytes[j] = (byte) byteInt;
            }
            //System.out.println(frameMsg);
            TDLMessageHandler.deFraming(txBytes);

        }
    }

    public static byte[] getBytesFromQueue() {
        String txBytesStr = txStack.removeFirst();
        String[] txBytesStrArray = txBytesStr.split(",");
        byte[] txBytes = new byte[txBytesStrArray.length];
        String frameMsg = "Tx Frame content: ";
            for (int j = 0; j < txBytesStrArray.length; j++) {
                int byteInt = Integer.parseInt(txBytesStrArray[j]);
                frameMsg = frameMsg+" "+byteInt;
                txBytes[j] = (byte) byteInt;
            }
            System.out.println(frameMsg);
        return txBytes;
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s == null || s == "") {
            s = "0000";
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static long CRC32Checksum(byte[] bytes) {
        long checksum = 0;

        Checksum check = new CRC32();
        // update the current checksum with the specified array of bytes
        check.update(bytes, 0, bytes.length);
        // get the current checksum value
        checksum = check.getValue();
        return checksum;

    }

    public static PPLI decodeOwnPosition(String posStr) {
        PPLI pos = new PPLI();
        //pos.posId = "0000";
        String[] posValues = posStr.split(",");
        String posTime = posValues[1];
        String posDate = posValues[9];

        double posLat = degreeToDecimal(posValues[3]);
        double posLon = degreeToDecimal(posValues[5]);
//        double posLat = Double.parseDouble(posValues[3]);
//        double posLon = Double.parseDouble(posValues[5]);
        double posSpeed = 0.0;
        if (!posValues[7].isEmpty()) {
            posSpeed = Double.parseDouble(posValues[7]);
        }
        double posTC = 0.0;
        if (!posValues[8].isEmpty()) {
            posTC = Double.parseDouble(posValues[8]);
        }
        double posMV = 0.0;
        if (!posValues[9].isEmpty()) {
            posMV = Double.parseDouble(posValues[9]);
        }
        DateFormat df = new SimpleDateFormat("MMddyy");
        DateFormat tf = new SimpleDateFormat("HHmmss");

        Date today = Calendar.getInstance().getTime();

        String reportDate = df.format(today);
        String reportTime = tf.format(today);
        pos.setPosDate(reportDate);
        pos.setPosTime(reportTime);
        pos.setPosLat(posLat);
        pos.setPosLon(posLon);
        pos.setTrueCourse(posTC);
        pos.setSpeed(posSpeed);
        pos.setMagVariation(posMV);
        return pos;
    }

    public static double degreeToDecimal(String degreeStr) {
        String posMinStr = 
                degreeStr.substring(degreeStr.length() - 8, degreeStr.length());
        String posDegreeStr = degreeStr.substring(0, degreeStr.length() - 8);
        double posMin = Double.parseDouble(posMinStr);
        double posDegree = Double.parseDouble(posDegreeStr);
        return posDegree + posMin / 60;

    }

    public static final byte[] double2Byte(double[] inData) {
        int j = 0;
        int length = inData.length;
        byte[] outData = new byte[length * 8];
        for (int i = 0; i < length; i++) {
            long data = Double.doubleToLongBits(inData[i]);
            outData[j++] = (byte) (data >>> 56);
            outData[j++] = (byte) (data >>> 48);
            outData[j++] = (byte) (data >>> 40);
            outData[j++] = (byte) (data >>> 32);
            outData[j++] = (byte) (data >>> 24);
            outData[j++] = (byte) (data >>> 16);
            outData[j++] = (byte) (data >>> 8);
            outData[j++] = (byte) (data >>> 0);
        }
        return outData;
    }

    public static final double[] byte2Double(byte[] inData, boolean byteSwap) {
        int j = 0, upper, lower;
        int length = inData.length / 8;
        double[] outData = new double[length];
        if (!byteSwap) {
            for (int i = 0; i < length; i++) {
                j = i * 8;
                upper = (((inData[j] & 0xff) << 24)
                        + ((inData[j + 1] & 0xff) << 16)
                        + ((inData[j + 2] & 0xff) << 8) + 
                        ((inData[j + 3] & 0xff) << 0));
                lower = (((inData[j + 4] & 0xff) << 24)
                        + ((inData[j + 5] & 0xff) << 16)
                        + ((inData[j + 6] & 0xff) << 8) + 
                        ((inData[j + 7] & 0xff) << 0));
                outData[i] = Double.longBitsToDouble((((long) upper) << 32)
                        + (lower & 0xffffffffl));
            }
        } else {
            for (int i = 0; i < length; i++) {
                j = i * 8;
                upper = (((inData[j + 7] & 0xff) << 24)
                        + ((inData[j + 6] & 0xff) << 16)
                        + ((inData[j + 5] & 0xff) << 8) + 
                        ((inData[j + 4] & 0xff) << 0));
                lower = (((inData[j + 3] & 0xff) << 24)
                        + ((inData[j + 2] & 0xff) << 16)
                        + ((inData[j + 1] & 0xff) << 8) + 
                        ((inData[j] & 0xff) << 0));
                outData[i] = Double.longBitsToDouble((((long) upper) << 32)
                        + (lower & 0xffffffffl));
            }
        }

        return outData;
    }
}
