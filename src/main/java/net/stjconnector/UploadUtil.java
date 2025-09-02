package net.stjconnector;

import com.eetrust.label.LabelOperator;
import net.stjconnector.exception.DataTransferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class UploadUtil {
    private static final Logger logger = LoggerFactory.getLogger(UploadUtil.class);
    private static final String filePath = "settings.xml";

    /**
     * 上传文件并处理
     *
     * @param iPath 输入文件路径
     * @param oPath 输出文件路径
     * @param fName 文件名
     * @throws DataTransferException 数据传输异常
     */
    public static void upload(String iPath, String oPath, String fName) throws DataTransferException {
        InputStream input = null;
        OutputStream output = null;
        try {
            String serverIp = XmlUtil.readElementTextFromFile(filePath, "platFormAddress");
            int serverPort = Integer.parseInt(XmlUtil.readElementTextFromFile(filePath, "platFormPort"));
            LabelOperator labelOperator = new LabelOperator(serverIp, serverPort);
            
            //应用系统用户唯一标识
            String userId = XmlUtil.readElementTextFromFile(filePath, "platFormUserId");
            //业务系统服务器文件路径
            input = new FileInputStream(iPath);
            output = new FileOutputStream(oPath);
            //labelXml="<secAttrib><secrecy>3</secrecy></secAttrib>"
            String labelXml = XmlUtil.readElementTextFromFile(filePath, "platFormLabel");
            int state = 1;
            short fileForm = 0;
            labelOperator.uploadHandle(userId, input, output, fName,
                    labelXml, state, fileForm);
            logger.info("文件处理成功: {}", fName);
        } catch (Exception e) {
            logger.error("文件处理异常: {}", fName, e);
            throw new DataTransferException("文件处理失败: " + fName, e);
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
    }
    
    /**
     * 安静地关闭流
     *
     * @param closeable 可关闭对象
     */
    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.warn("关闭流时发生异常", e);
            }
        }
    }
}