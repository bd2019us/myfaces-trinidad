/*
 * Copyright  2000-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.adfinternal.ui.data.bind;

import org.apache.myfaces.adfinternal.ui.RenderingContext;
import org.apache.myfaces.adfinternal.ui.data.BoundValue;


/**
 * BoundValue that returns <code>Boolean.TRUE</code> if either passed
 * in BoundValues return <code>Boolean.TRUE</code> and returns
 * <code>Boolean.FALSE</code> otherwise.
 * <p>
 * Short circuiting is supported, so that if the first BoudnValue
 * returns <code>Boolean.TRUE</code>, the value of the second
 * BoundValue will never be requested.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/data/bind/OrBoundValue.java#0 $) $Date: 10-nov-2005.18:56:41 $
 * @author The Oracle ADF Faces Team
 */
public class OrBoundValue implements BoundValue
{
  /**
   * Creates an OrBoundValue.  Null parameters are treated as if their
   * value returns <code>Boolean.FALSE</code>.
   * <p>
   * @param value1 first BoundValue to OR the result of
   * @param value2 second BoundValue to Or the result of.
   */
  public OrBoundValue(
    BoundValue value1,
    BoundValue value2
    )
  {
    this(new BoundValue[] { value1, value2 });
  }

  /**
   * Creates a OrBoundValue.  Null array elements are treated as if their
   * value returns <code>Boolean.FALSE</code>.
   * <p>
   * @param values array of BoundValues to OR the result of
   */
  public OrBoundValue(
    BoundValue[]  values)
  {
    _values = values;
  }

  public Object getValue(
    RenderingContext context
    )
  {
    if (_values == null || _values.length == 0)
      return Boolean.FALSE;

    for (int i=0; i < _values.length; i++)
    {
      // value is true iff it is not null and bound to Boolean.TRUE.
      boolean value = (_values[i] != null &&
                       Boolean.TRUE.equals(_values[i].getValue(context)));

      // short circuit on first TRUE value
      if (value)
        return Boolean.TRUE;
    }

    return Boolean.FALSE;
  }

  private BoundValue[] _values;
}

