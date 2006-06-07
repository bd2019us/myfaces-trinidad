/*
* Copyright 2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.myfaces.adfinternal.facelets;

import java.io.IOException;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;

import org.apache.myfaces.adfinternal.taglib.listener.ReturnActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.el.LegacyValueBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;

/**
 * @author Matthias Wessendorf
 *
 */
public class ReturnActionListenerTag extends TagHandler
{

  public ReturnActionListenerTag(TagConfig tagConfig) {
    super(tagConfig);
    _value = getAttribute("value");
  }

  public void apply(FaceletContext faceletContext,
          UIComponent parent) throws IOException, FacesException, FaceletException, ELException
  {
    if(ComponentSupport.isNew(parent))
    {
      ValueExpression valueExp = _value.getValueExpression(faceletContext, Object.class);
      ActionSource actionSource = (ActionSource)parent;
      ReturnActionListener listener = new ReturnActionListener();
      listener.setValueBinding(listener.VALUE_KEY, new LegacyValueBinding(valueExp));
      actionSource.addActionListener(listener);
    }
  }

  private final TagAttribute _value;

}