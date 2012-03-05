/*******************************************************************************
 * Copyright (c) 2010 Christoph Kulla
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Christoph Kulla - Initial API and implementation
 *******************************************************************************/
package org.eclipse.xtext.documentation.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.documentation.IEObjectDocumentationProviderExtension;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Christoph Kulla - Initial contribution and API
 */
public class MultiLineCommentDocumentationProvider implements IEObjectDocumentationProvider, IEObjectDocumentationProviderExtension {

	public final static String RULE = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.ruleName";
	public final static String START_TAG = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.startTag";
	public final static String END_TAG = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.endTag";
	public final static String LINE_PREFIX = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.linePrefix";
	public final static String LINE_POSTFIX = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.linePostfix";
	public final static String WHITESPACE = "org.eclipse.xtext.ui.editor.hover.MultiLineDocumentationProvider.whitespace";

	@Inject(optional = true)
	@Named(RULE)
	String ruleName = "ML_COMMENT";

	@Inject(optional = true)
	@Named(START_TAG)
	String startTag = "/\\*\\*?"; // regular expression

	@Inject(optional = true)
	@Named(END_TAG)
	String endTag = "\\*/"; // regular expression

	@Inject(optional = true)
	@Named(LINE_PREFIX)
	String linePrefix = "\\** ?"; // regular expression

	@Inject(optional = true)
	@Named(LINE_POSTFIX)
	String linePostfix = "\\**"; // regular expression

	@Inject(optional = true)
	@Named(WHITESPACE)
	String whitespace = "( |\\t)*"; // regular expression
	
	protected String findComment(EObject o) {
		String returnValue = null;
		List<INode> documentationNodes = getDocumentationNodes(o);
		if (!documentationNodes.isEmpty()) {
			return documentationNodes.get(0).getText();
		}
		return returnValue;
	}
	
	/**
	 * Returns the nearest multi line comment node that precedes the given object.
	 * @since 2.3
	 * @return a list with exactly one node or an empty list if the object is undocumented.
	 */
	@NonNull
	public List<INode> getDocumentationNodes(@NonNull EObject object) {
		ICompositeNode node = NodeModelUtils.getNode(object);
		List<INode> result = Collections.emptyList();
		if (node != null) {
			// get the last multi line comment before a non hidden leaf node
			for (INode abstractNode : node.getLeafNodes()) {
				if (abstractNode instanceof ILeafNode && !((ILeafNode) abstractNode).isHidden())
					break;
				if (abstractNode instanceof ILeafNode && abstractNode.getGrammarElement() instanceof TerminalRule
						&& ruleName.equalsIgnoreCase(((TerminalRule) abstractNode.getGrammarElement()).getName())) {
					String comment = ((ILeafNode) abstractNode).getText();
					if (comment.matches("(?s)" + startTag + ".*")) {
						result = Collections.singletonList(abstractNode);
					}
				}
			}
		}
		return result;
	}

	public String getDocumentation(EObject o) {
		String returnValue = findComment(o);
		if (returnValue != null) {
			returnValue = returnValue.replaceAll("\\A" + startTag, "");
			returnValue = returnValue.replaceAll(endTag + "\\z", "");
			returnValue = returnValue.replaceAll("(?m)^"+ whitespace + linePrefix, "");
			returnValue = returnValue.replaceAll("(?m)" + whitespace + linePostfix + whitespace + "$", "");
			return returnValue.trim();
		} else
			return null;
	}
}
