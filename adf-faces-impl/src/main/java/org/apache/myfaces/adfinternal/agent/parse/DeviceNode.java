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
package org.apache.myfaces.adfinternal.agent.parse;

import org.apache.myfaces.adf.context.Agent;

/**
 * Object that holds information about the device nodes in capabilities file
 */
class DeviceNode
{
  public DeviceNode(String id, NameVersion makeModel,
                    String extendsId, DeviceComponentNode[] components)
  {
    _id = id;
    //Really should be using only the name part. There is no version here
    _makeModel = makeModel;
    _extendsId = extendsId;
    _componentNodes = components;
  }

  DeviceComponentNode[] __getComponents()
  {
    return _componentNodes;
  }

  double __matches(Agent agent)
  {
    //TODO: Have a "Does not matter" match constant
    if (_makeModel == null)
      return 1;

    String makeModel = agent.getHardwareMakeModel();
    return _makeModel.match(makeModel, null);
  }

  private String _id;
  private NameVersion _makeModel;
  private String _extendsId;
  private DeviceComponentNode[] _componentNodes;
}
