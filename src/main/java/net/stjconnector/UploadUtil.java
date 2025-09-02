package net.stjconnector;

import com.eetrust.label.LabelOperator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadUtil {
    private static final String filePath = "settings.xml";

    public static void upload(String iPath, String oPath, String fName) {
        InputStream input = null;
        OutputStream output = null;
        try {
            String serverIp = XmlUtil.readElementTextFromFile(filePath, "platFormAddress");
            int serverPort = Integer.parseInt(XmlUtil.readElementTextFromFile(filePath, "platFormPort"));
            LabelOperator labelOperator = new LabelOperator(serverIp, serverPort);
            //设置本地工程编码格式（UTF_8、GBK），默认为UTF_8
            //labelOperator.setCharacter(CharacterEnums.UTF_8);
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
            System.out.println("处理成功");
        } catch (Exception e) {
            System.out.println("处理异常：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
