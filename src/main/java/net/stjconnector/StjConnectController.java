package net.stjconnector;

import caecc.iai.basecomponent.utility.security.SecurityLevelEnum;
import caecc.iai.datatranscomponent.DataSenderManager;
import caecc.iai.datatranscomponent.datasend.IFileSender;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Controller
public class StjConnectController {
    private static final String filePath = "settings.xml";

    public static boolean sendToDTS(String filePath, String starterID,
                                    String secure) throws Exception {
        String address = XmlUtil.readElementTextFromFile(filePath, "address");
        //接收端端口
        int port = Integer.parseInt(XmlUtil.readElementTextFromFile(filePath, "port"));
        //客户端标识
        String clientId = XmlUtil.readElementTextFromFile(filePath, "clientId");
        //令牌（必填）
        String token = XmlUtil.readElementTextFromFile(filePath, "token");
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
        String hashCode = XmlUtil.readElementTextFromFile(filePath, "hashCode");
        //扩展参数（非必填）
        String extendConfig = XmlUtil.readElementTextFromFile(filePath, "extendConfig");
        //任务名称（非必填）
        String title = XmlUtil.readElementTextFromFile(filePath, "title");
        //任务备注（非必填）
        String note = XmlUtil.readElementTextFromFile(filePath, "note");
        //dts内部扩展参数（非必填）
        String processExtendConfig = XmlUtil.readElementTextFromFile(filePath, "processExtendConfig");

        //获取传输文件
        File transFile = new File(filePath);
        //fileName和filePaths对应的文件顺序必须一致
        String fileName = transFile.getName();
//        String[] filePaths =
//                {transFile1.getPath(), transFile2.getPath(), transFile3.getPath(), transFile4.getPath()};

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
            e.printStackTrace();
        }
        boolean ok;
        try {
            //中转2.0文件发送
            ok = iFileSender.sendFileToDts(dataId, fileName,
                    FileBaseUtility.getBytes(transFile.getPath()),
                    secureLevel, hashCode, extendConfig, starterID, title,
                    note, mbInfoJson, processExtendConfig);
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String jsonToFile(@RequestBody JSONObject jsonParam,
                             String starterID, String fileName, String secure)
            throws Exception {
        String data = jsonParam.toJSONString();
        String filePath = fileName + ",json";
        TxtWriter writer = new TxtWriter();
        writer.writeDataToFile(data, filePath);
        File file = new File(filePath);
        //引入新的加密方式
        String oPath = fileName + "_O" + ".json";
        UploadUtil.upload(filePath, oPath, filePath);
        if (sendToDTS(oPath, starterID, secure)) {
            file.delete();
            return "发送成功";
        } else {
            file.delete();
            return "发送失败";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/xml", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public String xmlToFile(@RequestBody String xml, String starterID,
                            String fileName, String secure) throws Exception {
        String filePath = fileName + ".xml";
        TxtWriter writer = new TxtWriter();
        writer.writeDataToFile(xml, filePath);
        File file = new File(filePath);
        //引入新的加密方式
        String oPath = fileName + "_O" + ".xml";
        UploadUtil.upload(filePath, oPath, filePath);
        if (sendToDTS(oPath, starterID, secure)) {
            file.delete();
            return "发送成功";
        } else {
            file.delete();
            return "发送失败";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/file", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public String base64ToFile(@RequestBody String base64, String starterID,
                               String fileName, String secure) throws Exception {
        String filePath = fileName;
        File file = null;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            file = new File(filePath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //引入新的加密方式
        String oPath = "O_" + fileName;
        UploadUtil.upload(filePath, oPath, filePath);
        if (sendToDTS(oPath, starterID, secure)) {
            file.delete();
            return "发送成功";
        } else {
            file.delete();
            return "文件发送失败";
        }
    }
}