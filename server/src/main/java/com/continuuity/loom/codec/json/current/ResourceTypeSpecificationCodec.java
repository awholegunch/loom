/*
 * Copyright 2012-2014, Continuuity, Inc.
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

package com.continuuity.loom.codec.json.current;

import com.continuuity.loom.codec.json.AbstractCodec;
import com.continuuity.loom.spec.plugin.ResourceTypeFormat;
import com.continuuity.loom.spec.plugin.ResourceTypeSpecification;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

/**
 * Codec for serializing/deserializing a {@link ResourceTypeSpecification}. Used to handle enums.
 */
public class ResourceTypeSpecificationCodec extends AbstractCodec<ResourceTypeSpecification> {

  @Override
  public JsonElement serialize(ResourceTypeSpecification spec, Type type, JsonSerializationContext context) {
    JsonObject jsonObj = new JsonObject();

    jsonObj.add("format", context.serialize(spec.getFormat()));
    jsonObj.add("permissions", context.serialize(spec.getPermissions()));

    return jsonObj;
  }

  @Override
  public ResourceTypeSpecification deserialize(JsonElement json, Type type, JsonDeserializationContext context)
    throws JsonParseException {
    JsonObject jsonObj = json.getAsJsonObject();

    ResourceTypeFormat format = context.deserialize(jsonObj.get("format"), ResourceTypeFormat.class);
    String permissions = context.deserialize(jsonObj.get("permissions"), String.class);

    return new ResourceTypeSpecification(format, permissions);
  }
}
