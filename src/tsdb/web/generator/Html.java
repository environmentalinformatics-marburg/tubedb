package tsdb.web.generator;

import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Html {
	private static final Logger log = LogManager.getLogger();
	
	public final Document document;
	public final Tag html;
	public final Tag head;
	public final Css css;
	public final Tag body;
	
	public Html() {
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element htmlElement = document.createElement("HTML");
			document.appendChild(htmlElement);
			this.html = new Tag(htmlElement);
			this.head = html.addTag("head");
			this.css = new Css(head.addTag("style"));

			this.body = html.addTag("body");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write(Writer writer) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		} catch (Exception e) {
			log.error(e);
		}
	}
}
