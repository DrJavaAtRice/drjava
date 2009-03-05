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

package koala.dynamicjava.tree;

/**
 * This class represents the initializer statement nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/27
 */

public abstract class Initializer extends Node {
    /**
     * The block property name
     */
    public final static String BLOCK = "block";

    /**
     * The block
     */
    private BlockStatement block;

    /**
     * Creates a new initializer statement
     * @param block the block
     * @param fn    the filename
     * @param bl    the begin line
     * @param bc    the begin column
     * @param el    the end line
     * @param ec    the end column
     * @exception IllegalArgumentException if block is null
     */
    protected Initializer(BlockStatement block,
			  String fn, int bl, int bc, int el, int ec) {
	super(fn, bl, bc, el, ec);

	if (block == null) throw new IllegalArgumentException("block == null");

	this.block  = block;
    }
    
    /**
     * Gets the block statement
     */
    public BlockStatement getBlock() {
	return block;
    }

    /**
     * Sets the block statement
     * @exception IllegalArgumentException if bs is null
     */
    public void setBlock(BlockStatement bs) {
	if (bs == null) throw new IllegalArgumentException("bs == null");

	firePropertyChange(BLOCK, block, block = bs);
    }
}
