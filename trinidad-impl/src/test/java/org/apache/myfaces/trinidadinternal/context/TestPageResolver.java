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
package org.apache.myfaces.trinidadinternal.context;

import org.apache.myfaces.trinidad.context.PageResolver;

/**
 * A default implementation of the page resolver that returns the view ID
 * as the physical page name.
 */
public class TestPageResolver extends PageResolver
{
  @Override
  public String getPhysicalURI(String viewId)
  {
    return viewId;
  }

  @Override
  public String encodeActionURI(String actionURI)
  {
    return actionURI;
  }
}
