package tutorial691online.visitors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IDocElement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
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
	
	public static Map<String, ITypeBinding> getLocalJavadocExceptions(Javadoc jDoc) {
		if (jDoc == null) {
			return null;
		}
		Map<String, ITypeBinding> localJavadocException = new HashMap<String, ITypeBinding>();
		@SuppressWarnings("unchecked")
		List<TagElement> list = jDoc.tags();
		for (TagElement tagElement : list) {
			if (tagElement.getTagName() == null
					|| !(tagElement.getTagName().equals(TagElement.TAG_THROWS) 
							|| !tagElement.getTagName().equals(TagElement.TAG_EXCEPTION))) {
				continue;
			}
			@SuppressWarnings("unchecked")
			List<IDocElement> docElements = tagElement.fragments();
			for (IDocElement docElement : docElements) {
				if (docElement instanceof SimpleName) {
					SimpleName name = (SimpleName)docElement;
					ITypeBinding binding = name.resolveTypeBinding();
					localJavadocException.put(binding.getQualifiedName(), binding);
				}
			}
		}
		return localJavadocException;
	}
}
