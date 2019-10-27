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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
  webEnvironment = DEFINED_PORT,
  classes = TestsConfiguration.class
)
class UsersApiTests {

  @Autowired
  Server server;

  @Autowired
  UsersApi api;

  @BeforeEach
  void beforeEach () {
    server.refresh();
  }

  @Test
  void create () {
    val user = api.create("Artem", "good_cat@mail.ru");
    assertThat(user)
        .isNotNull()
        .extracting(User::getId)
        .isNotNull();
  }

  @Test
  void getAll () {
    val users = api.getAll();
    assertThat(users)
        .isNotNull()
        .hasSize(3);
  }

  @Test
  void getAllByName () {
    val users = api.getAllByName("Sergey");
    assertThat(users)
        .isNotNull()
        .hasSize(1);
  }

  @Test
  void get () {
    val user = api.get(1);
    assertThat(user)
        .isPresent()
        .map(User::getId)
        .hasValue(1);
  }

  @Test
  void getUnsafe () {
    val user = api.get(999);
    assertThat(user).isEmpty();
  }

  @Test
  void patch () {
    val response = api.patch(1, new User().withEmail("popa@mail.ru"));

    assertThat(response.status()).isEqualTo(204);
    assertThat(response.headers()).containsKey("Patch-Id");
  }

  @Test
  void delete () {
    api.delete(1, Server.TOKEN);

    assertThat(server)
        .extracting(it -> it.hasUser(1))
        .isEqualTo(false);
  }

  @Test
  void getAdmin () {
    val user = api.getAdmin();

    assertThat(user)
        .isNotNull()
        .matches(it -> it.getId() == 0)
        .matches(it -> "admin".equals(it.getName()));
  }

  @Test
  void deleteAll () {
    api.deleteAll(Server.TOKEN);

    assertThat(server)
        .extracting(Server::isEmpty)
        .isEqualTo(true);
  }
}
