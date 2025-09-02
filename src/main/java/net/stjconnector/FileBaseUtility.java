package net.stjconnector;

import net.stjconnector.exception.FileOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBaseUtility {
    private static final Logger logger = LoggerFactory.getLogger(FileBaseUtility.class);
    
    /**
     * 读取文件内容为字节数组
     * 使用NIO优化性能
     *
     * @param filePath 文件路径
     * @return 文件字节数组
     * @throws FileOperationException 文件操作异常
     */
    public static byte[] getBytes(String filePath) throws FileOperationException {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("读取文件失败: {}", filePath, e);
            throw new FileOperationException("读取文件失败: " + filePath, e);
        }
    }
    
    /**
     * 将字节数组写入文件
     *
     * @param data     数据
     * @param filePath 文件路径
     * @throws FileOperationException 文件操作异常
     */
    public static void writeBytesToFile(byte[] data, String filePath) throws FileOperationException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(filePath))) {
            os.write(data);
        } catch (IOException e) {
            logger.error("写入文件失败: {}", filePath, e);
            throw new FileOperationException("写入文件失败: " + filePath, e);
        }
    }
}
