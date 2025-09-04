package net.stjconnector;

import net.stjconnector.exception.XmlProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

/**
 * XML读取工具类
 * 用于从XML文件中读取指定标签的内容
 */
public class XmlUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

    /**
     * 从XML文件中读取指定标签的完整内容（包括所有子标签，但不包括标签本身）
     *
     * @param filePath XML文件路径
     * @param tagName  要读取的标签名称
     * @return 标签内的完整XML内容（不包括标签本身，但包括所有子标签）
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
                Node node = nodeList.item(0);
                return innerNodeToString(node);
            }
            return null;
        } catch (Exception e) {
            logger.error("解析XML文件失败: {} 标签: {}", filePath, tagName, e);
            throw new XmlProcessingException("解析XML文件失败: " + filePath + " 标签: " + tagName, e);
        }
    }
    
    /**
     * 将Node节点的子节点转换为字符串
     *
     * @param node 要转换的节点
     * @return 节点内子节点的XML字符串表示
     * @throws Exception 转换异常
     */
    private static String innerNodeToString(Node node) throws Exception {
        StringBuilder result = new StringBuilder();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                result.append(child.getTextContent());
            } else {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(child), new StreamResult(writer));
                result.append(writer.toString());
            }
        }
        return result.toString();
    }
}