package com.finexa.fixtester.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for building FIX protocol messages.
 */
public class FIXMessageBuilder {

    // FIX field delimiter (SOH - Start of Header)
    public static final char SOH = '\u0001';

    // Kafka message field delimiter (File Separator)
    public static final char FS = '\u001C';

    // FIX Message Types
    public static final String MSG_TYPE_NEW_ORDER = "D";
    public static final String MSG_TYPE_EXECUTION_REPORT = "8";
    public static final String MSG_TYPE_CANCEL_REQUEST = "F";
    public static final String MSG_TYPE_REPLACE_REQUEST = "G";
    public static final String MSG_TYPE_CANCEL_REJECT = "9";

    // FIX Tags
    public static final int TAG_BEGIN_STRING = 8;
    public static final int TAG_BODY_LENGTH = 9;
    public static final int TAG_MSG_TYPE = 35;
    public static final int TAG_SENDER_COMP_ID = 49;
    public static final int TAG_TARGET_COMP_ID = 56;
    public static final int TAG_MSG_SEQ_NUM = 34;
    public static final int TAG_SENDING_TIME = 52;
    public static final int TAG_CHECKSUM = 10;

    public static final int TAG_CL_ORD_ID = 11;
    public static final int TAG_ORDER_ID = 37;
    public static final int TAG_EXEC_ID = 17;
    public static final int TAG_EXEC_TYPE = 150;
    public static final int TAG_ORD_STATUS = 39;
    public static final int TAG_SYMBOL = 55;
    public static final int TAG_SECURITY_ID = 48;
    public static final int TAG_SECURITY_ID_SOURCE = 22;
    public static final int TAG_SIDE = 54;
    public static final int TAG_ORDER_QTY = 38;
    public static final int TAG_ORD_TYPE = 40;
    public static final int TAG_PRICE = 44;
    public static final int TAG_TIME_IN_FORCE = 59;
    public static final int TAG_LAST_QTY = 32;
    public static final int TAG_LAST_PX = 31;
    public static final int TAG_LEAVES_QTY = 151;
    public static final int TAG_CUM_QTY = 14;
    public static final int TAG_AVG_PX = 6;
    public static final int TAG_TRANSACT_TIME = 60;
    public static final int TAG_EXCHANGE = 207;
    public static final int TAG_SECURITY_TYPE = 167;
    public static final int TAG_ACCOUNT = 1;
    public static final int TAG_TEXT = 58;
    public static final int TAG_ORD_REJ_REASON = 103;
    public static final int TAG_HANDL_INST = 21;
    public static final int TAG_ORIG_CL_ORD_ID = 41;
    public static final int TAG_SENDER_SUB_ID = 50;
    public static final int TAG_MIN_QTY = 138;

    // Order Status values
    public static final char ORD_STATUS_NEW = '0';
    public static final char ORD_STATUS_PARTIAL = '1';
    public static final char ORD_STATUS_FILLED = '2';
    public static final char ORD_STATUS_DONE_FOR_DAY = '3';
    public static final char ORD_STATUS_CANCELED = '4';
    public static final char ORD_STATUS_REPLACED = '5';
    public static final char ORD_STATUS_PENDING_CANCEL = '6';
    public static final char ORD_STATUS_STOPPED = '7';
    public static final char ORD_STATUS_REJECTED = '8';
    public static final char ORD_STATUS_SUSPENDED = '9';
    public static final char ORD_STATUS_PENDING_NEW = 'A';
    public static final char ORD_STATUS_EXPIRED = 'C';

    // Execution Type values
    public static final char EXEC_TYPE_NEW = '0';
    public static final char EXEC_TYPE_PARTIAL = '1';
    public static final char EXEC_TYPE_FILL = '2';
    public static final char EXEC_TYPE_DONE_FOR_DAY = '3';
    public static final char EXEC_TYPE_CANCELED = '4';
    public static final char EXEC_TYPE_REPLACED = '5';
    public static final char EXEC_TYPE_PENDING_CANCEL = '6';
    public static final char EXEC_TYPE_STOPPED = '7';
    public static final char EXEC_TYPE_REJECTED = '8';
    public static final char EXEC_TYPE_SUSPENDED = '9';
    public static final char EXEC_TYPE_PENDING_NEW = 'A';
    public static final char EXEC_TYPE_EXPIRED = 'C';
    public static final char EXEC_TYPE_TRADE = 'F';

    // Side values
    public static final char SIDE_BUY = '1';
    public static final char SIDE_SELL = '2';

