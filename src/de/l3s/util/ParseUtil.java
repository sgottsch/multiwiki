package de.l3s.util;

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;

public class ParseUtil {
	
	public static List<Element> getChildren(Element element, String htmlElementName) {
		List<Element> children = new ArrayList<Element>();

		List<Element> childrenOfThis = element.getContent().getChildElements();
		for (Element subElement : element.getAllElements(htmlElementName)) {
			if (childrenOfThis.contains(subElement))
				children.add(subElement);
		}

		return children;
	}

}
