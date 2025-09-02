package net.stjconnector;

import net.stjconnector.exception.FileOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TxtWriter {
    private static final Logger logger = LoggerFactory.getLogger(TxtWriter.class);
    
    /**
     * 将数据写入文件
     *
     * @param data     数据
     * @param filePath 文件路径
     * @throws FileOperationException 文件操作异常
     */
    public void writeDataToFile(String data, String filePath) throws FileOperationException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(data);
        } catch (IOException e) {
            logger.error("写入文件失败: {}", filePath, e);
            throw new FileOperationException("写入文件失败: " + filePath, e);
        }
    }
}