    // Order Type values
    public static final char ORD_TYPE_MARKET = '1';
    public static final char ORD_TYPE_LIMIT = '2';
    public static final char ORD_TYPE_STOP = '3';
    public static final char ORD_TYPE_STOP_LIMIT = '4';

    // Time in Force values
    public static final char TIF_DAY = '0';
    public static final char TIF_GTC = '1';
    public static final char TIF_IOC = '3';
    public static final char TIF_FOK = '4';
    public static final char TIF_GTD = '6';

    // Event Types for Kafka wrapper
    public static final int EVENT_TYPE_APPLICATION = 23041;
    public static final int EVENT_TYPE_SESSION_CONNECT = 23210;
    public static final int EVENT_TYPE_SESSION_DISCONNECT = 23211;

    private static final DateTimeFormatter FIX_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    private static final DateTimeFormatter EXEC_ID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final AtomicInteger sequenceCounter = new AtomicInteger(1);
    private static final AtomicInteger execIdCounter = new AtomicInteger(1);

    /**
     * Generates an execution ID in format: yyyyMMddHHmm_counter (e.g., 202601301236_7185031)
     */
    public static String generateExecId() {
        return LocalDateTime.now().format(EXEC_ID_DATE_FORMAT) + "_" + (7185000 + execIdCounter.getAndIncrement());
    }

    private final Map<Integer, String> fields = new LinkedHashMap<>();

    public FIXMessageBuilder() {
        // Default FIX version - FIXT.1.1 for exchange compatibility
        addField(TAG_BEGIN_STRING, "FIXT.1.1");
    }

    public FIXMessageBuilder(String beginString) {
        addField(TAG_BEGIN_STRING, beginString);
    }

    public FIXMessageBuilder addField(int tag, String value) {
        if (value != null && !value.isEmpty()) {
            fields.put(tag, value);
        }
        return this;
    }

    public FIXMessageBuilder addField(int tag, char value) {
        fields.put(tag, String.valueOf(value));
        return this;
    }

    public FIXMessageBuilder addField(int tag, int value) {
        fields.put(tag, String.valueOf(value));
        return this;
    }

    public FIXMessageBuilder addField(int tag, double value) {
        fields.put(tag, String.valueOf(value));
        return this;
    }

    public String build() {
        StringBuilder body = new StringBuilder();

        // Add sending time if not already present
        if (!fields.containsKey(TAG_SENDING_TIME)) {
            fields.put(TAG_SENDING_TIME, LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT));
        }

        // Build body (all fields except BeginString, BodyLength, and Checksum)
        for (Map.Entry<Integer, String> entry : fields.entrySet()) {
            if (entry.getKey() != TAG_BEGIN_STRING && entry.getKey() != TAG_BODY_LENGTH && entry.getKey() != TAG_CHECKSUM) {
                body.append(entry.getKey()).append("=").append(entry.getValue()).append(SOH);
            }
        }

        // Calculate body length
        int bodyLength = body.length();

        // Build header
        StringBuilder header = new StringBuilder();
        header.append(TAG_BEGIN_STRING).append("=").append(fields.get(TAG_BEGIN_STRING)).append(SOH);
        header.append(TAG_BODY_LENGTH).append("=").append(bodyLength).append(SOH);

        // Build complete message without checksum
        String messageWithoutChecksum = header.toString() + body;

        // Calculate checksum
        int checksum = calculateChecksum(messageWithoutChecksum);

