// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.ftpserver.ftplet;

public interface FtpReply
{
    public static final int REPLY_110_RESTART_MARKER_REPLY = 110;
    public static final int REPLY_120_SERVICE_READY_IN_NNN_MINUTES = 120;
    public static final int REPLY_125_DATA_CONNECTION_ALREADY_OPEN = 125;
    public static final int REPLY_150_FILE_STATUS_OKAY = 150;
    public static final int REPLY_200_COMMAND_OKAY = 200;
    public static final int REPLY_202_COMMAND_NOT_IMPLEMENTED = 202;
    public static final int REPLY_211_SYSTEM_STATUS_REPLY = 211;
    public static final int REPLY_212_DIRECTORY_STATUS = 212;
    public static final int REPLY_213_FILE_STATUS = 213;
    public static final int REPLY_214_HELP_MESSAGE = 214;
    public static final int REPLY_215_NAME_SYSTEM_TYPE = 215;
    public static final int REPLY_220_SERVICE_READY = 220;
    public static final int REPLY_221_CLOSING_CONTROL_CONNECTION = 221;
    public static final int REPLY_225_DATA_CONNECTION_OPEN_NO_TRANSFER_IN_PROGRESS = 225;
    public static final int REPLY_226_CLOSING_DATA_CONNECTION = 226;
    public static final int REPLY_227_ENTERING_PASSIVE_MODE = 227;
    public static final int REPLY_230_USER_LOGGED_IN = 230;
    public static final int REPLY_250_REQUESTED_FILE_ACTION_OKAY = 250;
    public static final int REPLY_257_PATHNAME_CREATED = 257;
    public static final int REPLY_331_USER_NAME_OKAY_NEED_PASSWORD = 331;
    public static final int REPLY_332_NEED_ACCOUNT_FOR_LOGIN = 332;
    public static final int REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION = 350;
    public static final int REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION = 421;
    public static final int REPLY_425_CANT_OPEN_DATA_CONNECTION = 425;
    public static final int REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED = 426;
    public static final int REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN = 450;
    public static final int REPLY_451_REQUESTED_ACTION_ABORTED = 451;
    public static final int REPLY_452_REQUESTED_ACTION_NOT_TAKEN = 452;
    public static final int REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED = 500;
    public static final int REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS = 501;
    public static final int REPLY_502_COMMAND_NOT_IMPLEMENTED = 502;
    public static final int REPLY_503_BAD_SEQUENCE_OF_COMMANDS = 503;
    public static final int REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER = 504;
    public static final int REPLY_530_NOT_LOGGED_IN = 530;
    public static final int REPLY_532_NEED_ACCOUNT_FOR_STORING_FILES = 532;
    public static final int REPLY_550_REQUESTED_ACTION_NOT_TAKEN = 550;
    public static final int REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN = 551;
    public static final int REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE = 552;
    public static final int REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED = 553;
    
    int getCode();
    
    String getMessage();
    
    long getSentTime();
    
    String toString();
    
    boolean isPositive();
}
