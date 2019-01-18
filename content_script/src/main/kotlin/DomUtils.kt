package com.inspiritious.HoverTranslateWebExtension.content_script

import org.w3c.dom.CaretPosition
import org.w3c.dom.DOMRect

// here we put all extension functions concerning the DOM API

fun DOMRect?.isWithinBoundaries(x : Double, y:Double, delta:Double = 0.0) : Boolean {

    if (this == null) {
        return false;
    }

    // notice: it can happen that heigh of width is zero, e.g. if the paragraph
    // spans the entire window, then we can not do this boundary check properly because values
    // do not match reality
    var withinXBoundaries = if (width>0) left - delta <= x && right + delta >= x else true
    var withinYBoundaries = if (height>0) top - delta <= y && bottom + delta >= y else true

    return withinXBoundaries && withinYBoundaries;
}

fun CaretPosition.isTextNode() = this.offsetNode.nodeType == 3.toShort()