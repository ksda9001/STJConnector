package net.stjconnector;

import caecc.iai.basecomponent.utility.security.SecurityLevelEnum;
import caecc.iai.datatranscomponent.DataSenderManager;
import caecc.iai.datatranscomponent.datasend.IFileSender;
import com.alibaba.fastjson2.JSONObject;
import net.stjconnector.exception.DataTransferException;
import net.stjconnector.exception.FileOperationException;
import net.stjconnector.exception.StjException;
import net.stjconnector.exception.XmlProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 控制器类，用于处理文件上传和发送到DTS的操作。
 */
@Controller
public class StjConnectController {
    private static final Logger logger = LoggerFactory.getLogger(StjConnectController.class);
    private static final String settingFilePath = "settings.xml";

    /**
     * 发送文件到DTS
     *
     * @param filePath  文件路径
     * @param starterID 发起者ID
     * @param secure    安全级别
     * @return 是否发送成功
     * @throws StjException STJ异常
     */
    public static boolean sendToDTS(String filePath, String starterID,
                                    String secure) throws StjException {
        try {
            String address = XmlUtil.readElementTextFromFile(settingFilePath, "address");
            //接收端端口
            int port = Integer.parseInt(XmlUtil.readElementTextFromFile(settingFilePath, "port"));
            //客户端标识
            String clientId = XmlUtil.readElementTextFromFile(settingFilePath, "clientId");
            //令牌（必填）
            String token = XmlUtil.readElementTextFromFile(settingFilePath, "token");
            //任务id（必填），任务唯一标识，不可重复
            String dataId = System.currentTimeMillis() + "";
            //密标信息（非必填）
            String secureLevel = SecurityLevelEnum.Interior.name();
            //标记文件密级等级（必填），对应关系为：公开：None; 内部：Interior; 秘密：Secret; 绝密：TopSecret
            String mbInfoJson = "{\"secrecy\":[5]}";
            if (secure.equals("5")) {
                //公开
                secureLevel = SecurityLevelEnum.None.name();
                mbInfoJson = "{\"secrecy\":[5]}";
            } else if (secure.equals("4")) {
                //内部
                secureLevel = SecurityLevelEnum.Interior.name();
                mbInfoJson = "{\"secrecy\":[4]}";
            }
            //生成hashCode（非必填），根据自身需求生成hashCode，用于完整性校验
            String hashCode = XmlUtil.readElementTextFromFile(settingFilePath, "hashCode");
            //扩展参数（非必填）
            String extendConfig = XmlUtil.readElementTextFromFile(settingFilePath, "extendConfig");
            //任务名称（非必填）
            String title = XmlUtil.readElementTextFromFile(settingFilePath, "title");
            //任务备注（非必填）
            String note = XmlUtil.readElementTextFromFile(settingFilePath, "note");
            //dts内部扩展参数（非必填）
            String processExtendConfig = XmlUtil.readElementTextFromFile(settingFilePath, "processExtendConfig");

            //获取传输文件
            File transFile = new File(filePath);
            //fileName和filePaths对应的文件顺序必须一致
            String fileName = transFile.getName();

            //调用发送接口，发送文件
            String localKeyPair = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<CryptoKey>\n"
                    + "    <priKey>128B2FA8BD433C6C068C8D803DEF79792A519A55171B1B650C23661D15897263</priKey>\n"
                    + "    <pubKey>0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857</pubKey>\n"
                    + "</CryptoKey>";
            String remotePubKey = "0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857";
            //设置密钥对和远程公钥
            DataSenderManager.setSMkeys(localKeyPair, remotePubKey);
            IFileSender iFileSender = DataSenderManager.getFileSender(address,
                    port, clientId, token);

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(FileBaseUtility.getBytes(transFile.getPath()));
                hashCode = new BigInteger(1, md.digest()).toString(16);
            } catch (NoSuchAlgorithmException e) {
                logger.warn("MD5算法不可用", e);
            }
            
            //中转2.0文件发送
            return iFileSender.sendFileToDts(dataId, fileName,
                    FileBaseUtility.getBytes(transFile.getPath()),
                    secureLevel, hashCode, extendConfig, starterID, title,
                    note, mbInfoJson, processExtendConfig);
        } catch (Exception e) {
            logger.error("发送文件到DTS失败", e);
            throw new DataTransferException("发送文件到DTS失败", e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String jsonToFile(@RequestBody JSONObject jsonParam,
                             String starterID, String fileName, String secure) {
        String data = jsonParam.toJSONString();
        String filePath = fileName + ".json";
        String oPath = fileName + "_O" + ".json";
        
        try {
            TxtWriter writer = new TxtWriter();
            writer.writeDataToFile(data, filePath);
            
            boolean success = fileSend(filePath, oPath, starterID, secure);
            
            return success ? "{\"status\":\"success\",\"message\":\"发送成功\"}" : 
                           "{\"status\":\"error\",\"message\":\"发送失败\"}";
        } catch (Exception e) {
            logger.error("处理JSON文件失败", e);
            cleanupFile(filePath);
            cleanupFile(oPath);
            return "{\"status\":\"error\",\"message\":\"处理JSON文件失败: " + e.getMessage() + "\"}";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/xml", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public String xmlToFile(@RequestBody String xml, String starterID,
                            String fileName, String secure) {
        String filePath = fileName + ".xml";
        String oPath = fileName + "_O" + ".xml";
        
        try {
            TxtWriter writer = new TxtWriter();
            writer.writeDataToFile(xml, filePath);
            
            boolean success = fileSend(filePath, oPath, starterID, secure);
            
            return success ? "{\"status\":\"success\",\"message\":\"发送成功\"}" : 
                           "{\"status\":\"error\",\"message\":\"发送失败\"}";
        } catch (Exception e) {
            logger.error("处理XML文件失败", e);
            cleanupFile(filePath);
            cleanupFile(oPath);
            return "{\"status\":\"error\",\"message\":\"处理XML文件失败: " + e.getMessage() + "\"}";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/file", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public String base64ToFile(@RequestBody String base64, String starterID,
                               String fileName, String secure) {
        String filePath = fileName;
        String oPath = "O_" + fileName;
        
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            FileBaseUtility.writeBytesToFile(bytes, filePath);

            boolean success = fileSend(filePath, oPath, starterID, secure);
            
            return success ? "{\"status\":\"success\",\"message\":\"发送成功\"}" : 
                           "{\"status\":\"error\",\"message\":\"文件发送失败\"}";
        } catch (Exception e) {
            logger.error("处理Base64文件失败", e);
            cleanupFile(filePath);
            cleanupFile(oPath);
            return "{\"status\":\"error\",\"message\":\"处理Base64文件失败: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 清理临时文件
     *
     * @param filePath 文件路径
     */
    private void cleanupFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    logger.warn("无法删除临时文件: {}", filePath);
                }
            }
        } catch (Exception e) {
            logger.warn("删除临时文件失败: {}", filePath, e);
        }
    }

    // 文件发送
    private boolean fileSend(String filePath, String oPath, String starterID,
                             String secure) {
        boolean success = false;
        try{
            if(XmlUtil.readElementTextFromFile(settingFilePath, "featureV2").equals("enable")){
                UploadUtil.upload(filePath, oPath, filePath);
                success = sendToDTS(oPath, starterID, secure);
                cleanupFile(filePath);
                cleanupFile(oPath);
            }else if(XmlUtil.readElementTextFromFile(settingFilePath, "featureV2").equals("disable")){
                success = sendToDTS(filePath, starterID, secure);
                cleanupFile(filePath);
            }
        } catch (StjException e) {
            logger.error("文件发送失败！", e);
        }
        return success;
    }
}