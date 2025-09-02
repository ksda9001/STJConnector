package net.stjconnector;

import net.stjconnector.exception.XmlProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * XML读取工具类
 * 用于从XML文件中读取指定标签的内容
 */
public class XmlUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

    /**
     * 从XML文件中读取指定标签的文本内容
     *
     * @param filePath XML文件路径
     * @param tagName  要读取的标签名称
     * @return 标签中的文本内容
     * @throws XmlProcessingException 解析异常
     */
    public static String readElementTextFromFile(String filePath, String tagName) throws XmlProcessingException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 防止XXE攻击
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
            return null;
        } catch (Exception e) {
            logger.error("解析XML文件失败: {} 标签: {}", filePath, tagName, e);
            throw new XmlProcessingException("解析XML文件失败: " + filePath + " 标签: " + tagName, e);
        }
    }
}