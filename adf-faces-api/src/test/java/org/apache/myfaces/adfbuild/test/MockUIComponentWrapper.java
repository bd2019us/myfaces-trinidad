/*
 * Copyright 2005,2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfbuild.test;

import javax.faces.component.UIComponent;

import org.jmock.Mock;

/**
 * 
 * @author Apache Trinidad Team
 *
 */
public class MockUIComponentWrapper
{

   public MockUIComponentWrapper(Mock mock, UIComponent component)
   {
     _mock = mock;
     _component = component;
   }
   
   public Mock getMock()
   {
     return _mock;
   }
   
   public UIComponent getUIComponent()
   {
     return _component;
   }
   
   private Mock _mock;
   private UIComponent _component;
  
}