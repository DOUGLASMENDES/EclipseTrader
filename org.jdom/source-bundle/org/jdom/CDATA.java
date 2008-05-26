/*--

 $Id: CDATA.java,v 1.30 2004/02/27 11:32:57 jhunter Exp $

 Copyright (C) 2000-2004 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

package org.jdom;

/**
 * An XML CDATA section. Represents character-based content within an XML
 * document that should be output within special CDATA tags. Semantically it's
 * identical to a simple {@link Text} object, but output behavior is different.
 * CDATA makes no guarantees about the underlying textual representation of
 * character data, but does expose that data as a Java String.
 *
 * @version $Revision: 1.30 $, $Date: 2004/02/27 11:32:57 $
 * @author  Dan Schaffer
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Bradley S. Huffman
 */
public class CDATA extends Text {

    private static final String CVS_ID = 
      "@(#) $RCSfile: CDATA.java,v $ $Revision: 1.30 $ $Date: 2004/02/27 11:32:57 $ $Name: jdom_1_0 $";

    /**
     * This is the protected, no-args constructor standard in all JDOM
     * classes. It allows subclassers to get a raw instance with no
     * initialization.
     */
    protected CDATA() { }

    /**
     * This constructor creates a new <code>CDATA</code> node, with the
     * supplied string value as it's character content.
     *
     * @param str the node's character content.
     * @throws IllegalDataException if <code>str</code> contains an 
     *         illegal character such as a vertical tab (as determined
     *          by {@link org.jdom.Verifier#checkCharacterData})
     *         or the CDATA end delimiter <code>]]&gt;</code>.
     */
    public CDATA(String str) {
        setText(str);
    }

    /**
     * This will set the value of this <code>CDATA</code> node.
     *
     * @param str value for node's content.
     * @return the object on which the method was invoked
     * @throws IllegalDataException if <code>str</code> contains an 
     *         illegal character such as a vertical tab (as determined
     *          by {@link org.jdom.Verifier#checkCharacterData})
     *         or the CDATA end delimiter <code>]]&gt;</code>.
     */
    public Text setText(String str) {
        // Overrides Text.setText() because this needs to check CDATA
        // rules are enforced.  We could have a separate Verifier check
        // for CDATA beyond Text and call that alone before super.setText().

        String reason;

        if (str == null) {
            value = EMPTY_STRING;
            return this;
        }

        if ((reason = Verifier.checkCDATASection(str)) != null) {
            throw new IllegalDataException(str, "CDATA section", reason);
        }
        value = str;
        return this;
    }

    /**
     * This will append character content to whatever content already
     * exists within this <code>CDATA</code> node.
     *
     * @param str character content to append.
     * @throws IllegalDataException if <code>str</code> contains an 
     *         illegal character such as a vertical tab (as determined
     *          by {@link org.jdom.Verifier#checkCharacterData})
     *         or the CDATA end delimiter <code>]]&gt;</code>.
     */
    public void append(String str) {
        // Overrides Text.setText() because this needs to check CDATA
        // rules are enforced.  We could have a separate Verifier check
        // for CDATA beyond Text and call that alone before super.setText().

        String reason;

        if (str == null) {
            return;
        }
        if ((reason = Verifier.checkCDATASection(str)) != null) {
            throw new IllegalDataException(str, "CDATA section", reason);
        }

        if (value == EMPTY_STRING)
             value = str;
        else value += str;
    }

    /**
     * This returns a <code>String</code> representation of the
     * <code>CDATA</code> node, suitable for debugging. If the XML
     * representation of the <code>CDATA</code> node is desired,
     * either <code>{@link #getText}</code> or
     * {@link org.jdom.output.XMLOutputter#output(CDATA, java.io.Writer)}</code>
     * should be used.
     *
     * @return <code>String</code> - information about this node.
     */
    public String toString() {
        return new StringBuffer(64)
            .append("[CDATA: ")
            .append(getText())
            .append("]")
            .toString();
    }
}