        // Build final message with checksum
        return messageWithoutChecksum + TAG_CHECKSUM + "=" + String.format("%03d", checksum) + SOH;
    }

    private int calculateChecksum(String message) {
        int sum = 0;
        for (char c : message.toCharArray()) {
            sum += c;
        }
        return sum % 256;
    }

    /**
     * Wraps a FIX message in the Kafka message format expected by the listener.
     * Format: sessionID | sequenceNo | eventType | eventData
     */
    public static String wrapForKafka(String sessionId, String fixMessage, int eventType) {
//        return sessionId + FS + seqNo + FS + eventType + FS + fixMessage;
        return fixMessage;
    }

    /**
     * Creates an Execution Report (message type 8) for a new order acknowledgment.
     * Format matches exchange: 8=FIXT.1.1|9=xxx|35=8|34=seqNum|49=sender|52=timestamp|56=target|1=account|6=avgPx|11=clOrdId|14=cumQty|17=execId|31=lastPx|32=lastQty|37=orderId|38=orderQty|39=ordStatus|40=ordType|44=price|54=side|55=symbol|60=transactTime|150=execType|151=leavesQty|10=checksum|
     */
    public static String createNewOrderAck(String clOrdId, String orderId, String symbol, char side,
                                           double quantity, double price, String exchange, String sessionId,
                                           String account, int msgSeqNum, String execId,
                                           String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, 0.0)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, 0)
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, 0.0)
                .addField(TAG_LAST_QTY, 0)
                .addField(TAG_ORDER_ID, orderId)
                .addField(TAG_ORDER_QTY, (int) quantity)
                .addField(TAG_ORD_STATUS, ORD_STATUS_NEW)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_PRICE, price)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_NEW)
                .addField(TAG_LEAVES_QTY, (int) quantity);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createNewOrderAck(String clOrdId, String orderId, String symbol, char side,
                                           double quantity, double price, String exchange, String sessionId) {
        return createNewOrderAck(clOrdId, orderId, symbol, side, quantity, price, exchange, sessionId,
                null, 0, null, null, null);
    }

    /**
     * Creates an Execution Report for a partial fill.
     */
    public static String createPartialFill(String clOrdId, String orderId, String symbol, char side,
                                           double orderQty, double fillQty, double fillPrice,
                                           double cumQty, double leavesQty, double avgPrice,
                                           String exchange, String sessionId,
                                           String account, int msgSeqNum, String execId,
                                           String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, avgPrice)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, (int) cumQty)
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, fillPrice)
                .addField(TAG_LAST_QTY, (int) fillQty)
                .addField(TAG_ORDER_ID, orderId)
                .addField(TAG_ORDER_QTY, (int) orderQty)
                .addField(TAG_ORD_STATUS, ORD_STATUS_PARTIAL)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_PRICE, fillPrice)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_PARTIAL)
                .addField(TAG_LEAVES_QTY, (int) leavesQty);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createPartialFill(String clOrdId, String orderId, String symbol, char side,
                                           double orderQty, double fillQty, double fillPrice,
                                           double cumQty, double leavesQty, double avgPrice,
                                           String exchange, String sessionId) {
        return createPartialFill(clOrdId, orderId, symbol, side, orderQty, fillQty, fillPrice,
                cumQty, leavesQty, avgPrice, exchange, sessionId, null, 0, null, null, null);
    }

    /**
     * Creates an Execution Report for a complete fill (OrdStatus=2, ExecType=2).
     */
    /**
     * Creates an Execution Report for a complete fill (OrdStatus=2, ExecType=2).
     *
     * @param fillQty  Tag 32 - Last Qty (quantity filled in this execution)
     * @param cumQty   Tag 14 - Cumulative Qty (total filled so far, equals orderQty on final fill)
     * @param avgPrice Tag 6  - Average Price across all fills
     */
    public static String createFill(String clOrdId, String orderId, String symbol, char side,
                                    double orderQty, double fillQty, double cumQty,
                                    double fillPrice, double avgPrice,
                                    String exchange, String sessionId,
                                    String account, int msgSeqNum, String execId,
                                    String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, avgPrice)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, (int) cumQty)       // Tag 14 - total filled qty
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, fillPrice)
                .addField(TAG_LAST_QTY, (int) fillQty)     // Tag 32 - this execution's qty
                .addField(TAG_ORDER_ID, orderId)
                .addField(TAG_ORDER_QTY, (int) orderQty)   // Tag 38 - original order qty
                .addField(TAG_ORD_STATUS, ORD_STATUS_FILLED)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_PRICE, fillPrice)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_FILL)
                .addField(TAG_LEAVES_QTY, 0);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Overload preserving old 13-arg signature: treats the order as a simple complete fill
     * where fillQty = cumQty = orderQty (backward compatibility).
     */
    public static String createFill(String clOrdId, String orderId, String symbol, char side,
                                    double orderQty, double fillPrice, String exchange, String sessionId,
                                    String account, int msgSeqNum, String execId,
                                    String senderCompId, String targetCompId) {
        return createFill(clOrdId, orderId, symbol, side,
                orderQty, orderQty, orderQty, fillPrice, fillPrice,
                exchange, sessionId, account, msgSeqNum, execId, senderCompId, targetCompId);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createFill(String clOrdId, String orderId, String symbol, char side,
                                    double orderQty, double fillPrice, String exchange, String sessionId) {
        return createFill(clOrdId, orderId, symbol, side, orderQty, fillPrice, exchange, sessionId,
                null, 0, null, null, null);
    }

    /**
     * Creates an Execution Report for a rejected order (OrdStatus=8, ExecType=8).
     */
    public static String createReject(String clOrdId, String orderId, String symbol, char side,
                                      double quantity, String rejectReason, int rejectCode,
                                      String exchange, String sessionId,
                                      String account, int msgSeqNum, String execId,
                                      String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, 0.0)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, 0)
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, 0.0)
                .addField(TAG_LAST_QTY, 0)
                .addField(TAG_ORDER_ID, orderId != null ? orderId : "NONE")
                .addField(TAG_ORDER_QTY, (int) quantity)
                .addField(TAG_ORD_STATUS, ORD_STATUS_REJECTED)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TEXT, rejectReason)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_REJECTED)
                .addField(TAG_LEAVES_QTY, 0)
                .addField(TAG_ORD_REJ_REASON, rejectCode);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createReject(String clOrdId, String orderId, String symbol, char side,
                                      double quantity, String rejectReason, int rejectCode,
                                      String exchange, String sessionId) {
        return createReject(clOrdId, orderId, symbol, side, quantity, rejectReason, rejectCode,
                exchange, sessionId, null, 0, null, null, null);
    }

    /**
     * Creates an Execution Report for a canceled order (OrdStatus=4, ExecType=4).
     */
    public static String createCanceled(String clOrdId, String origClOrdId, String orderId, String symbol,
                                        char side, double orderQty, double cumQty, double leavesQty,
                                        String exchange, String sessionId,
                                        String account, int msgSeqNum, String execId,
                                        String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, 0.0)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, (int) cumQty)
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, 0.0)
                .addField(TAG_LAST_QTY, 0)
                .addField(TAG_ORDER_ID, orderId)
                .addField(TAG_ORDER_QTY, (int) orderQty)
                .addField(TAG_ORD_STATUS, ORD_STATUS_CANCELED)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_ORIG_CL_ORD_ID, origClOrdId)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_CANCELED)
                .addField(TAG_LEAVES_QTY, 0);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createCanceled(String clOrdId, String origClOrdId, String orderId, String symbol,
                                        char side, double orderQty, double cumQty, double leavesQty,
                                        String exchange, String sessionId) {
        return createCanceled(clOrdId, origClOrdId, orderId, symbol, side, orderQty, cumQty, leavesQty,
                exchange, sessionId, null, 0, null, null, null);
    }

    /**
     * Creates an Execution Report for a pending new order (OrdStatus=A, ExecType=A).
     */
    public static String createPendingNew(String clOrdId, String orderId, String symbol, char side,
                                          double quantity, double price, String exchange, String sessionId,
                                          String account, int msgSeqNum, String execId,
                                          String senderCompId, String targetCompId) {
        if (execId == null || execId.isEmpty()) {
            execId = generateExecId();
        }
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        if (senderCompId == null || senderCompId.isEmpty()) {
            senderCompId = exchange;
        }
        if (targetCompId == null || targetCompId.isEmpty()) {
            targetCompId = "OMS";
        }

        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder()
                .addField(TAG_MSG_TYPE, MSG_TYPE_EXECUTION_REPORT)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_AVG_PX, 0.0)
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_CUM_QTY, 0)
                .addField(TAG_EXEC_ID, execId)
                .addField(TAG_LAST_PX, 0.0)
                .addField(TAG_LAST_QTY, 0)
                .addField(TAG_ORDER_ID, orderId)
                .addField(TAG_ORDER_QTY, (int) quantity)
                .addField(TAG_ORD_STATUS, ORD_STATUS_PENDING_NEW)
                .addField(TAG_ORD_TYPE, ORD_TYPE_LIMIT)
                .addField(TAG_PRICE, price)
                .addField(TAG_SIDE, side)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_EXEC_TYPE, EXEC_TYPE_PENDING_NEW)
                .addField(TAG_LEAVES_QTY, (int) quantity);

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Legacy method for backward compatibility.
     */
    public static String createPendingNew(String clOrdId, String orderId, String symbol, char side,
                                          double quantity, double price, String exchange, String sessionId) {
        return createPendingNew(clOrdId, orderId, symbol, side, quantity, price, exchange, sessionId,
                null, 0, null, null, null);
    }

    // ── Bloomberg FIX DMA messages (FIX 4.2) ────────────────────────────

    /**
     * Creates a Bloomberg-style New Order Single (35=D) in FIX 4.2 format.
     * Tags in order: 35,34,49,52,56,50,21,11,1,[41],22,55,48,54,60,44,38,40,207,59
     */
    public static String createDmaNewOrder(String sessionId, int msgSeqNum,
                                           String senderCompId, String targetCompId, String senderSubId,
                                           String handlInst, String clOrdId, String account, String origClOrdId,
                                           String symbol, String securityId, String securityIdSource,
                                           int side, double price, int quantity,
                                           String ordType, String exchange, String tif) {
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        FIXMessageBuilder builder = new FIXMessageBuilder("FIX.4.2")
                .addField(TAG_MSG_TYPE, MSG_TYPE_NEW_ORDER)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_SENDER_SUB_ID, senderSubId != null ? senderSubId : "null")
                .addField(TAG_HANDL_INST, handlInst != null ? handlInst : "1")
                .addField(TAG_CL_ORD_ID, clOrdId);

        if (account != null && !account.isEmpty()) {
            builder.addField(TAG_ACCOUNT, account);
        }
        if (origClOrdId != null && !origClOrdId.isEmpty()) {
            builder.addField(TAG_ORIG_CL_ORD_ID, origClOrdId);
        }

        builder.addField(TAG_SECURITY_ID_SOURCE, securityIdSource != null ? securityIdSource : "8")
               .addField(TAG_SYMBOL, symbol)
               .addField(TAG_SECURITY_ID, securityId != null ? securityId : symbol)
               .addField(TAG_SIDE, String.valueOf(side))
               .addField(TAG_TRANSACT_TIME, timestamp)
               .addField(TAG_PRICE, price)
               .addField(TAG_ORDER_QTY, quantity)
               .addField(TAG_ORD_TYPE, ordType != null ? ordType : "2")
               .addField(TAG_EXCHANGE, exchange)
               .addField(TAG_TIME_IN_FORCE, tif != null ? tif : "0");

        return wrapForKafka(sessionId, builder.build(), EVENT_TYPE_APPLICATION);
    }

    /**
     * Creates a Bloomberg-style Order Cancel/Replace Request (35=G) in FIX 4.2 format.
     * Tags in order: 35,34,49,52,56,50,11,1,55,54,41,60,44,138,38,40,207,59
     */
    public static String createDmaAmend(String sessionId, int msgSeqNum,
                                        String senderCompId, String targetCompId, String senderSubId,
                                        String clOrdId, String account, String symbol,
                                        int side, String origClOrdId,
                                        double price, int minQty, int quantity,
                                        String ordType, String exchange, String tif) {
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        String fixMsg = new FIXMessageBuilder("FIX.4.2")
                .addField(TAG_MSG_TYPE, MSG_TYPE_REPLACE_REQUEST)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_SENDER_SUB_ID, senderSubId != null ? senderSubId : "null")
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_SIDE, String.valueOf(side))
                .addField(TAG_ORIG_CL_ORD_ID, origClOrdId)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .addField(TAG_PRICE, price)
                .addField(TAG_MIN_QTY, minQty)
                .addField(TAG_ORDER_QTY, quantity)
                .addField(TAG_ORD_TYPE, ordType != null ? ordType : "2")
                .addField(TAG_EXCHANGE, exchange)
                .addField(TAG_TIME_IN_FORCE, tif != null ? tif : "0")
                .build();

        return wrapForKafka(sessionId, fixMsg, EVENT_TYPE_APPLICATION);
    }

    /**
     * Creates a Bloomberg-style Order Cancel Request (35=F) in FIX 4.2 format.
     * Tags in order: 35,34,49,52,56,50,11,1,55,54,38,41,60
     */
    public static String createDmaCancel(String sessionId, int msgSeqNum,
                                         String senderCompId, String targetCompId, String senderSubId,
                                         String clOrdId, String account, String symbol,
                                         int side, int quantity, String origClOrdId) {
        if (msgSeqNum <= 0) {
            msgSeqNum = sequenceCounter.getAndIncrement();
        }
        String timestamp = LocalDateTime.now().format(FIX_TIMESTAMP_FORMAT);

        String fixMsg = new FIXMessageBuilder("FIX.4.2")
                .addField(TAG_MSG_TYPE, MSG_TYPE_CANCEL_REQUEST)
                .addField(TAG_MSG_SEQ_NUM, msgSeqNum)
                .addField(TAG_SENDER_COMP_ID, senderCompId)
                .addField(TAG_SENDING_TIME, timestamp)
                .addField(TAG_TARGET_COMP_ID, targetCompId)
                .addField(TAG_SENDER_SUB_ID, senderSubId != null ? senderSubId : "null")
                .addField(TAG_CL_ORD_ID, clOrdId)
                .addField(TAG_ACCOUNT, account)
                .addField(TAG_SYMBOL, symbol)
                .addField(TAG_SIDE, String.valueOf(side))
                .addField(TAG_ORDER_QTY, quantity)
                .addField(TAG_ORIG_CL_ORD_ID, origClOrdId)
                .addField(TAG_TRANSACT_TIME, timestamp)
                .build();

        return wrapForKafka(sessionId, fixMsg, EVENT_TYPE_APPLICATION);
    }
}
