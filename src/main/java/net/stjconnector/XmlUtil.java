package net.stjconnector;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * XML读取工具类
 * 用于从XML文件中读取指定标签的内容
 */
public class XmlUtil {

    /**
     * 从XML文件中读取指定标签的文本内容
     *
     * @param filePath XML文件路径
     * @param tagName  要读取的标签名称
     * @return 标签中的文本内容
     * @throws Exception 解析异常
     */
    public static String readElementTextFromFile(String filePath, String tagName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filePath));
        document.getDocumentElement().normalize();

        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}