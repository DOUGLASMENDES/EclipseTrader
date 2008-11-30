// HTMLParser Library $Name: v1_6_20060319 $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/util/SimpleNodeIterator.java,v $
// $Author: derrickoswald $
// $Date: 2004/01/02 16:24:58 $
// $Revision: 1.34 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.util;

import org.htmlparser.Node;

/**
 * The HTMLSimpleEnumeration interface is similar to NodeIterator,
 * except that it does not throw exceptions. This interface is useful
 * when using HTMLVector, to enumerate through its elements in a simple
 * manner, without needing to do class casts for Node.
 * @author Somik Raha
 */
public interface SimpleNodeIterator extends NodeIterator
{
    /**
     * Check if more nodes are available.
     * @return <code>true</code> if a call to <code>nextHTMLNode()</code> will
     * succeed.
     */
    public boolean hasMoreNodes();

    /**
     * Get the next node.
     * @return The next node in the HTML stream, or null if there are no more
     * nodes.
     */
    public Node nextNode();
}
