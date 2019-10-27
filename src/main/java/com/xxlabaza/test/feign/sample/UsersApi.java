/*
 * Copyright 2019 the original author or authors.
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

package com.xxlabaza.test.feign.sample;

import static feign.Logger.Level.FULL;

import java.util.List;
import java.util.Optional;

import feign.Body;
import feign.Feign;
import feign.Headers;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.optionals.OptionalDecoder;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import feign.slf4j.Slf4jLogger;
import lombok.val;

@Headers("Content-Type: application/json")
interface UsersApi {

  @RequestLine("POST /user")
  @Headers("Server-Key: popa")
  // json curly braces must be escaped!
  @Body("%7B\"name\": \"{name}\", \"email\": \"{email}\"%7D")
  User create (@Param("name") String name, @Param("email") String email);

  @RequestLine("GET /users")
  List<User> getAll ();

  @RequestLine("GET /users?name={name}")
  List<User> getAllByName (@Param("name") String name);

  @RequestLine("GET /user/{id}")
  Optional<User> get (@Param("id") int id);

  @RequestLine("GET /user/{id}")
  User getUnsafe (@Param("id") int id);

  @RequestLine("PATCH /user/{id}")
  Response patch (@Param("id") int id, User updates);

  @RequestLine("DELETE /user/{id}")
  @Headers("Authorization: {secretToken}")
  void delete (@Param("id") int id, @Param("secretToken") String token);

  default User getAdmin () {
    return getUnsafe(0);
  }

  default void deleteAll (String token) {
    for (val user : getAll()) {
      delete(user.getId(), token);
    }
  }

  static UsersApi create (String url) {
    return Feign.builder()
        .encoder(new JacksonEncoder())
        .decoder(new OptionalDecoder(new JacksonDecoder()))
        .client(new OkHttpClient())
        .logger(new Slf4jLogger())
        .logLevel(FULL)
        .requestInterceptor(new EachRequestInterceptor())
        .decode404()
        .target(UsersApi.class, url);
  }
}
