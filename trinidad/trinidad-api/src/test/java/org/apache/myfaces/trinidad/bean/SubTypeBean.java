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
package org.apache.myfaces.trinidad.bean;

import org.apache.myfaces.trinidad.bean.PropertyKey;


public class SubTypeBean extends TestBean
{
  static public final Type TYPE = new Type(TestBean.TYPE);
  static public final PropertyKey SUB_KEY =
    TYPE.registerKey("sub");
  
  @Override
  public Type getType()
  {
    return TYPE;
  }

  public String getSub()
  {
    return (String) getProperty(SUB_KEY);
  }

  public void setSub(String sub)
  {
    setProperty(SUB_KEY, sub);
  }
}
