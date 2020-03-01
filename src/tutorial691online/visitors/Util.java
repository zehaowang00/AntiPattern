package tutorial691online.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Util {
	public static Set<String> getJavadocExceptions(IMethod iMethod) {
		Set<String> javadocExceptions = new HashSet<String>();
		try {
			String javadocStr = iMethod.getAttachedJavadoc(null);
			if (javadocStr != null) {
				Document javadoc = Jsoup.parse(javadocStr);
				Element throwsLabel = javadoc.selectFirst("dt span:matches([Tt]hrows)");
				Element dd = throwsLabel.parent().nextElementSibling();
				while(dd != null) {
					Element a = dd.selectFirst("code a");
					String cls = a.text();
					String pkg = a.attr("title").split(" ")[2];
					javadocExceptions.add(pkg + "." + cls);
					dd = dd.nextElementSibling();
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return javadocExceptions;
	}
}
