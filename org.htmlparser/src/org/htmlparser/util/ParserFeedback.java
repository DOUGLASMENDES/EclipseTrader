// HTMLParser Library $Name: v1_6_20060319 $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Claude Duguay
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/util/ParserFeedback.java,v $
// $Author: derrickoswald $
// $Date: 2004/01/02 16:24:58 $
// $Revision: 1.30 $
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

/**
 * Interface for providing feedback without forcing the output
 * destination to be predefined. A default implementation is
 * provided to output events to the console but alternate
 * implementations that log, watch for specific messages, etc.
 * are also possible.
 *
 * @see DefaultParserFeedback
 * @see FeedbackManager
**/
public interface ParserFeedback
{
  public void info(String message);
  public void warning(String message);
  public void error(String message, ParserException e);
}

