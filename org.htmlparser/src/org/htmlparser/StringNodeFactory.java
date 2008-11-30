// HTMLParser Library $Name: v1_6_20060319 $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/StringNodeFactory.java,v $
// $Author: derrickoswald $
// $Date: 2005/11/15 02:09:10 $
// $Revision: 1.14 $
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

package org.htmlparser;

import java.io.Serializable;
import org.htmlparser.lexer.Page;

import org.htmlparser.nodeDecorators.DecodingNode;
import org.htmlparser.nodeDecorators.EscapeCharacterRemovingNode;
import org.htmlparser.nodeDecorators.NonBreakingSpaceConvertingNode;

/**
 * @deprecated Use PrototypicalNodeFactory#setTextPrototype(Text)
 * <p>A more efficient implementation of affecting all string nodes, is to replace
 * the Text node prototype in the {@link PrototypicalNodeFactory} with a
 * custom TextNode that performs the required operation.</p>
 * <p>For example, if you were using:
 * <pre>
 * StringNodeFactory factory = new StringNodeFactory();
 * factory.setDecode(true);
 * </pre>
 * to decode all text issued from
 * {@link org.htmlparser.nodes.TextNode#toPlainTextString() Text.toPlainTextString()},
 * you would instead create a subclass of {@link org.htmlparser.nodes.TextNode TextNode}
 * and set it as the prototype for text node generation:
 * <pre>
 * PrototypicalNodeFactory factory = new PrototypicalNodeFactory ();
 * factory.setTextPrototype (new TextNode () {
 *     public String toPlainTextString()
 *     {
 *         return (org.htmlparser.util.Translate.decode (super.toPlainTextString ()));
 *     }
 * });
 * </pre>
 * Similar constructs apply to removing escapes and converting non-breaking
 * spaces, which were the examples previously provided.</p>
 * <p>Using a subclass avoids the wrapping and delegation inherent in the
 * decorator pattern, with subsequent improvements in processing speed
 * and memory usage.</p>
 */
public class StringNodeFactory
    extends
        PrototypicalNodeFactory
    implements
        Serializable
{
    /**
     * Flag to toggle decoding of strings.
     * Decoding occurs via the method, org.htmlparser.util.Translate.decode()
     */
    protected boolean mDecode;


    /**
     * Flag to toggle removal of escape characters, like \n and \t.
     * Escape character removal occurs via the method,
     * org.htmlparser.util.ParserUtils.removeEscapeCharacters()
     */
    protected boolean mRemoveEscapes;

    /**
     * Flag to toggle converting non breaking spaces (from \u00a0 to space " ").
     * If true, this will happen inside StringNode's toPlainTextString.
     */
    protected boolean mConvertNonBreakingSpaces;

    /**
     * Create the default string node factory.
     */
    public StringNodeFactory ()
    {
        mDecode = false;
        mRemoveEscapes = false;
        mConvertNonBreakingSpaces = false;
    }

    //
    // NodeFactory interface override
    //

    /**
     * Create a new string node.
     * @param page The page the node is on.
     * @param start The beginning position of the string.
     * @param end The ending positiong of the string.
     * @return The text node for the page and range given.
     */
    public Text createStringNode (Page page, int start, int end)
    {
        Text ret;
        
        ret = super.createStringNode (page, start, end);
        if (getDecode ())
            ret = new DecodingNode (ret);
        if (getRemoveEscapes ())
            ret = new EscapeCharacterRemovingNode (ret);
        if (getConvertNonBreakingSpaces ())
            ret = new NonBreakingSpaceConvertingNode (ret);

        return (ret);
    }

    /**
     * Set the decoding state.
     * @param decode If <code>true</code>, string nodes decode text using
     * {@link org.htmlparser.util.Translate#decode}.
     * @see #getDecode
     */
    public void setDecode (boolean decode)
    {
        mDecode = decode;
    }

    /**
     * Get the decoding state.
     * @return <code>true</code> if string nodes decode text.
     * @see #setDecode
     */
    public boolean getDecode ()
    {
        return (mDecode);
    }

    /**
     * Set the escape removing state.
     * @param remove If <code>true</code>, string nodes remove escape
     * characters.
     * @see #getRemoveEscapes
     */
    public void setRemoveEscapes (boolean remove)
    {
        mRemoveEscapes = remove;
    }

    /**
     * Get the escape removing state.
     * @return The removing state.
     * @see #setRemoveEscapes
     */
    public boolean getRemoveEscapes ()
    {
        return (mRemoveEscapes);
    }

    /**
     * Set the non-breaking space replacing state.
     * @param convert If <code>true</code>, string nodes replace &semi;nbsp;
     * characters with spaces.
     * @see #getConvertNonBreakingSpaces
     */
    public void setConvertNonBreakingSpaces (boolean convert)
    {
        mConvertNonBreakingSpaces = convert;
    }

    /**
     * Get the non-breaking space replacing state.
     * @return The replacing state.
     * @see #setConvertNonBreakingSpaces
     */
    public boolean getConvertNonBreakingSpaces ()
    {
        return (mConvertNonBreakingSpaces);
    }
}
