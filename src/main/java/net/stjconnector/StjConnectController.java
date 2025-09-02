package net.stjconnector;

import javax.xml.ws.RequestWrapper;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
public class StjConnectController {
    private final String filePath = "settings.xml";
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
        if (secure.equals("5")){
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
                +"<CryptoKey>\n"
                +"    <priKey>128B2FA8BD433C6C068C8D803DEF79792A519A55171B1B650C23661D15897263</priKey>\n"
                +"    <pubKey>0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857</pubKey>\n"
                +"</CryptoKey>";
        String remotePubKey = "0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857";
        //设置密钥对和远程公钥
        DataSenderManager.setSMkeys(localKeyPair, remotePubKey);
        IFileSender iFileSender = DataSenderManager.getFileSender(address,
                port, clientid, token);

        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(FileBaseUtility.getBytes(transFile.getPath()));
            hashCode = new BigInteger(1, md.digest()).toString(16);
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        boolean ok;
        try{
            //中转2.0文件发送
            ok = iFileSender.sendFileToDts(dataId, fileName,
                    FileBaseUtility.getBytes(transFile.getPath()),
                    secureLevel, hashCode, extendConfig, starterID, title,
                    note, mbInfoJson, processExtendConfig);
        }catch(Exception e){
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String jsonToFile(@RequestBody JSONObject jsonParam,
                             String starterID, String filename, String secure)
        throws IOException{
        String data = jsonParam.toJSONString();
        String filePath = filename + ",json";
        TxtWriter writer = new TxtWriter();
    }
}