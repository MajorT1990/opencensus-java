/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/** Native implementation {@link CenusContext} serialization. */
final class CensusSerializer {
  private static final char TAG_PREFIX = '\2';
  private static final char TAG_DELIM = '\3';

  // The serialized tags are of the form: (<tag prefix> + 'key' + <tag delim> + 'value')*
  static ByteBuffer serialize(CensusContextImpl context) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, String> tag : context.tags.entrySet()) {
      builder
          .append(TAG_PREFIX)
          .append(tag.getKey())
          .append(TAG_DELIM)
          .append(tag.getValue());
    }
    return ByteBuffer.wrap(builder.toString().getBytes(UTF_8));
  }

  // The serialized tags are of the form: (<tag prefix> + 'key' + <tag delim> + 'value')*
  @Nullable
  static CensusContextImpl deserialize(ByteBuffer buffer) {
    String input = new String(buffer.array(), UTF_8);
    HashMap<String, String> tags = new HashMap<>();
    if (!input.matches("(\2[^\2\3]*\3[^\2\3]*)*")) {
      return null;
    }
    if (!input.isEmpty()) {
      int keyIndex = 0;
      do {
        int valIndex = input.indexOf(TAG_DELIM, keyIndex + 1);
        String key = input.substring(keyIndex + 1, valIndex);
        keyIndex = input.indexOf(TAG_PREFIX, valIndex + 1);
        String val = input.substring(valIndex + 1, keyIndex == -1 ? input.length() : keyIndex);
        tags.put(key, val);
      } while (keyIndex != -1);
    }
    return new CensusContextImpl(tags);
  }
}
