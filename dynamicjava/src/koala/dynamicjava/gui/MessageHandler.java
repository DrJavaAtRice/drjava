/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.gui;

/**
 * This interface represents objets that display messages
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/10/28
 */


public interface MessageHandler {
    /**
     * Sets a temporary message to display
     * @param s the message
     */
    void setMessage(String s);

    /**
     * Sets a temporary message to display
     * @param s  the message
     * @param s2 a string to concatenate with the message
     */
    void setMessage(String s, String s2);

    /**
     * Sets the main message to display
     * @param s the message
     */
    void setMainMessage(String s);

    /**
     * Sets the main message to display
     * @param s  the message
     * @param s2 a string to concatenate with the message
     */
    void setMainMessage(String s, String s2);
}
