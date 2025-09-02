package net.stjconnector.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        logger.error("系统异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", "系统内部错误: " + e.getMessage());
        return result;
    }
    
    @ResponseBody
    @ExceptionHandler(StjException.class)
    public Map<String, Object> handleStjException(StjException e) {
        logger.error("STJ异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", e.getMessage());
        return result;
    }
    
    @ResponseBody
    @ExceptionHandler(FileOperationException.class)
    public Map<String, Object> handleFileOperationException(FileOperationException e) {
        logger.error("文件操作异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", "文件操作失败: " + e.getMessage());
        return result;
    }
    
    @ResponseBody
    @ExceptionHandler(XmlProcessingException.class)
    public Map<String, Object> handleXmlProcessingException(XmlProcessingException e) {
        logger.error("XML处理异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", "XML处理失败: " + e.getMessage());
        return result;
    }
    
    @ResponseBody
    @ExceptionHandler(DataTransferException.class)
    public Map<String, Object> handleDataTransferException(DataTransferException e) {
        logger.error("数据传输异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", "数据传输失败: " + e.getMessage());
        return result;
    }
}