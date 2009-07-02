package org.apache.wink.test.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;


public class TestUtils {

    public static String packageToPath(String packageName) {
        return packageName.replace(".", File.separator);
    }

    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
    }

    /**
     * appends package to the name and returns it as stream.
     * 
     * @param name
     * @param cls
     *            TODO
     * @return
     */
    public static InputStream getResourceOfSamePackage(String name, Class<?> cls) {
        String packagePath = packageToPath(cls.getPackage().getName());
        return SpringMockServletInvocationTest.class.getClassLoader().getResourceAsStream(
            packagePath + File.separator + name);
    }

    public static byte[] getResourceOfSamePackageAsBytes(String name, Class<?> cls)
        throws Exception {
        InputStream resource = getResourceOfSamePackage(name, cls);
        byte[] b = new byte[4096];
        int read = resource.read(b);
        byte[] result = new byte[read];
        System.arraycopy(b, 0, result, 0, read);
        return result;
    }

    /**
     * returns xml from file. Xml must be located in the same package as the
     * current class.
     * 
     * @param fileName
     * @param cls
     *            TODO
     * @return
     * @throws Exception
     */
    protected static Document getXML(String fileName, Class<?> cls) throws Exception {
        return createDocumentBuilder().parse(getResourceOfSamePackage(fileName, cls));
    }

    public static Document getXML(byte[] bs) throws Exception {
        return createDocumentBuilder().parse(new ByteArrayInputStream(bs));
    }

    public static String diffIgnoreUpdateWithAttributeQualifier(String expectedFileName,
        byte[] actual, Class<?> cls) throws Exception {
        Document xmlExpected = getXML(expectedFileName, cls);
        Document xmlActual = getXML(actual);
        Diff diff = new DiffIgnoreUpdateWithAttributeQualifier(xmlExpected, xmlActual);
        if (diff.similar()) {
            return null;
        }
        System.err.println("Expected:\r\n" + TestUtils.printPrettyXML(xmlExpected));
        System.err.println("Actual:\r\n" + TestUtils.printPrettyXML(xmlActual));
        return diff.toString();
    }

    public static String printPrettyXML(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
        StringWriter output = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output.toString();
    }

}
