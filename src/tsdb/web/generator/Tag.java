package tsdb.web.generator;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Tag {
	public final Element element;
	public Tag(Element element) {
		this.element = element;
	}
	public Tag addTag(String tagName) {
		Element subElement = element.getOwnerDocument().createElement(tagName);
		element.appendChild(subElement);
		return new Tag(subElement);
	}
	public Tag addTag(String tagName, String text) {
		Tag tag = addTag(tagName);
		tag.element.setTextContent(text);
		return tag;
	}
	public void setAttribute(String attributeName, String attributeValue) {
		element.setAttribute(attributeName, attributeValue);
	}
	public void setClass(String className) {
		element.setAttribute("class", className);
	}
	public void setId(String id) {
		element.setAttribute("id", id);
	}
	public Tag addDiv() {
		return addTag("div");
	}
	public Tag addLink(String target) {
		Tag link = addTag("a");
		link.setAttribute("href", target);
		return link;
	}
	public Tag addLink(String target, String text) {
		Tag link = addLink(target);
		link.element.setTextContent(text);
		return link;
	}
	public Text addText(String text) {
		Text node = element.getOwnerDocument().createTextNode(text);
		element.appendChild(node);
		return node;
	}
	public Tag addButton(String text) {
		Tag button = addTag("button");
		button.element.setTextContent(text);
		return button;
	}
	public void setStyle(String styleText) {
		setAttribute("style", styleText);
	}
}
